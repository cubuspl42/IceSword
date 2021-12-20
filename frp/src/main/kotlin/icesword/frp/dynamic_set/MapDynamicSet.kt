package icesword.frp.dynamic_set

import icesword.base.mapSome
import icesword.base.none
import icesword.base.some
import icesword.frp.*

class MapDynamicSet<A, B>(
    private val source: DynamicSet<A>,
    private val transform: (A) -> B,
    tag: String,
) : SimpleDynamicSet<B>(tag = tag) {
    private var mutableContent: MutableMap<B, Int>? = null

    override val volatileContentView: Set<B>
        get() = mutableContent!!.keys

    private var subscription: Subscription? = null

    override val changes: Stream<SetChange<B>>
        get() = Stream.source(this::subscribe, tag = "MapDynamicSet.changes")

    override val content: Cell<Set<B>>
        get() = RawCell(
            { mutableContent!!.keys.toSet() },
            changes.map { mutableContent!!.keys.toSet() },
        )

    override fun onStart() {
        subscription = source.changes.subscribe { change ->
            val mutableContent = this.mutableContent!!

            val added: Set<B> = change.added.mapSome {
                val b = transform(it)
                val oldCount = mutableContent[b]

                if (oldCount == null) {
                    mutableContent[b] = 1
                    some(b)
                } else {
                    mutableContent[b] = oldCount + 1
                    none()
                }
            }.toSet()

            val removed: Set<B> = change.removed.mapSome {
                val b = transform(it)
                val oldCount = mutableContent[b]

                if (oldCount == null || oldCount < 1)
                    throw IllegalStateException("Attempted to remove $b, but it wasn't present? [oldCount = $oldCount] (id = $id)")

                if (oldCount == 1) {
                    mutableContent.remove(b)
                    some(b)
                } else {
                    mutableContent[b] = oldCount - 1
                    none()
                }
            }.toSet()

            val outChange = SetChange(
                added = added,
                removed = removed,
            )

            if (!outChange.isEmpty()) {
                notifyListeners(
                    outChange
                )
            }
        }

        val initialContent = source.volatileContentView
            .groupingBy(transform).eachCount().toMutableMap()

        mutableContent = initialContent
    }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()
    }
}
