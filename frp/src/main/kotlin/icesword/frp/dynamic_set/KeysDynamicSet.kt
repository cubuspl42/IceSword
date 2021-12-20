package icesword.frp.dynamic_set

import icesword.frp.*

class KeysDynamicSet<K, V>(
    private val source: DynamicMap<K, V>,
    tag: String,
) : SimpleDynamicSet<K>(tag = tag) {
    private var mutableContent: MutableSet<K>? = null

    override val volatileContentView: Set<K>
        get() = mutableContent!!

    private var subscription: Subscription? = null

    override val changes: Stream<SetChange<K>>
        get() = Stream.source(this::subscribe, tag = "MapDynamicSet.changes")

    override fun onStart() {
        subscription = source.changes.subscribe { change ->
            val outChange = SetChange(
                added = change.added.keys,
                removed = change.removedEntries.keys,
            )

            outChange.applyTo(mutableContent!!)
            notifyListeners(outChange)
        }

        mutableContent = source.volatileContentView.keys.toMutableSet()
    }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()
    }
}
