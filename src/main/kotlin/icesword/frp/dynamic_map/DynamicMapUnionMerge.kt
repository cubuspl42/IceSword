package icesword.frp.dynamic_map

import icesword.SegregationTag
import icesword.SegregationTag3
import icesword.frp.*
import icesword.segregate
import icesword.segregate3
import kotlinx.css.map

class DynamicMapUnionMerge<K, V, R>(
    private val maps: DynamicSet<DynamicMap<K, V>>,
    merge: (Set<V>) -> R,
) : SimpleDynamicMap<K, R>(tag = "DynamicMapUnionMerge") {

//    private var mutableContent: MutableSet<K>? = null

//    private var linksMap: MutableMap<K, MutableSet<DynamicMap<K, V>>>? = null

    override val volatileContentView: Map<K, R>
        get() = TODO()

//    override val volatileContentView: Set<K>
//        get() = mutableContent!!

    private var subscriptionOuter: Subscription? = null

    private var subscriptionMap: MutableMap<DynamicMap<K, V>, Subscription>? = null


//    private var subscription2: Subscription? = null

//    override val content: Cell<Set<K>>
//        get() = RawCell(
//            { mutableContent!!.toSet() },
//            changes.map { mutableContent!!.toSet() },
//        )

//    override val content: Cell<Map<K, R>>
//        get() = TODO()

    override val content: Cell<Map<K, R>>
        get() = RawCell(
            { volatileContentView },
            changes.map { volatileContentView },
        )

    override fun onStart() {
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

//            val foo = change.added.keys + change.updated.ke

            // added or updated, other map contains same entry -> nothing
            // added, no other map contains same key -> added: marge(added values)
            // added, other map contains same key -> updated: marge(all values under same key)

            // removed: removed

            val addedSegregated = change.added.entries.segregate3 { (k, v) ->
                when {
                    otherMaps.any { it.getNow(k) == v } -> SegregationTag3.A // shadowed
                    otherMaps.none { it.containsKeyNow(k) } -> SegregationTag3.B // added
                    else -> SegregationTag3.C // updated
                }
            }

            val addedShadowed = addedSegregated.groupA
            val addedActually = addedSegregated.groupB
            val addedUpdated = addedSegregated.groupC

            val updatedSegregated = change.updated.entries.segregate3 { (k, v) ->

                when {
                    otherMaps.any { it.getNow(k) == v } -> SegregationTag3.A // shadowed
                    otherMaps.none { it.containsKeyNow(k) } -> SegregationTag3.B // added
                    else -> SegregationTag3.C // updated
                } // updated actually

            }


            val removed = change.added.filter { (k, _) ->
                otherMaps.none { it.containsKeyNow(k) }
            }

        }

}


