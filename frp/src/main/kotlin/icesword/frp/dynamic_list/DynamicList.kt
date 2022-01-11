package icesword.frp.dynamic_list

import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
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
        fun <E /* : Eq */> identifyByOrder(list: List<E>): List<IdentifiedElement<E>> {
            val elementCounter = CounterMap<E>()

            return list.map {
                val oldCount = elementCounter.increaseCount(it)

                IdentifiedElement(
                    element = it,
                    identity = OrderIdentity(
                        element = it,
                        order = oldCount,
                    ),
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

data class OrderIdentity<E>(
    val element: E,
    val order: Int,
) : DynamicList.ElementIdentity

fun <E> staticListOf(vararg elements: E): DynamicList<E> =
    DynamicList.of(elements.toList())

val <E> DynamicList<E>.size: Cell<Int>
    get() = content.map { it.size }

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
