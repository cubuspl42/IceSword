package icesword.frp.dynamic_map

import icesword.frp.*

class DynamicMapUnionMerge<K, V, R>(
    private val maps: DynamicSet<DynamicMap<K, V>>,
    private val merge: (Set<V>) -> R,
    tag: String? = null,
) : SimpleDynamicMap<K, R>(tag = tag ?: "DynamicMapUnionMerge") {

    private var mutableContent: MutableMap<K, R>? = null

    override val volatileContentView: Map<K, R>
        get() = mutableContent!!

    private var subscriptionOuter: Subscription? = null

    private var subscriptionMap: MutableMap<DynamicMap<K, V>, Subscription>? = null

    override val content: Cell<Map<K, R>> by lazy {
        RawCell(
            { mutableContent!!.toMap() },
            changes.map { mutableContent!!.toMap() },
        )
    }

    override fun onStart() {
        // TODO: Emit changes for outer map change
        maps.changes.subscribe { outerChange: SetChange<DynamicMap<K, V>> ->
            outerChange.added.forEach { subscribeToInner(it) }
            outerChange.removed.forEach {
                val subscription = subscriptionMap!!.remove(it)!!
                subscription.unsubscribe()
            }
        }

        subscriptionMap = maps.volatileContentView.associateWith { innerMap ->
            subscribeToInner(innerMap)
        }.toMutableMap()

        val initialKeys = maps.volatileContentView.flatMap { it.volatileContentView.keys }.toSet()
        val initialContent = initialKeys.associateWith { key ->
            val values = maps.volatileContentView.mapNotNull { it.getNow(key) }.toSet()
            merge(values)
        }

        mutableContent = initialContent.toMutableMap()
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
                removedEntries = removedEntriesActually.mapValues { (k, _) ->
                    volatileContentView[k]!!
                },
            )

            outChange.applyTo(mutableContent!!)

            notifyListeners(outChange)
        }
}
