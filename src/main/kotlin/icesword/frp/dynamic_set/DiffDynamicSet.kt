package icesword.frp.dynamic_set

import icesword.frp.*

class DiffDynamicSet<A>(
    private val inputContent: Cell<Set<A>>,
) : SimpleDynamicSet<A>(tag = "DiffDynamicSet") {
    private var mutableContent: MutableSet<A>? = null

    override val volatileContentView: Set<A>
        get() = mutableContent ?: inputContent.sample()

    private var subscription: Subscription? = null

    override val changes: Stream<SetChange<A>>
        get() = Stream.source(this::subscribe)

    override val content: Cell<Set<A>>
        get() = RawCell(
            { mutableContent!!.toSet() },
            changes.map { mutableContent!!.toSet() },
        )

    override fun onStart() {
        subscription = inputContent.subscribe { newContent ->
            val change = SetChange.diff(mutableContent!!, newContent)

            if (change.added.intersect(mutableContent!!).isNotEmpty()) {
                throw IllegalStateException("DiffDynamicSet: change.added.intersect")
            }

            change.applyTo(mutableContent!!)
            notifyListeners(change)
        }

        mutableContent = inputContent.sample().toMutableSet()
    }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()
    }
}
