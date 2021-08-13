package icesword.frp.dynamic_map

import icesword.frp.*

class DynamicSetAssociateWith<K, V>(
    private val source: DynamicSet<K>,
    private val valueSelector: (K) -> V,
    tag: String,
) : SimpleDynamicMap<K, V>(tag = tag) {
    private var mutableContent: MutableMap<K, V>? = null

    override val volatileContentView: Map<K, V>
        get() = mutableContent!!

    private var subscription: Subscription? = null

    override val content: Cell<Map<K, V>>
        get() = RawCell(
            { mutableContent!!.toMap() },
            changes.map { mutableContent!!.toMap() },
        )

    override fun onStart() {
        subscription = source.changes.subscribe { change ->
            val added = change.added.associateWith(valueSelector)

            val mappedChange = MapChange(
                added = added,
                updated = emptyMap(),
                removed = change.removed,
            )

            val intersect = change.added.intersect(mutableContent!!.keys)
            if (intersect.isNotEmpty()) {
                debugLog { "$name: change.added.keys.intersect: $intersect" }
                throw IllegalStateException("change.added.keys.intersect")
            }

            mappedChange.applyTo(mutableContent!!)

            notifyListeners(mappedChange)
        }

        val initialContent = source.volatileContentView
            .associateWith(valueSelector)

        debugLog { "$name: initializing mutable content (source: $source)" }
        debugLog { "$name: initialContent: $initialContent" }

        mutableContent = initialContent.toMutableMap()
    }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()

        subscription = null
    }
}
