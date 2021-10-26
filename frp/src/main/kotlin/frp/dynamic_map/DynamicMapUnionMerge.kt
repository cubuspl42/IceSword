package icesword.frp.dynamic_map

import icesword.collections.MapFactory
import icesword.collections.associateWithThrough
import icesword.frp.*

class DynamicMapUnionMerge<K, V, R>(
    private val through: MapFactory<K, R>,
    private val maps: DynamicSet<DynamicMap<K, V>>,
    private val merge: (Set<V>) -> R,
    tag: String? = null,
) : SimpleDynamicMap<K, R>(tag = tag ?: "DynamicMapUnionMerge") {

    private var mutableContent: MutableMap<K, R>? = null

//    override val volatileContentView: Map<K, R>
//        get() {
//            val initialKeys = maps.volatileContentView.flatMap { it.volatileContentView.keys }.toSet()
//            return initialKeys.associateWith { key ->
//                val values = maps.volatileContentView.mapNotNull { it.getNow(key) }.toSet()
//                merge(values)
//            }
//        }

    override val volatileContentView: Map<K, R>
        get() = this.mutableContent!!


//    override fun containsKeyNow(key: K): Boolean =
//        maps.volatileContentView.any { it.containsKeyNow(key) }
//
//    override fun getNow(key: K): R? {
//        val values: Set<V> = maps.volatileContentView.mapNotNull { it.getNow(key) }.toSet()
//        return if (values.isEmpty()) null
//        else merge(values)
//    }

    private var subscriptionOuter: Subscription? = null

    private var subscriptionMap: MutableMap<DynamicMap<K, V>, Subscription>? = null

    override fun onStart() {
        // TODO: Emit changes for outer map change
        maps.changes.subscribe { outerChange: SetChange<DynamicMap<K, V>> ->

            val allMaps = maps.volatileContentView

            outerChange.added.forEach { subscribeToInner(it) }
            outerChange.removed.forEach {
                val subscription = subscriptionMap!!.remove(it)!!
                subscription.unsubscribe()
            }

            if (outerChange.added.size > 1) {
                throw UnsupportedOperationException("unionMarge outer change added.size != 1 (change: $outerChange)")
            }

            val addedMap = outerChange.added.singleOrNull() ?: DynamicMap.empty()

            val addedAffectedEntries = addedMap.volatileContentView.entries

            val addedAddedEntries = addedAffectedEntries.asSequence()
                .filter { (k, v) -> !volatileContentView.containsKey(k) }.toSet()

            val addedUpdatedEntries = addedAffectedEntries.asSequence()
                .filter { (k, v) -> volatileContentView.containsKey(k) }.toSet()

            val removedAffectedEntries = outerChange.removed.asSequence().flatMap {
                it.volatileContentView.entries
            }.toSet()

            val removedUpdated = removedAffectedEntries.asSequence()
                .mapNotNull { (k, v) ->
                    val values = allMaps.asSequence().mapNotNull { it.getNow(k) }.toSet()
                    if (values.isNotEmpty()) k to merge(values)
                    else null
                }
                .toMap()

            val added = addedAddedEntries.associate { (k, v) -> k to merge(setOf(v)) }

            val updated = addedUpdatedEntries.associate { (k, _) ->
                k to merge(allMaps.mapNotNull { it.getNow(k) }.toSet())
            } + removedUpdated

            val removedEntries = removedAffectedEntries.asSequence()
                .mapNotNull { (k, v) ->
                    val values = allMaps.asSequence().mapNotNull { it.getNow(k) }.toSet()
                    val oldValue = volatileContentView[k]!!
                    if (values.isEmpty()) k to oldValue
                    else null
                }
                .toMap()

            val outChange = MapChange(
                added = added,
                updated = updated,
                removedEntries = removedEntries,
            )

            processChange(outChange)
        }

        subscriptionMap = maps.volatileContentView.asSequence()
            .associateWith { innerMap -> subscribeToInner(innerMap) }
            .toMutableMap()

        val initialKeys = maps.volatileContentView.asSequence()
            .flatMap { it.volatileContentView.keys }

        val initialContent = initialKeys
            .associateWithThrough(through) { key ->
                val values = maps.volatileContentView.asSequence()
                    .mapNotNull { it.getNow(key) }.toSet()
                merge(values)
            }

        mutableContent = initialContent
    }

    override fun onStop() {
        subscriptionMap!!.forEach { (_, sub) ->
            sub.unsubscribe()
        }
        subscriptionMap = null

        subscriptionOuter!!.unsubscribe()
        subscriptionOuter = null

    }

    private fun subscribeToInner(innerMap: DynamicMap<K, V>): Subscription =
        innerMap.changes.subscribe { change ->
            val allMaps = maps.volatileContentView
            val otherMaps = allMaps.filter { it != innerMap }

            val addedActually = change.added.filter { (k, _) ->
                otherMaps.none { it.containsKeyNow(k) }
            }

            val addedUpdated = change.added.filter { (k, v) ->
                otherMaps.any {
                    val v2 = it.getNow(k)
                    v2 != null && v2 != v
                }
            }

            val removedEntriesActually = change.removedEntries.filter { (k, _) ->
                otherMaps.none { it.containsKeyNow(k) }
            }

            val removedEntriesUpdated = change.removedEntries.filter { (k, v) ->
                otherMaps.any {
                    val v2 = it.getNow(k)
                    v2 != null && v2 != v
                }
            }

            val updated = addedUpdated + change.updated + removedEntriesUpdated

            val outChange = MapChange<K, R>(
                added = addedActually.mapValues { (_, v) ->
                    merge(setOf(v))
                },
                updated = updated.mapValues { (k, _) ->
                    merge(allMaps.mapNotNull { it.getNow(k) }.toSet())
                },
                removedEntries = removedEntriesActually.mapValues { (k, v) ->
                    merge(allMaps.mapNotNull { it.getNow(k) }.toSet() + setOf(v))
                },
            )

            processChange(outChange)
        }

    private fun processChange(change: MapChange<K, R>) {
        if (!change.isEmpty()) {
            change.applyTo(mutableContent!!)
            notifyListeners(change)
        }
    }
}
