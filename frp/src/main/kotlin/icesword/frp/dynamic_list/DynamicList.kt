package icesword.frp.dynamic_list

import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.IndexedIdentity
import icesword.frp.KeyIdentity
import icesword.frp.RawCell
import icesword.frp.Stream
import icesword.frp.divertMap
import icesword.frp.map
import icesword.frp.switchMap

interface DynamicList<out E> {
    data class IdentifiedElement<out E>(
        val element: E,
        val identity: ElementIdentity,
    )

    /**
     * An additional identity of an element of the list, which makes all
     * elements present in a specific list distinguishable from all the other
     * elements present in that list at the same time.
     *
     * There are no additional guarantees about the uniqueness of that
     * identity, especially between elements of a given list which weren't
     * present in that list at the same time or between elements of different
     * lists.
     *
     * The nature of the identity can differ between lists being results of
     * different operators.
     */
    interface ElementIdentity {
        override fun equals(other: Any?): Boolean

        override fun hashCode(): Int
    }

    companion object {
        fun <E> of(list: List<E>): DynamicList<E> =
            DynamicList.diff(content = constant(list))

        fun <E : Any> ofSingle(element: Cell<E?>): DynamicList<E> =
            DynamicList.diffIdentified(
                identifiedContent = element.map { elementOrNull ->
                    listOfNotNull(
                        elementOrNull?.let { element ->
                            IdentifiedElement(
                                element = element,
                                // FIXME: Glitch-prone
                                identity = ReferenceIdentity.allocate(),
                            )
                        },
                    )
                },
            )

        fun <E> merge(list: DynamicList<Stream<E>>): Stream<E> =
            list.content.divertMap { Stream.merge(it) }

        fun <E> fuse(content: DynamicList<Cell<E>>): DynamicList<E> =
            DynamicList.diffIdentified(
                identifiedContent = content.identifiedContent.switchMap { identifiedCells ->
                    Cell.traverse(identifiedCells) { identifiedCell ->
                        identifiedCell.element.map { valueNow -> identifiedCell.map { valueNow } }
                    }
                },
            )

        fun <E> concat(lists: Iterable<DynamicList<E>>): DynamicList<E> =
            DynamicList.diffIdentified(
                identifiedContent = Cell.traverse(lists.withIndex()) { (index, dynamicList) ->
                    dynamicList.identifiedContent.map { identifiedContentNow ->
                        identifiedContentNow.map { identifiedElement ->
                            IdentifiedElement(
                                element = identifiedElement.element,
                                identity = IndexedIdentity(
                                    index = index,
                                    identity = identifiedElement.identity,
                                )
                            )
                        }
                    }
                }.map { it.flatten() },
            )

        fun <E> concat(vararg lists: DynamicList<E>): DynamicList<E> =
            concat(lists.toList())

        fun <E> empty(): DynamicList<E> =
            DynamicList.diff(constant(emptyList()))
    }

    val changes: Stream<ListChange<E>>

    val identifiedContent: Cell<List<IdentifiedElement<E>>>
        get() = RawCell(
            { volatileIdentifiedContentView.toList() },
            changes.map { volatileIdentifiedContentView.toList() },
        )

    val content: Cell<List<E>>

    val volatileIdentifiedContentView: List<IdentifiedElement<E>>

    // May be view, may be copy
    val volatileContentView: List<E>
        get() = content.sample()
}

fun <E, R> DynamicList.IdentifiedElement<E>.map(transform: (E) -> R) =
    DynamicList.IdentifiedElement(
        element = transform(this.element),
        identity = this.identity,
    )

val <E> DynamicList<E>.size: Cell<Int>
    get() = content.map { it.size }

fun <E> staticListOf(vararg elements: E): DynamicList<E> =
    DynamicList.of(elements.toList())
