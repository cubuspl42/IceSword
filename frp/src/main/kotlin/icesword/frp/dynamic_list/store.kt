package icesword.frp.dynamic_list

import icesword.frp.BehaviorComputation
import icesword.frp.Cell
import icesword.frp.RawCell
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.mapTill

@BehaviorComputation
fun <E> DynamicList.Companion.store(
    initialIdentifiedContent: List<DynamicList.IdentifiedElement<E>>,
    buildChanges: (contentView: List<DynamicList.IdentifiedElement<E>>) -> Stream<ListChange<E>>,
    tillFreeze: Till,
): DynamicList<E> =
    StoreDynamicList(
        initialIdentifiedContent = initialIdentifiedContent,
        buildChanges = buildChanges,
        tillFreeze = tillFreeze,
    )

class StoreDynamicList<E>(
    initialIdentifiedContent: List<DynamicList.IdentifiedElement<E>>,
    buildChanges: (contentView: List<DynamicList.IdentifiedElement<E>>) -> Stream<ListChange<E>>,
    tillFreeze: Till,
) : DynamicList<E> {
    private var mutableContent: MutableList<DynamicList.IdentifiedElement<E>> =
        initialIdentifiedContent.toMutableList()

    override val changes: Stream<ListChange<E>> = buildChanges(mutableContent)
        .mapTill(tillFreeze) { change ->
            change.apply {
                applyTo(mutableContent)
            }
        }

    override val content: Cell<List<E>>
        get() = RawCell(
            { volatileContentView.toList() },
            changes.map { volatileContentView.toList() },
        )

    override val volatileIdentifiedContentView: List<DynamicList.IdentifiedElement<E>>
        get() = mutableContent

    override val volatileContentView: List<E>
        // TODO: Lazy mapping?
        get() = volatileIdentifiedContentView.map { it.element }
}
