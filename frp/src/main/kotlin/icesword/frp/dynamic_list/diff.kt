package icesword.frp.dynamic_list

import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.map
import icesword.frp.switchMap

fun <E> DynamicList.Companion.diff(content: Cell<List<E>>): DynamicList<E> =
    DiffDynamicList(content = content)

fun <E> DynamicList.Companion.diff(content: Cell<DynamicList<E>>): DynamicList<E> =
    DynamicList.diff(content = content.switchMap { it.content })

private class DiffDynamicList<E /* : Eq<E> */>(
    override val content: Cell<List<E>>,
) : InstantiatingDynamicList<E>() {
    override fun buildContent(): MutableList<DynamicList.IdentifiedElement<E>> =
        identifyByOrder(content.sample()).toMutableList()

    override fun buildChanges(): Stream<ListChange<E>> = content.changes.map { valueChange ->
        val oldContent: List<E> = valueChange.oldValue
        val newContent: List<E> = valueChange.newValue

        ListChange.diff(oldContent, newContent)
    }
}
