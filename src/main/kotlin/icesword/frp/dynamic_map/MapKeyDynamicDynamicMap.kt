package icesword.frp.dynamic_map

import icesword.frp.*

class MapKeyDynamicDynamicMap<K, V, K2 : Any>(
    private val source: DynamicMap<K, V>,
    private val transform: (Map.Entry<K, V>) -> K2?,
    tag: String,
) : SimpleDynamicMap<K2, V>(
    tag = tag,
) {
//    private var keyMap: MutableMap<K, V>? = null

    private var mutableContent: MutableMap<K2, V>? = null

//    override val volatileContentView: Map<K, V>
//        get() = mutableContent ?: sampleUncached()

    override val volatileContentView: Map<K2, V>
        get() = mutableContent!!

    private var subscription: Subscription? = null

    override val content: Cell<Map<K2, V>>
        get() = RawCell(
            { mutableContent!!.toMap() },
            changes.map { mutableContent!!.toMap() },
        )

//    private fun sampleUncached(): Map<K2, V> {
//        return source.sample().mapKeys(transform)
//    }

    override fun onStart() {
//        subscription = source.changes.subscribe { change ->
//            // Added in source
//            val added1 = change.added.mapNotNull { entry ->
//                transform(entry)?.let { entry.key to it }
//            }.toMap()
//
//            // Updated in source, but filtered-out before
//            val added2 = change.updated.mapNotNull { entry ->
//                val key = entry.key
//                transform(entry)
//                    ?.takeIf { !mutableContent!!.containsKey(key) }
//                    ?.let { key to it }
//            }.toMap()
//
//            // Updated in source, not filtered-out before
//            val updated = change.updated.mapNotNull { entry ->
//                val key = entry.key
//                transform(entry)
//                    ?.takeIf { mutableContent!!.containsKey(key) }
//                    ?.let { key to it }
//            }.toMap()
//
//            // Removed in source
//            val removed1 = change.removedEntries.mapNotNull { (key, _) ->
//                volatileContentView[key]?.let { value -> key to value }
//            }.toMap()
//
//            // Updated in source, not filtered-out before
//            val removed2 = change.updated.mapNotNull { entry ->
//                val key = entry.key
//                when {
//                    transform(entry) == null -> volatileContentView[key]?.let { value -> key to value }
//                    else -> null
//                }
//            }.toMap()
//
//            val mappedChange = MapChange<K2, V>(
//                added = added1 + added2,
//                updated = updated,
//                removedEntries = removed1 + removed2,
//            )
//
//            mappedChange.applyTo(mutableContent!!)
//            notifyListeners(mappedChange)
//        }
//
//        val initialMutableContent: MutableMap<K2, V> = source.sample().entries
//            .mapNotNull { entry -> transform(entry)?.let { entry.key to it } }
//            .toMap()
//            .toMutableMap()
//
//        mutableContent = initialMutableContent
    }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()
    }

}