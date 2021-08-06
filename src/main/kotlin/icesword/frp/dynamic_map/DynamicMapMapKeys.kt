package icesword.frp.dynamic_map

import icesword.frp.*

class DynamicMapMapKeys<K, K2, V>(
    private val source: DynamicMap<K, V>,
    private val transform: (Map.Entry<K, V>) -> K2
) : SimpleDynamicMap<K2, V>() {
    private var keyMap: MutableMap<K, K2>? = null

    private var mutableContent: MutableMap<K2, V>? = null

    private var subscription: Subscription? = null

    override val content: Cell<Map<K2, V>>
        get() = RawCell(
            { mutableContent!!.toMap() },
            changes.map { mutableContent!!.toMap() },
        )

    override fun onStart() {
        subscription = source.changes.subscribe { change ->
            val mappedChange = change.mapKeys(transform, keyMap!!)
            mappedChange.applyTo(mutableContent!!)
            notifyListeners(mappedChange)
        }

        mutableContent = source.sample().mapKeys(transform).toMutableMap()
    }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()
    }
}