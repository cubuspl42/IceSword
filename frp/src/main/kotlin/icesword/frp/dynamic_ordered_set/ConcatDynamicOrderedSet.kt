package icesword.frp.dynamic_ordered_set

import icesword.frp.Subscription

class ConcatDynamicOrderedSet<A>(
    private val set1: DynamicOrderedSet<A>,
    private val set2: DynamicOrderedSet<A>,
) : SimpleDynamicOrderedSet<A>() {
    override val volatileContentView: List<A>
        get() = set1.volatileContentView + set2.volatileContentView

    private var subscription1: Subscription? = null
    private var subscription2: Subscription? = null

    override fun onStart() {
        subscription1 = set1.changes.subscribe { change ->
            val outChange = OrderedSetChange(
                inserted = change.inserted?.let { insertion ->
                    OrderedSetChange.Insertion(
                        value = insertion.value,
                        before = insertion.before ?: set2.volatileContentView.firstOrNull(),
                    )
                },
                removed = change.removed,
            )

            notifyListeners(outChange)
        }

        subscription2 = set2.changes.subscribe { change ->
            notifyListeners(change)
        }
    }

    override fun onStop() {
        subscription1!!.unsubscribe()
        subscription2!!.unsubscribe()
    }
}
