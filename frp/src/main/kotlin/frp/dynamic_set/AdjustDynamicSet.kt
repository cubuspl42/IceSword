package icesword.frp.dynamic_set

import icesword.frp.*

class AdjustDynamicSet<K, A>(
    private val source: DynamicSet<K>,
    private val adjustment: Cell<A>,
    private val combine: (K, A) -> K,
    tag: String,
) : SimpleDynamicSet<K>(tag = tag) {
    private var mutableContent: MutableSet<K>? = null

    override val volatileContentView: Set<K>
        get() {
            val adj = adjustment.sample()
            return source.volatileContentView.asSequence()
                .map { combine(it, adj) }.toSet()
        }

    private var subscription: Subscription? = null

    override val changes: Stream<SetChange<K>>
        get() = Stream.source(this::subscribe, tag = "MapDynamicSet.changes")

    override val content: Cell<Set<K>>
        get() = RawCell(
            { mutableContent!!.toSet() },
            changes.map { mutableContent!!.toSet() },
        )

    override fun onStart() {
        fun processAdjustmentChange(adj: A): SetChange<K> {
            val newContent = this.source.volatileContentView.asSequence()
                .map { combine(it, adj) }.toMutableSet()

            val outChange = SetChange.diff(mutableContent!!, newContent)

            this.mutableContent = newContent

            return outChange
        }

        fun processSourceChange(change: SetChange<K>): SetChange<K> {
            val adj = adjustment.sample()

            val outChange = change.map { combine(it, adj) }

            return outChange
        }

        subscription = source.changes.map(::processSourceChange)
            .mergeWith(adjustment.values().map(::processAdjustmentChange))
            .subscribe { change -> notifyListeners(change) }

        mutableContent = source.volatileContentView.toMutableSet()
    }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()
    }
}
