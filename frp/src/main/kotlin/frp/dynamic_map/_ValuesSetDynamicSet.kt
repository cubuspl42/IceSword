package icesword.frp.dynamic_map

import icesword.frp.*

class _ValuesSetDynamicSet<K, V>(
    private val source: DynamicMap<K, V>,
) : SimpleDynamicSet<V>(tag = "MapDynamicSet") {
//    private var mutableContent: MutableSet<V>? = null

    private var keysMap: MutableMap<V, MutableSet<K>>? = null

    override val volatileContentView: Set<V>
        get() = keysMap!!.keys

    private var subscription: Subscription? = null

    override val changes: Stream<SetChange<V>>
        get() = Stream.source(this::subscribe, tag = "ValuesSetDynamicSet.changes")

    override val content: Cell<Set<V>>
        get() = RawCell(
            { volatileContentView },
            changes.map { volatileContentView },
        )

    override fun onStart() {
        // Returns: true if value was actually added
        fun processAddedValue(key: K, addedValue: V): Boolean {
            debugLog { "$name: added value: $addedValue" }

            val keys = keysMap!!.getOrPut(addedValue) { mutableSetOf() }
            return keys.isEmpty().also { keys.add(key) }
        }

        fun processAddedValues(added: Map<K, V>): Collection<V> =
            added.filter { (key, value) -> processAddedValue(key, value) }.values

        // Returns: true if value was actually removed
        fun processRemovedValue(key: K, value: V): Boolean {
            debugLog { "$name: removed value: $value" }

            val keysMap = this.keysMap!!
            val keys = keysMap[value]!!

            if (keys.isEmpty()) {
                throw IllegalStateException("keys.isEmpty()")
            }

            return (keys.size == 1).also {
                val wasThere = keys.remove(key)

                if (keys.isEmpty()) {
                    keysMap.remove(value)
                }

                if (!wasThere) {
                    throw IllegalStateException("!wasThere")
                }
            }
        }

        fun processRemovedValues(removed: Map<K, V>): Collection<V> =
            removed.filter { (key, value) -> processRemovedValue(key, value) }.values

        subscription = source.changes.subscribe { change ->
            val added = processAddedValues(change.added)


            debugLog { "$name: source updated values: ${change.updated}" }

            change.added.keys.map { key -> }

            val updatedKeys = change.updated.keys

            val updateRemovedEntries = keysMap!!.mapNotNull { (value, keys) ->
                val commonKeys = updatedKeys.intersect(keys)
                when (commonKeys.size) {
                    0 -> null
                    1 -> commonKeys.first() to value
                    else -> throw IllegalStateException()
                }
            }.toMap()

            val updatedRemoved = processRemovedValues(updateRemovedEntries)

            val updatedAdded = processAddedValues(change.updated)
//            val updatedRemoved = processRemovedValues(change.updated)

            // FIXME: This is bugged (doesn't handle source updates correctly, probably)

            val removed = processRemovedValues(change.removedEntries)

            val addedTotal = added.toSet() + updatedRemoved.toSet()
            val removedTotal = removed.toSet() + updatedAdded.toSet()

            val outChange = SetChange(
                added = addedTotal - removedTotal,
                removed = removedTotal - addedTotal,
            )

            notifyListeners(outChange)
        }

        keysMap = mutableMapOf()

        processAddedValues(source.volatileContentView)
    }

    override fun onStop() {
        keysMap = null

        subscription!!.unsubscribe()
    }
}
