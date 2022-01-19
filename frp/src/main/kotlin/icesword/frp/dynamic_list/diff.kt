package icesword.frp.dynamic_list

import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.dynamic_list.DynamicList.IdentifiedElement
import icesword.frp.map
import icesword.frp.switchMap

fun <E> DynamicList.Companion.diff(content: Cell<List<E>>): DynamicList<E> =
    diffIdentified(
        identifiedContent = content.map(::identifyByOrder),
    )

fun <E> DynamicList.Companion.diffIdentified(identifiedContent: Cell<List<IdentifiedElement<E>>>): DynamicList<E> =
    DiffDynamicList(identifiedContent = identifiedContent)

fun <E> DynamicList.Companion.diff(content: Cell<DynamicList<E>>): DynamicList<E> =
    DynamicList.diff(content = content.switchMap { it.content })

private class DiffDynamicList<E /* : Eq<E> */>(
    override val identifiedContent: Cell<List<IdentifiedElement<E>>>,
) : InstantiatingDynamicList<E>() {
    override val content: Cell<List<E>> = identifiedContent.map { identifiedContentNow ->
        identifiedContentNow.map { it.element }
    }

    override fun buildContent(): MutableList<IdentifiedElement<E>> =
        identifiedContent.sample().toMutableList()

    override fun buildChanges(): Stream<ListChange<E>> = identifiedContent.changes.map { valueChange ->
        val oldContent: List<IdentifiedElement<E>> = valueChange.oldValue
        val newContent: List<IdentifiedElement<E>> = valueChange.newValue

        ListChange.diffIdentified(oldContent, newContent)
    }
}
