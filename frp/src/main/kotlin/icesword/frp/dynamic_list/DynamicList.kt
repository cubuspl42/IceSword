package icesword.frp.dynamic_list

import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.Stream
import icesword.frp.divertMap
import icesword.frp.dynamic_list.DynamicList.IdentifiedElement
import icesword.frp.map
import icesword.frp.switchMap
import icesword.indexOfOrNull

interface DynamicList<out E> {
    /**
     * Element of a list with an additional identity, which makes all elements
     * present in a specific list, at a specific time, distinguishable from each
     * other.
     *
     * There are no additional guarantees about the uniqueness of that
     * identity, especially between elements of a given list which weren't
     * present in that list at the same time or between elements of different
     * lists.
     *
     * The nature of the identity can differ between lists being results of
     * different operators.
     *
     * The [equals] and [hashCode] methods of this interface refer to the
     * additional identity described above.
     */
    interface IdentifiedElement<out E> {
        val element: E

        override fun equals(other: Any?): Boolean

        override fun hashCode(): Int
    }

    companion object {
        fun <E /* : Eq */> identifyByOrder(list: List<E>): List<IdentifiedElement<E>> {
            val elementCounter = CounterMap<E>()

            return list.map {
                val oldCount = elementCounter.increaseCount(it)

                OrderIdentifiedElement(
                    element = it,
                    order = oldCount,
                )
            }
        }

        fun <E> of(list: List<E>): DynamicList<E> =
            DynamicList.diff(content = constant(list))

        fun <E : Any> ofSingle(element: Cell<E?>): DynamicList<E> =
            DynamicList.diff(
                content = element.map { it?.let(::listOf) ?: emptyList() },
            )

        fun <E> merge(list: DynamicList<Stream<E>>): Stream<E> =
            list.content.divertMap { Stream.merge(it) }

        fun <E> diff(content: Cell<List<E>>): DynamicList<E> =
            DiffDynamicList(content = content)

        fun <E> diff(content: Cell<DynamicList<E>>): DynamicList<E> =
            DynamicList.diff(content = content.switchMap { it.content })

        fun <E> fuse(content: DynamicList<Cell<E>>): DynamicList<E> =
            DynamicList.diff(
                content = content.content.switchMap { cells ->
                    Cell.traverse(cells) { it }
                },
            )

        fun <E> fuse(content: List<Cell<E>>): DynamicList<E> =
            DynamicList.diff(
                content = Cell.traverse(content) { it },
            )

        fun <E> concat(lists: Iterable<DynamicList<E>>): DynamicList<E> =
            DynamicList.diff(
                content = Cell.traverse(lists) { dl -> dl.content }
                    .map { ls: List<List<E>> -> ls.flatten() },
            )

        fun <E> concat(vararg lists: DynamicList<E>): DynamicList<E> =
            concat(lists.toList())

        fun <E> empty(): DynamicList<E> =
            DynamicList.diff(constant(emptyList()))
    }

    val changes: Stream<ListChange<E>>

    val content: Cell<List<E>>

    val volatileIdentifiedContentView: List<IdentifiedElement<E>>

    // May be view, may be copy
    val volatileContentView: List<E>
        get() = content.sample()
}

data class OrderIdentifiedElement<E>(
    override val element: E,
    val order: Int,
) : IdentifiedElement<E>


fun <E> staticListOf(vararg elements: E): DynamicList<E> =
    DynamicList.of(elements.toList())

val <E> DynamicList<E>.size: Cell<Int>
    get() = content.map { it.size }

data class ListChange<out E>(
    val added: Set<AddedElement<E>>,
    val removed: Set<RemovedElement<E>>,
    val reordered: Set<ReorderedElement<E>>,
) {
    data class AddedElement<out E>(
        /**
         * Index at which the added element is inserted.
         *
         * Insertion index is in range from 0 (push-front) to list size (push-back).
         */
        val indexAfter: Int,
        /**
         * The element being added.
         */
        val addedElement: IdentifiedElement<E>,
    )

    data class RemovedElement<out E>(
        /**
         * Index at which the removed element was present at the moment of removal.
         */
        val indexBefore: Int,
        /**
         * The element being removed.
         */
        val removedElement: IdentifiedElement<E>,
    )

    data class ReorderedElement<out E>(
        /**
         * Index at which the removed element was present at the moment of removal.
         */
        val indexBefore: Int,
        /**
         *
         */
        val indexAfter: Int,
        /**
         * The element being reordered.
         */
        val reorderedElement: IdentifiedElement<E>,
    )

    companion object {
        fun <E> diff(
            oldList: List<E>,
            newList: List<E>,
        ): ListChange<E> {
            val oldListIdentified = DynamicList.identifyByOrder(oldList)
            val newListIdentified = DynamicList.identifyByOrder(newList)

            val added: Set<AddedElement<E>> =
                newListIdentified.mapIndexedNotNull { indexNew, newIdentifiedElement ->
                    if (newIdentifiedElement !in oldListIdentified)
                        AddedElement(
                            indexAfter = indexNew,
                            addedElement = newIdentifiedElement,
                        )
                    else null
                }.toSet()

            val removed: Set<RemovedElement<E>> =
                oldListIdentified.mapIndexedNotNull { indexOld, oldIdentifiedElement ->
                    if (oldIdentifiedElement !in newListIdentified)
                        RemovedElement(
                            indexBefore = indexOld,
                            removedElement = oldIdentifiedElement,
                        )
                    else null
                }.toSet()

            val reordered: Set<ReorderedElement<E>> =
                newListIdentified.mapIndexedNotNull { indexNew, newIdentifiedElement ->
                    oldListIdentified.indexOfOrNull(newIdentifiedElement)?.let { indexOld ->
                        if (indexOld != indexNew) ReorderedElement(
                            indexBefore = indexOld,
                            indexAfter = indexNew,
                            reorderedElement = newIdentifiedElement,
                        ) else null
                    }
                }.toSet()

            return ListChange(
                added = added,
                removed = removed,
                reordered = reordered,
            )
        }
    }

}

fun <E> ListChange<E>.applyTo(
    oldContent: List<IdentifiedElement<E>>,
): List<IdentifiedElement<E>> {
    val newSize = oldContent.size + added.size - removed.size
    val newContent = MutableList<IdentifiedElement<E>?>(newSize) { null }

    added.forEach {
        newContent[it.indexAfter] = it.addedElement
    }

    reordered.forEach {
        newContent[it.indexAfter] = it.reorderedElement
    }

    return newContent.mapIndexed { index, it -> it ?: oldContent[index] }
}

private class CounterMap<A> {
    private val map = mutableMapOf<A, Int>()

    private fun getCount(a: A) =
        map.getOrElse(a) { 0 }

    fun increaseCount(a: A): Int {
        val oldCount = getCount(a)
        map[a] = oldCount + 1
        return oldCount
    }

    private fun decreaseCount(a: A) {
        val oldCount = map.getOrElse(a) { throw IllegalStateException() }

        if (oldCount == 0) {
            map.remove(a)
        } else {
            map[a] = oldCount - 1
        }
    }
}
