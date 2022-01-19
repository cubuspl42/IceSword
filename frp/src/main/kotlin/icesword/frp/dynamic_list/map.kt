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

private fun <E, R> ListChange<E>.map(transform: (E) -> R): ListChange<R> =
    ListChange(
        pushIns = pushIns.map { it.map(transform) }.toSet(),
        pullOuts = pullOuts.map { it.map(transform) }.toSet(),
    )

private fun <E, R> ListChange.PushIn<E>.map(transform: (E) -> R): ListChange.PushIn<R> =
    ListChange.PushIn(
        indexBefore = indexBefore,
        indexAfter = indexAfter,
        pushedInElements = pushedInElements.map { it.map(transform) },
    )

private fun <E, R> ListChange.PullOut<E>.map(transform: (E) -> R): ListChange.PullOut<R> =
    ListChange.PullOut(
        indexBefore = indexBefore,
        pulledOutElement = pulledOutElement.map(transform),
    )
