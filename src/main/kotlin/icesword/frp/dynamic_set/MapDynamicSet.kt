package icesword.frp.dynamic_set

import icesword.frp.*

class MapDynamicSet<A, B>(
    private val source: DynamicSet<A>,
    private val transform: (A) -> B,
) : SimpleDynamicSet<B>(tag = "MapDynamicSet") {
    private var mutableContent: MutableSet<B>? = null

    override val volatileContentView: Set<B>
        get() = mutableContent!!

    private var subscription: Subscription? = null

    override val changes: Stream<SetChange<B>>
        get() = Stream.source(this::subscribe)

    override val content: Cell<Set<B>>
        get() = RawCell(
            { mutableContent!!.toSet() },
            changes.map { mutableContent!!.toSet() },
        )

    override fun onStart() {
        subscription = source.changes.subscribe { change ->
            val mappedChange = change.map(transform)
            mappedChange.applyTo(mutableContent!!)
            notifyListeners(mappedChange)
        }

        mutableContent = source.volatileContentView.map(transform).toMutableSet()
    }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()
    }
}
