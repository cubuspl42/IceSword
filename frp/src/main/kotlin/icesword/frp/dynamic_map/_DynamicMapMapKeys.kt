package icesword.frp.dynamic_map

import icesword.frp.*

class _DynamicMapMapKeys<K, K2, V>(
    private val source: DynamicMap<K, V>,
    private val transform: (Map.Entry<K, V>) -> K2,
    tag: String,
) : SimpleDynamicMap<K2, V>(tag = tag) {
    private var keyMap: MutableMap<K, K2>? = null

    private var mutableContent: MutableMap<K2, V>? = null

//    override val volatileContentView: Map<K2, V>
//        get() = mutableContent ?: sampleUncached()

    override val volatileContentView: Map<K2, V>
        get() = mutableContent!!

    private var subscription: Subscription? = null

    private fun sampleUncached(): Map<K2, V> {
        return source.sample().mapKeys(transform)
    }

    override fun onStart() {
        subscription = source.changes.subscribe { change ->
            val mappedChange = change.mapKeys(transform, keyMap!!)
            mappedChange.applyTo(mutableContent!!)
            notifyListeners(mappedChange)
        }

        mutableContent = sampleUncached().toMutableMap()
    }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()
    }

}