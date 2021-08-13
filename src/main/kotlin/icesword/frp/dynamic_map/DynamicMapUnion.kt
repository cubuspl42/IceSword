package icesword.frp.dynamic_map

import icesword.frp.*

class DynamicMapUnion<K, V>(
    private val source1: DynamicMap<K, V>,
    private val source2: DynamicMap<K, V>,
    tag: String,
) : SimpleDynamicMap<K, V>(tag = tag) {
    private var keyMap: MutableMap<K, K>? = null

    private var mutableContent: MutableMap<K, V>? = null

    override val volatileContentView: Map<K, V>
        get() = mutableContent!!

    private var subscription1: Subscription? = null

    private var subscription2: Subscription? = null

    //    override val content: Cell<Map<K, V>>
//        get() = RawCell(
//            { mutableContent!!.toMap() },
//            changes.map { mutableContent!!.toMap() },
//        )
//
    override val content: Cell<Map<K, V>>
        get() = RawCell(
            { mutableContent!! },
            changes.map { mutableContent!! },
        )

    override fun onStart() {
        subscription1 = source1.changes.subscribe { change ->
            val unionChange = MapChange(
                added = change.added.filter { (key, _) -> !source2.containsKeyNow(key) },
                updated = change.updated.filter { (key, _) -> !source2.containsKeyNow(key) },
                removed = change.removed.filter { key -> !source2.containsKeyNow(key) }.toSet(),
            )

            unionChange.applyTo(mutableContent!!)

            notifyListeners(unionChange)
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

                removed = change.removed.filter { key -> !source1.containsKeyNow(key) }.toSet(),
            )

            unionChange.applyTo(mutableContent!!)

            notifyListeners(unionChange)
        }

        mutableContent = (source1.volatileContentView + source2.volatileContentView).toMutableMap()
    }

    override fun onStop() {
        mutableContent = null

        subscription2!!.unsubscribe()
        subscription2 = null

        subscription1!!.unsubscribe()
        subscription1 = null
    }

}


