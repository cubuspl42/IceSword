package icesword.frp.dynamic_set

import icesword.frp.*

class DiffDynamicSet<A>(
    private val inputContent: Cell<Set<A>>,
    tag: String,
) : SimpleDynamicSet<A>(tag = tag) {
    private var mutableContent: MutableSet<A>? = null

    override val volatileContentView: Set<A>
        get() = mutableContent ?: inputContent.sample()

    private var subscription: Subscription? = null

    override val changes: Stream<SetChange<A>>
        get() = Stream.source(this::subscribe, tag = "DiffDynamicSet.changes")

    override val content: Cell<Set<A>>
        get() = RawCell(
            { mutableContent!!.toSet() },
            changes.map { mutableContent!!.toSet() },
        )

    override fun onStart() {
        subscription = inputContent.values().subscribe { newContent ->
//            println("DiffDynamicSet newContent: $newContent")

            val change = SetChange.diff(mutableContent!!, newContent)

            if (change.added.intersect(mutableContent!!).isNotEmpty()) {
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
