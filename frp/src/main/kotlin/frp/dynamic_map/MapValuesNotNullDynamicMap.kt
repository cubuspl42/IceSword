package icesword.frp.dynamic_map

import icesword.frp.*

class MapValuesNotNullDynamicMap<K, V, V2 : Any>(
    private val source: DynamicMap<K, V>,
    private val transform: (Map.Entry<K, V>) -> V2?,
    tag: String,
) : SimpleDynamicMap<K, V2>(
    tag = tag,
) {
//    private var keyMap: MutableMap<K, V>? = null

    private var mutableContent: MutableMap<K, V2>? = null

//    override val volatileContentView: Map<K, V>
//        get() = mutableContent ?: sampleUncached()

    override val volatileContentView: Map<K, V2>
        get() = mutableContent!!

    private var subscription: Subscription? = null

//    private fun sampleUncached(): Map<K, V2> {
//        return source.sample().mapKeys(transform)
//    }

    override fun onStart() {
        subscription = source.changes.subscribe { change ->
            // Added in source
            val added1 = change.added.mapNotNull { entry ->
                transform(entry)?.let { entry.key to it }
            }.toMap()

            // Updated in source, but filtered-out before
            val added2 = change.updated.mapNotNull { entry ->
                val key = entry.key
                transform(entry)
                    ?.takeIf { !containsKeyNow(key) }
                    ?.let { key to it }
            }.toMap()

            // Updated in source, not filtered-out before
            val updated = change.updated.mapNotNull { entry ->
                val key = entry.key
                transform(entry)
                    ?.takeIf { containsKeyNow(key) }
                    ?.let { key to it }
            }.toMap()

            // Removed in source
            val removed1 = change.removedEntries.mapNotNull { (key, _) ->
                getNow(key)?.let { value -> key to value }
            }.toMap()

            // Updated in source, not filtered-out before
            val removed2 = change.updated.mapNotNull { entry ->
                val key = entry.key
                when {
                    transform(entry) == null -> getNow(key)?.let { value -> key to value }
                    else -> null
                }
            }.toMap()

            val mappedChange = MapChange<K, V2>(
                added = added1 + added2,
                updated = updated,
                removedEntries = removed1 + removed2,
            )

//            println("mappedChange: $mappedChange")


//            println("mutableContent: $mutableContent")

            if (!mappedChange.isEmpty()) {
                mappedChange.validated().applyTo(mutableContent!!)
                notifyListeners(mappedChange)
            }
        }

        val initialMutableContent: MutableMap<K, V2> = source.sample().entries
            .mapNotNull { entry -> transform(entry)?.let { entry.key to it } }
            .toMap()
            .toMutableMap()

        mutableContent = initialMutableContent
    }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()
    }

}