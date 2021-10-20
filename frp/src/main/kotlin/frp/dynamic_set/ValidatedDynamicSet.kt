package icesword.frp.dynamic_set

import icesword.frp.*

open class ValidatedDynamicSet<A>(
    private val source: DynamicSet<A>,
    private val sourceTag: String,
) : SimpleDynamicSet<A>(tag = "$sourceTag-validated") {
    private var mutableContent: MutableSet<A>? = null

    override val volatileContentView: Set<A>
        get() = source.volatileContentView

    private var subscription: Subscription? = null

//    override val content: Cell<Set<A>>
//        get() = RawCell(
//            { mutableContent!!.toSet() },
//            changes.map { mutableContent!!.toSet() },
//        )

    override val content: Cell<Set<A>>
        get() = source.content

    private fun sampleUncached(): Set<A> =
        source.sample()

    override fun onStart() {
        subscription = source.changes.subscribe { change ->
            val addedUpdatedIntersection = change.added.intersect(change.removed)
            if (addedUpdatedIntersection.isNotEmpty()) {
                throw IllegalStateException("Dynamic set #${sourceTag} adds and removes same keys: $addedUpdatedIntersection")
            }

            change.added.forEach {
                if (!source.containsNow(it)) {
                    throw IllegalStateException("Dynamic set #${sourceTag} does not expose added value: $it")
                }

                if (mutableContent!!.contains(it)) {
                    throw IllegalStateException("Dynamic set #${sourceTag} already contains $it (attempted to add)")
                }
            }

            change.removed.forEach {
                if (source.containsNow(it)) {
                    throw IllegalStateException("Dynamic set #${sourceTag} still exposes removed element: $it")

                }

                if (!mutableContent!!.contains(it)) {
                    throw IllegalStateException("Dynamic set #${sourceTag} removed a value that it shouldn't have contained: $it")
                }
            }

            val intersection = change.added.intersect(change.removed)

            if (intersection.isNotEmpty()) {
                throw IllegalStateException("intersection.isNotEmpty()")
            }

            change.applyTo(mutableContent!!)
            notifyListeners(change)
        }

        mutableContent = sampleUncached().toMutableSet()
    }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()
        subscription = null
    }
}

class MutableValidatedDynamicSet<A>(
    private val source: MutableDynamicSet<A>,
    sourceTag: String,
) : ValidatedDynamicSet<A>(
    source = source,
    sourceTag = sourceTag,
), MutableDynamicSet<A> {
    override fun add(element: A) {
        source.add(element)
    }

    override fun remove(element: A) {
        source.remove(element)
    }
}
