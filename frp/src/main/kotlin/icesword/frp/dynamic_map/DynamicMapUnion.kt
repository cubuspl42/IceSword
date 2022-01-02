package icesword.frp.dynamic_map

import icesword.frp.*

class DynamicMapUnion<K, V>(
    private val source1: DynamicMap<K, V>,
    private val source2: DynamicMap<K, V>,
    identity: Identity,
) : SimpleDynamicMap<K, V>(identity = identity) {
    private var keyMap: MutableMap<K, K>? = null

//    private var mutableContent: MutableMap<K, V>? = null

    override val volatileContentView: Map<K, V>
        get() = (source1.volatileContentView + source2.volatileContentView)

    override fun containsKeyNow(key: K): Boolean =
        source1.containsKeyNow(key) || source2.containsKeyNow(key)

    override fun getNow(key: K): V? =
        source1.getNow(key) ?: source2.getNow(key)

    private var subscription1: Subscription? = null

    private var subscription2: Subscription? = null

    override fun onStart() {
        subscription1 = source1.changes.subscribe { change ->
            val unionChange = MapChange(
                added = change.added.filter { (key, _) -> !source2.containsKeyNow(key) },
                updated = change.updated.filter { (key, _) -> !source2.containsKeyNow(key) },
                removedEntries = change.removedEntries.filter { (key, _) -> !source2.containsKeyNow(key) },
            )

//            unionChange.applyTo(mutableContent!!)

            processChange(unionChange)
        }

        subscription2 = source2.changes.subscribe { change ->
            val unionChange = MapChange(
                added = change.added.filter { (key, _) -> !source1.containsKeyNow(key) },
                updated = change.updated +
                        change.added.mapNotNull { (key, value) ->
                            if (source1.containsKeyNow(key)) key to value
                            else null
                        } +
                        change.removed.mapNotNull { key ->
                            source1.getNow(key)?.let { value ->
                                key to value
                            }
                        }.toMap(),
                removedEntries = change.removedEntries.filter { (key, _) -> !source1.containsKeyNow(key) },
            )

            processChange(unionChange)
        }

//        mutableContent = (source1.volatileContentView + source2.volatileContentView).toMutableMap()
    }

    override fun onStop() {
//        mutableContent = null

        subscription2!!.unsubscribe()
        subscription2 = null

        subscription1!!.unsubscribe()
        subscription1 = null
    }

    private fun processChange(change: MapChange<K, V>) {
        if (!change.isEmpty()) {
//            change.applyTo(mutableContent)
            notifyListeners(change)
        }
    }

}


