package icesword.frp.dynamic_list

import icesword.indexOfOrNull

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
        val addedElement: DynamicList.IdentifiedElement<E>,
    )

    data class RemovedElement<out E>(
        /**
         * Index at which the removed element was present at the moment of removal.
         */
        val indexBefore: Int,
        /**
         * The element being removed.
         */
        val removedElement: DynamicList.IdentifiedElement<E>,
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
        val reorderedElement: DynamicList.IdentifiedElement<E>,
    )

    companion object {
        fun <E> diff(
            oldList: List<E>,
            newList: List<E>,
        ): ListChange<E> {
            val oldListIdentified = identifyByOrder(oldList)
            val newListIdentified = identifyByOrder(newList)

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
    oldContent: List<DynamicList.IdentifiedElement<E>>,
): List<DynamicList.IdentifiedElement<E>> {
    val newSize = oldContent.size + added.size - removed.size
    val newContent = MutableList<DynamicList.IdentifiedElement<E>?>(newSize) { null }

    added.forEach {
        newContent[it.indexAfter] = it.addedElement
    }

    reordered.forEach {
        newContent[it.indexAfter] = it.reorderedElement
    }

    return newContent.mapIndexed { index, it -> it ?: oldContent[index] }
}