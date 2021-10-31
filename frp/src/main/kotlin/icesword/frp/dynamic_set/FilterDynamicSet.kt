package icesword.frp.dynamic_set

import icesword.frp.*

class FilterDynamicSet<A>(
    private val source: DynamicSet<A>,
    private val test: (A) -> Boolean,
    tag: String,
) : SimpleDynamicSet<A>(
    tag = tag,
) {
    override val volatileContentView: Set<A>
        get() = source.volatileContentView.asSequence()
            .filter(test).toSet()

    private var subscription: Subscription? = null

    override fun onStart() {
        subscription = source.changes.subscribe { change ->
            // Added in source
            val added = change.added.asSequence().filter(test).toSet()

            // Removed in source
            val removed = change.removed.asSequence().filter(test).toSet()

            val outChange = SetChange(
                added = added,
                removed = removed,
            )

            if (!outChange.isEmpty()) {
                notifyListeners(outChange)
            }
        }
    }

    override fun onStop() {
        subscription!!.unsubscribe()
    }
}
