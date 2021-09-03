package icesword.frp.dynamic_map

import icesword.frp.*

class FromEntriesDynamicMap<K, V>(
    private val source: DynamicSet<Pair<K, V>>,
    tag: String,
) : SimpleDynamicMap<K, V>(tag = tag) {
    private var mutableContent: MutableMap<K, V>? = null

    override val volatileContentView: Map<K, V>
        get() = mutableContent!!

    private var subscription: Subscription? = null

    override val content: Cell<Map<K, V>>
        get() = RawCell(
            { mutableContent!!.toMap() },
            changes.map { mutableContent!!.toMap() },
        )

    override fun onStart() {
        subscription = source.changes.subscribe { change ->
            val outChange = MapChange(
                added = change.added.toMap(),
                updated = emptyMap(),
                removedEntries = change.removed.toMap(),
            )

            outChange.applyTo(mutableContent!!)
            notifyListeners(outChange)
        }

        mutableContent = source.volatileContentView.toMap().toMutableMap()
    }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()
        subscription = null
    }
}
