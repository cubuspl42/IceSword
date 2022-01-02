package icesword.frp.dynamic_set

import icesword.frp.*

class DiffDynamicSet<A>(
    private val inputContent: Cell<Set<A>>,
    identity: Identity,
) : SimpleDynamicSet<A>(identity = identity) {
    private var mutableContent: MutableSet<A>? = null

    override val volatileContentView: Set<A>
        get() = mutableContent ?: inputContent.sample()

    private var subscription: Subscription? = null

    override val changes: Stream<SetChange<A>>
        get() = Stream.source(this::subscribe, tag = "DiffDynamicSet.changes")

    override fun onStart() {
        subscription = inputContent.values().subscribe { newContent ->
//            println("DiffDynamicSet newContent: $newContent")

            val change = SetChange.diff(mutableContent!!, newContent)

            if (change.added.intersect(mutableContent!!).isNotEmpty()) {
                logError("Dynamic map already contains added value")
                throw IllegalStateException("DiffDynamicSet: change.added.intersect")
            }

            change.applyTo(mutableContent!!)


//            println("DiffDynamicSet notifyListeners")
            notifyListeners(change)
        }

        mutableContent = inputContent.sample().toMutableSet()
    }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()
    }
}
