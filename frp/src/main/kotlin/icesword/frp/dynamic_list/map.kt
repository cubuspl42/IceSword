package icesword.frp.dynamic_list

import icesword.frp.Stream
import icesword.frp.dynamic_list.DynamicList.IdentifiedElement
import icesword.frp.map

fun <E, R> DynamicList<E>.map(transform: (element: E) -> R): DynamicList<R> =
    MapDynamicList(
        source = this,
        transform = transform,
    )

private class MapDynamicList<E, R>(
    private val source: DynamicList<E>,
    private val transform: (E) -> R,
) : InstantiatingDynamicList<R>() {
    override fun buildContent(): MutableList<IdentifiedElement<R>> =
        source.volatileIdentifiedContentView.map {
            it.map(transform)
        }.toMutableList()

    override fun buildChanges(): Stream<ListChange<R>> = source.changes.map { change ->
        change.map(transform)
    }
}
