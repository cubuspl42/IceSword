package icesword.frp.dynamic_list

import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.map

class MapDynamicList<E /* : Eq<E> */>(
    override val content: Cell<List<E>>,
) : InstantiatingDynamicList<E>() {
    override fun buildContent(): MutableList<DynamicList.IdentifiedElement<E>> =
        DynamicList.identifyByOrder(content.sample()).toMutableList()

    override fun buildChanges(): Stream<ListChange<E>> = content.changes.map { valueChange ->
        val oldContent: List<E> = valueChange.oldValue
        val newContent: List<E> = valueChange.newValue

        ListChange.diff(oldContent, newContent)
    }
}
