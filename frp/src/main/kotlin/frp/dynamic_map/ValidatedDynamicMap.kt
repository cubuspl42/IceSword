package icesword.frp.dynamic_map

import icesword.frp.*

class ValidatedDynamicMap<K, V>(
    private val source: DynamicMap<K, V>,
    tag: String,
) : SimpleDynamicMap<K, V>(tag = "${tag}-validated") {
    private var mutableContent: MutableMap<K, V>? = null

    override val volatileContentView: Map<K, V>
        get() = source.volatileContentView

    private var subscription: Subscription? = null

//    override val content: Cell<Map<K, V>>
//        get() = RawCell(
//            { mutableContent!!.toMap() },
//            changes.map { mutableContent!!.toMap() },
//        )

    override val content: Cell<Map<K, V>>
        get() = source.content

    private fun sampleUncached(): Map<K, V> =
        source.sample()

    override fun onStart() {
        subscription = source.changes.subscribe { change ->
            change.added.forEach {
                if (source.getNow(it.key) != it.value) {
                    throw IllegalStateException("source.getNow(it.key) != it.value")
                }

                if (mutableContent!!.containsKey(it.key)) {
                    throw IllegalStateException("Dynamic map #${tag} already contains key ${it.key} (attempted to add)")
                }
            }

            change.updated.forEach {
                if (source.getNow(it.key) != it.value) {
                    throw IllegalStateException("source.getNow(it.key) != it.value")
                }

                if (!mutableContent!!.containsKey(it.key)) {
                    throw IllegalStateException("Dynamic map #${tag} does not contain key ${it.key} (attempted to update)")
                }
            }

            change.removed.forEach {
                if (source.getNow(it) != null) {
                    throw IllegalStateException("source.getNow(it) != null")
                }

                if (!mutableContent!!.containsKey(it)) {
                    throw IllegalStateException("Dynamic map #${tag} does not contain key ${it} (attempted to remove)")
                }
            }

            change.applyTo(mutableContent!!)
            notifyListeners(change)
        }

        mutableContent = sampleUncached().toMutableMap()
    }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()
        subscription = null
    }
}
