package icesword.frp.dynamic_set

import icesword.frp.*

class FuseMapDynamicSet<A, B>(
    private val source: DynamicSet<A>,
    // Note: this needs to be pure!
    private val extract: (A) -> Cell<B>,
) : SimpleDynamicSet<B>(tag = "MapDynamicSet") {
    // Value -> reference count
    private var linksMap: MutableMap<B, MutableSet<A>>? = null

    private var subscriptionMap: MutableMap<A, Subscription>? = null

    override val volatileContentView: Set<B>
        get() = linksMap!!.keys

    private var subscription: Subscription? = null

    override val changes: Stream<SetChange<B>>
        get() = Stream.source(this::subscribe, tag = "MapDynamicSet.changes")

    override val content: Cell<Set<B>>
        get() = RawCell(
            { volatileContentView.toSet() },
            changes.map { volatileContentView.toSet() },
        )

    override fun onStart() {
        subscription = source.changes.subscribe { change ->
            val added: Set<B> = change.added.mapNotNull { a: A ->
                val cell: Cell<B> = extract(a)
                val b = cell.sample()

                subscribeToCell(a, cell)

                addLink(b, a)
            }.toSet()

            val removed = change.removed.mapNotNull { a: A ->
                val cell: Cell<B> = extract(a)
                val b = cell.sample()

                val sub = subscriptionMap!!.remove(a)!!
                sub.unsubscribe()

                removeLink(b, a)
            }.toSet()

            val outChange = SetChange(
                added = added,
                removed = removed,
            )

            if (!outChange.isEmpty()) {
                notifyListeners(
                    outChange,
                )
            }
        }

        linksMap = source.volatileContentView
            .groupBy { extract(it).sample() }
            .mapValues { (_, list) -> list.toMutableSet() }
            .toMutableMap()

        subscriptionMap = mutableMapOf()

        source.volatileContentView.forEach { subscribeToCell(it, extract(it)) }
    }

    // Returns `b` if first link was removed, null otherwise
    private fun addLink(b: B, a: A): B? {
        val links: MutableSet<A> = linksMap!!.getOrPut(b) { mutableSetOf() }

        val wasAdded = links.add(a)
        if (!wasAdded) throw IllegalStateException()

        return b.takeIf { links.size == 1 }
    }

    // Returns `b` if last link was removed, null otherwise
    private fun removeLink(b: B, a: A): B? {
        val linksMap = this.linksMap!!
        val links: MutableSet<A> = linksMap[b]!!

        val wasRemoved = links.remove(a)
        if (!wasRemoved) throw IllegalStateException()

        return if (links.isEmpty()) {
            linksMap.remove(b)
            b
        } else null
    }

    private fun subscribeToCell(key: A, cell: Cell<B>) {
        debugLog { "$name: subscribe to cell @ key: $key" }

        val subscription = cell.subscribe { newValue: B ->
            val linksMap = this.linksMap!!

            val oldValue = linksMap.firstNotNullOf { (value, links) ->
                value.takeIf { links.contains(key) }
            }

            val removed: B? = removeLink(oldValue, key)
            val added: B? = addLink(newValue, key)

            val outChange = SetChange(
                added = added?.let(::setOf) ?: emptySet(),
                removed = removed?.let(::setOf) ?: emptySet(),
            )

            if (!outChange.isEmpty()) {
                notifyListeners(
                    outChange,
                )
            }
        }

        if (subscriptionMap!!.put(key, subscription) != null) {
            throw IllegalStateException()
        }
    }

    override fun onStop() {
        linksMap = null

        subscriptionMap!!.values.forEach { it.unsubscribe() }

        subscription!!.unsubscribe()
    }
}
