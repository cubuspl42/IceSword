package icesword.frp.dynamic_set

import icesword.frp.*

class MemorizedDynamicSet<A>(
    private val source: DynamicSet<A>,
) : SimpleDynamicSet<A>(
    identity = SimpleObservable.Identity.build("MemorizedDynamicSet"),
) {
    private var mutableContent: MutableSet<A>? = null

    override val volatileContentView: Set<A>
        get() = mutableContent!!

    private var subscription: Subscription? = null

    override val changes: Stream<SetChange<A>>
        get() = Stream.source(this::subscribe, tag = "MapDynamicSet.changes")

    override fun onStart() {
        subscription = source.changes.subscribe { change ->
            change.applyTo(mutableContent!!)
            notifyListeners(change)
        }

        mutableContent = source.volatileContentView.toMutableSet()
    }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()
    }
}
