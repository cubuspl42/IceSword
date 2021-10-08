package icesword.frp.dynamic_set

import icesword.frp.*

class ValidatedDynamicSet<A>(
    private val source: DynamicSet<A>,
    tag: String,
) : SimpleDynamicSet<A>(tag = "${tag}-validated") {
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
            change.added.forEach {
                if (!source.containsNow(it)) {
                    throw IllegalStateException("Dynamic set #${tag} does not expose added value: $it")
                }

                if (mutableContent!!.contains(it)) {
                    throw IllegalStateException("Dynamic set #${tag} already contains $it (attempted to add)")
                }
            }

            change.removed.forEach {
                if (source.containsNow(it)) {
                    throw IllegalStateException("Dynamic set #${tag} still exposes removed element: $it")

                }

                if (!mutableContent!!.contains(it)) {
                    throw IllegalStateException("Dynamic set #${tag} does not contain $it (attempted to remove)")
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
