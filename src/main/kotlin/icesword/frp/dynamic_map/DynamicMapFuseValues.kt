package icesword.frp.dynamic_map

import icesword.frp.*

class DynamicMapFuseValues<K, V>(
    private val source: DynamicMap<K, Cell<V>>,
) : SimpleDynamicMap<K, V>() {
    private var mutableContent: MutableMap<K, V>? = null

    private var subscriptionMap: MutableMap<K, Subscription>? = null

//    override val volatileContentView: Map<K, V>
//        get() = mutableContent ?: sampleUncached()

    override val volatileContentView: Map<K, V>
        get() = mutableContent!!

    private var subscription: Subscription? = null

    override val content: Cell<Map<K, V>>
        get() = RawCell(
            { mutableContent!!.toMap() },
            changes.map { mutableContent!!.toMap() },
        )

    private fun sampleUncached(): Map<K, V> {
        return source.sample().mapValues { (_, cell) -> cell.sample() }
    }

    private fun subscribeToCell(key: K, cell: Cell<V>) {
        val subscription = cell.subscribe { value ->
            mutableContent!![key] = value

            notifyListeners(
                MapChange(
                    added = emptyMap(),
                    updated = mapOf(key to value),
                    removed = emptySet(),
                ),
            )
        }

        mutableContent!![key] = cell.sample()

        subscriptionMap!![key] = subscription
    }


    private fun unsubscribeFromCell(key: K) {
        val subscription = subscriptionMap!![key]!!

        subscription.unsubscribe()

        subscriptionMap!!.remove(key)

        mutableContent!!.remove(key)
    }

    override fun onStart() {
        subscription = source.changes.subscribe { change ->
            change.added.forEach { (key, cell) ->
                subscribeToCell(key, cell)
            }

            change.updated.forEach { (key, cell) ->
                unsubscribeFromCell(key)
                subscribeToCell(key, cell)
            }

            change.removed.forEach { key ->
                unsubscribeFromCell(key)
            }

            val sampledChange = change.mapValues { (_, cell) ->
                cell.sample()
            }

            notifyListeners(sampledChange)
        }

        mutableContent = sampleUncached().toMutableMap()
    }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()
    }
}