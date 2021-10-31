package icesword.frp.dynamic_map

import icesword.collections.mapValuesLazy
import icesword.frp.*

class DynamicMapFuseValues<K, V>(
    private val source: DynamicMap<K, Cell<V>>,
    tag: String,
) : SimpleDynamicMap<K, V>(
    tag = tag,
) {
//    private var _mutableContent: MutableMap<K, V>? = null
//
//    private val mutableContent: MutableMap<K, V>
//        get() {
//            val content = _mutableContent
//            if (content == null) {
//                debugLog { "DynamicMapFuseValues#${id}: _mutableContent == null" }
//                throw IllegalStateException("_mutableContent == null")
//            } else {
//                return content
//            }
//        }

    private var subscriptionMap: MutableMap<K, Subscription>? = null

//    override val volatileContentView: Map<K, V>
//        get() = mutableContent ?: sampleUncached()

//    override val volatileContentView: Map<K, V>
//        get() = mutableContent

    override val volatileContentView: Map<K, V>
        get() = source.volatileContentView.mapValuesLazy { (_, cell) -> cell.sample() }

    override fun containsKeyNow(key: K): Boolean =
        source.containsKeyNow(key)

    override fun getNow(key: K): V? =
        source.getNow(key)?.sample()

    private var subscription: Subscription? = null


    private fun subscribeToCell(key: K, cell: Cell<V>) {
        debugLog { "$name: subscribe to cell @ key: $key" }

        val subscription = cell.subscribe { value ->
//            mutableContent[key] = value

            processChange(
                MapChange(
                    added = emptyMap(),
                    updated = mapOf(key to value),
                    removedEntries = emptyMap(),
                ),
            )
        }

//        mutableContent[key] = cell.sample()

        if (subscriptionMap!!.put(key, subscription) != null) {
            throw IllegalStateException("subscriptionMap!!.put(key,subscription ) != null")
        }
    }

    private fun unsubscribeFromCell(key: K) {
        val subscription = subscriptionMap!![key]!!

        subscription.unsubscribe()

        subscriptionMap!!.remove(key)

//        mutableContent.remove(key)
    }

    private fun handleChange(change: MapChange<K, Cell<V>>) {
        debugLog { "$name: source change: $change" }

        change.added.forEach { (key, cell) ->

            subscribeToCell(key, cell)
        }

        change.updated.forEach { (key, cell) ->
            unsubscribeFromCell(key)
            subscribeToCell(key, cell)
        }


        val sampledChange = change.mapValues { (_, cell) ->
            cell.sample()
        }


        change.removed.forEach { key ->
//                 FIXME
            unsubscribeFromCell(key)
        }

        processChange(sampledChange)
    }

    private fun processChange(change: MapChange<K, V>) {
        if (!change.isEmpty()) {
//            change.applyTo(mutableContent)
            notifyListeners(change)
        }
    }

    override fun onStart() {
        subscription = source.changes.subscribe(this::handleChange)

//        _mutableContent = mutableMapOf()

        subscriptionMap = mutableMapOf()

        source.volatileContentView.forEach { (key, cell) ->
            subscribeToCell(key, cell)
        }
    }

    override fun onStop() {
        subscriptionMap!!.forEach { (_, sub) ->
            sub.unsubscribe()
        }

        subscriptionMap = null

//        _mutableContent = null

        subscription!!.unsubscribe()

        subscription = null
    }
}
