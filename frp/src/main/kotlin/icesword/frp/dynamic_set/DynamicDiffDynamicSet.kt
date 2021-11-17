package icesword.frp.dynamic_set

import icesword.frp.*

class DynamicDiffDynamicSet<A>(
    private val inputContent: Cell<DynamicSet<A>>,
    tag: String,
) : SimpleDynamicSet<A>(tag = tag) {
    private var mutableContent: MutableSet<A>? = null

    override val volatileContentView: Set<A>
        get() = mutableContent ?: inputContent.sample().volatileContentView

    private var subscription: Subscription? = null

    private var innerSubscription: Subscription? = null

    override val changes: Stream<SetChange<A>>
        get() = Stream.source(this::subscribe, tag = "DiffDynamicSet.changes")

    override val content: Cell<Set<A>>
        get() = RawCell(
            { mutableContent!!.toSet() },
            changes.map { mutableContent!!.toSet() },
        )

    override fun onStart() {
        subscription = inputContent.subscribe { newInner ->
            innerSubscription!!.unsubscribe()
            innerSubscription = subscribeInner(newInner)

            val change = SetChange.diff(mutableContent!!, newInner.volatileContentView)
            change.applyTo(mutableContent!!)

            notifyListeners(change)
        }

        val initialInner = inputContent.sample()

        innerSubscription = subscribeInner(initialInner)

        mutableContent = initialInner.volatileContentView.toMutableSet()
    }

    private fun subscribeInner(initialInner: DynamicSet<A>): Subscription =
        initialInner.changes.subscribe { innerChange ->
            innerChange.applyTo(mutableContent!!)
            notifyListeners(innerChange)
        }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()
    }
}
