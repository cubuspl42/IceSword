package icesword.frp.dynamic_ordered_set

import icesword.frp.Cell
import icesword.frp.SimpleObservable
import icesword.frp.Stream
import icesword.frp.Subscription

interface DynamicOrderedSet<A> {
    companion object {
        fun <A> of(content: List<A>): DynamicOrderedSet<A> =
            StaticDynamicOrderedSet(content = content)

        fun <A> ofSingle(element: Cell<A?>): DynamicOrderedSet<A> =
            OfSingleDynamicOrderedSet(source = element)

        fun <A> concat(
            set1: DynamicOrderedSet<A>,
            set2: DynamicOrderedSet<A>,
        ): DynamicOrderedSet<A> = ConcatDynamicOrderedSet(
            set1 = set1,
            set2 = set2,
        )
    }

    val volatileContentView: List<A>

    val changes: Stream<OrderedSetChange<A>>
}

class OrderedSetChange<A>(
    /// A removed value. If null, no value is removed by this change.
    /// Semantically, removing occurs before the insertion.
    val removed: A?,

    /// An inserted value. If null, no value is inserted by this change
    val inserted: Insertion<A>?,

    ) {
    data class Insertion<A>(
        /// A new value to be inserted
        val value: A,

        /// Before which element the new value should be inserted. If null, the
        /// new value should be appended at the end of the ordered set.
        val before: A?,
    )
}

abstract class SimpleDynamicOrderedSet<A> :
    DynamicOrderedSet<A>,
    SimpleObservable<OrderedSetChange<A>>(tag = "SimpleDynamicOrderedSet") {

    override val changes: Stream<OrderedSetChange<A>>
        get() = Stream.source(this::subscribe, tag = "SimpleDynamicSet.changes")

    override fun toString(): String = "SimpleDynamicSet(name = $name)"
}

class OfSingleDynamicOrderedSet<A>(
    private val source: Cell<A?>,
) : SimpleDynamicOrderedSet<A>() {

    override val volatileContentView: List<A>
        get() = source.sample()?.let { listOf(it) } ?: emptyList()

    private var subscription: Subscription? = null

    override fun onStart() {
        subscription = source.changes.subscribe { change ->
            val change = OrderedSetChange<A>(
                removed = change.oldValue,
                inserted = change.newValue?.let {
                    OrderedSetChange.Insertion(
                        value = it,
                        before = null,
                    )
                }
            )

            notifyListeners(change)
        }
    }

    override fun onStop() {
        subscription!!.unsubscribe()
    }
}

