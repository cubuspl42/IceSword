package icesword.frp.dynamic_list

import icesword.frp.dynamic_list.DynamicList.IdentifiedElement
import icesword.indexOfOrNull


data class ListChange<out E>(
    val pushIns: Set<PushIn<E>>,
    val pullOuts: Set<PullOut<E>>,
) {
    data class PushIn<out E>(
        val indexBefore: Int,
        val indexAfter: Int,
        val pushedInElements: List<IdentifiedElement<E>>,
    )

    data class PullOut<out E>(
        val indexBefore: Int,
        val pulledOutElement: IdentifiedElement<E>,
    )

    companion object {
        fun <E> empty() = ListChange<E>(
            pushIns = emptySet(),
            pullOuts = emptySet(),
        )

        fun <E> diff(
            oldList: List<E>,
            newList: List<E>,
        ): ListChange<E> {
            val oldListIdentified = identifyByOrder(oldList)
            val newListIdentified = identifyByOrder(newList)

            return diffIdentified(
                oldListIdentified = oldListIdentified,
                newListIdentified = newListIdentified,
            )
        }

        fun <E> diffIdentified(
            oldListIdentified: List<IdentifiedElement<E>>,
            newListIdentified: List<IdentifiedElement<E>>,
        ): ListChange<E> = object {
            fun buildChangeSkippingEqual(
                oldListIdentified: List<IdentifiedElement<E>>,
                oldShift: Int,
                newListIdentified: List<IdentifiedElement<E>>,
                newShift: Int,
            ): ListChange<E> {
                val leadingEqualElementCount = oldListIdentified.zip(newListIdentified)
                    .takeWhile { (oldElement, newElement) -> oldElement == newElement }
                    .count()

                return buildChangeToBoundary(
                    oldListIdentified = oldListIdentified.drop(leadingEqualElementCount),
                    oldShift = oldShift + leadingEqualElementCount,
                    newListIdentified = newListIdentified.drop(leadingEqualElementCount),
                    newShift = newShift + leadingEqualElementCount,
                )
            }

            fun buildChangeToBoundary(
                oldListIdentified: List<IdentifiedElement<E>>,
                oldShift: Int,
                newListIdentified: List<IdentifiedElement<E>>,
                newShift: Int,
            ): ListChange<E> {
                fun buildChange(
                    oldBoundaryIndex: Int,
                    newBoundaryIndex: Int,
                ): ListChange<E> {
                    val pullOuts = oldListIdentified.withIndex().toList()
                        .slice(0 until oldBoundaryIndex)
                        .map { (index, element) ->
                            PullOut(
                                indexBefore = oldShift + index,
                                pulledOutElement = element,
                            )
                        }
                        .toSet()

                    val pushInOrNull = newListIdentified.toList()
                        .slice(0 until newBoundaryIndex)
                        .takeIf { it.isNotEmpty() }?.let { pushedInElements ->
                            PushIn(
                                indexBefore = oldShift,
                                indexAfter = newShift,
                                pushedInElements = pushedInElements,
                            )
                        }

                    return ListChange(
                        pushIns = setOfNotNull(pushInOrNull),
                        pullOuts = pullOuts,
                    )
                }

                return oldListIdentified
                    .withIndex()
                    .firstNotNullOfOrNull { (oldIndex, element) ->
                        newListIdentified.indexOfOrNull(element)?.let { newIndex ->
                            Pair(oldIndex, newIndex)
                        }
                    }?.let { (oldBoundaryIndex, newBoundaryIndex) ->
                        buildChange(
                            oldBoundaryIndex = oldBoundaryIndex,
                            newBoundaryIndex = newBoundaryIndex,
                        ).unionWith(
                            buildChangeSkippingEqual(
                                oldListIdentified = oldListIdentified.drop(oldBoundaryIndex),
                                oldShift = oldShift + oldBoundaryIndex,
                                newListIdentified = newListIdentified.drop(newBoundaryIndex),
                                newShift = newShift + newBoundaryIndex,
                            ),
                        )
                    } ?: buildChange(
                    oldBoundaryIndex = oldListIdentified.size,
                    newBoundaryIndex = newListIdentified.size,
                )
            }
        }.buildChangeSkippingEqual(
            oldListIdentified = oldListIdentified,
            oldShift = 0,
            newListIdentified = newListIdentified,
            newShift = 0,
        )
    }

    val pushedInElements: Set<IdentifiedElement<E>>
        get() = pushIns.flatMap { it.pushedInElements }.toSet()

    val pulledOutElements: Set<IdentifiedElement<E>>
        get() = pullOuts.map { it.pulledOutElement }.toSet()

    val addedElements: Set<IdentifiedElement<E>>
        get() = pushedInElements - pulledOutElements

    val removedElements: Set<IdentifiedElement<E>>
        get() = pulledOutElements - pushedInElements
}

fun <E> ListChange<E>.unionWith(
    other: ListChange<E>,
): ListChange<E> = ListChange(
    pushIns = this.pushIns + other.pushIns,
    pullOuts = this.pullOuts + other.pullOuts,
)

fun <E> ListChange<E>.applyTo(
    mutableContent: MutableList<IdentifiedElement<E>>,
) {
    if (
        pushIns.any { it.indexBefore !in mutableContent.indices } ||
        pullOuts.any { it.indexBefore !in mutableContent.indices }
    ) {
        throw IndexOutOfBoundsException("List change refers to indices out of content bounds")
    }

    val pushInsSorted = pushIns.sortedBy { it.indexBefore }
    val pullOutsSorted = pullOuts.sortedBy { it.indexBefore }

    var pullOutIndex = 0
    var shift = 0

    fun performPullOutIteration(toIndex: Int) {
        while (true) {
            val nextRemove = pullOutsSorted.getOrNull(pullOutIndex) ?: break

            if (nextRemove.indexBefore >= toIndex) break

            val removedElement = mutableContent.removeAt(nextRemove.indexBefore + shift)

            if (nextRemove.pulledOutElement.identity != removedElement.identity) {
                throw IllegalStateException("Removed element doesn't have the expected identity")
            }

            shift -= 1
            pullOutIndex += 1
        }
    }

    fun performPushInIteration(
        pushIn: ListChange.PushIn<E>,
    ) {
        val indexAfter = pushIn.indexBefore + shift

        mutableContent.addAll(
            index = indexAfter,
            elements = pushIn.pushedInElements,
        )

        shift += pushIn.pushedInElements.size
    }

    pushInsSorted.forEach { pushIn ->
        performPullOutIteration(toIndex = pushIn.indexBefore)
        performPushInIteration(pushIn = pushIn)
    }

    performPullOutIteration(mutableContent.size)
}
