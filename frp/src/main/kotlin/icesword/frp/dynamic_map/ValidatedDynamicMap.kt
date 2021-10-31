package icesword.frp.dynamic_map

import icesword.frp.*

class ValidatedDynamicMap<K, V>(
    private val source: DynamicMap<K, V>,
    private val sourceTag: String,
) : SimpleDynamicMap<K, V>(tag = "${sourceTag}-validated") {
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
            validateChange(
                sourceTag = sourceTag,
                change = change,
            )

            change.added.forEach {
                if (source.getNow(it.key) != it.value) {
                    throw IllegalStateException("source.getNow(it.key) != it.value")
                }

                if (mutableContent!!.containsKey(it.key)) {
                    throw IllegalStateException("Dynamic map #${sourceTag} already contains key ${it.key} (attempted to add)")
                }
            }

            change.updated.forEach {
                val exposedValue = source.getNow(it.key)
                val updatedValue = it.value

                if (exposedValue != updatedValue) {
                    throw IllegalStateException("Dynamic map #${sourceTag}, for key ${it.key}, exposes value: $exposedValue, while emits update: $updatedValue")
                }

                if (!mutableContent!!.containsKey(it.key)) {
                    throw IllegalStateException("Dynamic map #${sourceTag} dependent does not contain key ${it.key} (attempted to update)")
                }
            }

            change.removed.forEach {
                if (source.getNow(it) != null) {
                    throw IllegalStateException("source.getNow(it) != null")
                }

                if (!mutableContent!!.containsKey(it)) {
                    throw IllegalStateException("Dynamic map #${sourceTag} does not contain key ${it} (attempted to remove)")
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

fun <K, V> validateChange(
    sourceTag: String,
    change: MapChange<K, V>,
) {
    val addedUpdatedIntersection = change.added.keys.intersect(change.updated.keys)
    if (addedUpdatedIntersection.isNotEmpty()) {
        throw IllegalStateException("Dynamic map #${sourceTag} adds and updates same keys: $addedUpdatedIntersection")
    }

    val updatedRemovedIntersection = change.updated.keys.intersect(change.removedEntries.keys)
    if (updatedRemovedIntersection.isNotEmpty()) {
        throw IllegalStateException("Dynamic map #${sourceTag} updates and removes same keys: $updatedRemovedIntersection")
    }

    val addedRemovedIntersection = change.added.keys.intersect(change.removedEntries.keys)
    if (addedRemovedIntersection.isNotEmpty()) {
        throw IllegalStateException("Dynamic map #${sourceTag} adds and removes same keys: $addedRemovedIntersection")
    }
}
