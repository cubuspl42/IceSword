package icesword.frp.dynamic_list

import icesword.frp.dynamic_list.DynamicList.IdentifiedElement

data class ReferenceIdentity(
    val reference: Any,
) : DynamicList.ElementIdentity {
    companion object {
        fun allocate(): ReferenceIdentity = ReferenceIdentity(object {})
    }
}

data class ListOperation<out E>(
    val inserts: List<InsertElement<E>>,
    val removes: Set<RemoveElement>,
    val reorders: List<ReorderElement>,
) {
    data class InsertElement<out E>(
        /**
         * Index at which the element should be inserted. It has to be in range
         * from 0 (push-front) to list size (push-back). If the index is less
         * list size, the element previously occupying the specified index is
         * shifted right.
         */
        val insertionIndex: Int,
        /**
         * The element being inserted.
         */
        val insertedElement: E,
    )

    data class RemoveElement(
        /**
         * Index of the element that should be removed.
         */
        val removalIndex: Int,
    )

    data class ReorderElement(
        val sourceIndex: Int,
        val destinationIndex: Int,
    )
}

fun <E> ListOperation<E>.applyTo(
    mutableContent: MutableList<IdentifiedElement<E>>,
) {
    fun performListMutation(
        effectiveInserts: List<ListOperation.InsertElement<IdentifiedElement<E>>>,
        effectiveRemoves: List<ListOperation.RemoveElement>,
    ) {
        var shift = 0
        var removeIndex = 0

        fun performRemovalIteration(indexLimit: Int) {
            while (true) {
                val nextRemove = effectiveRemoves.getOrNull(removeIndex) ?: break

                if (nextRemove.removalIndex >= indexLimit) break

                mutableContent.removeAt(nextRemove.removalIndex + shift)

                shift -= 1
                removeIndex += 1
            }
        }

        effectiveInserts.forEach { insert ->
            performRemovalIteration(indexLimit = insert.insertionIndex)

            mutableContent.add(
                index = insert.insertionIndex + shift,
                element = insert.insertedElement,
            )

            shift += 1
        }

        performRemovalIteration(mutableContent.size)
    }

    val actualIdentifiedInserts = inserts.map {
        ListOperation.InsertElement(
            insertionIndex = it.insertionIndex,
            insertedElement = IdentifiedElement(
                element = it.insertedElement,
                identity = ReferenceIdentity.allocate(),
            ),
        )
    }

    val reorderInserts = reorders.map {
        ListOperation.InsertElement(
            insertionIndex = it.destinationIndex,
            insertedElement = mutableContent[it.sourceIndex],
        )
    }

    val reorderRemoves = reorders.map {
        ListOperation.RemoveElement(
            removalIndex = it.sourceIndex,
        )
    }

    performListMutation(
        effectiveInserts = (actualIdentifiedInserts + reorderInserts)
            .sortedBy { it.insertionIndex },
        effectiveRemoves = (removes + reorderRemoves)
            .sortedBy { it.removalIndex }
    )
}
