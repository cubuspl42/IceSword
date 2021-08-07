package icesword.frp.dynamic_map

import icesword.frp.*

class DynamicSetAssociateWith<K, V>(
    private val source: DynamicSet<K>,
    private val valueSelector: (K) -> V,
) : SimpleDynamicMap<K, V>() {
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
            val added = change.added.associateWith(valueSelector)

            val mappedChange = MapChange(
                added = added,
                updated = emptyMap(),
                removed = change.removed,
            )

            mappedChange.applyTo(mutableContent!!)

            notifyListeners(mappedChange)
        }

        mutableContent = source.volatileContentView
            .associateWith(valueSelector).toMutableMap()
    }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()

        subscription = null
    }
}
