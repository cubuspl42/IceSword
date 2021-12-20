package icesword.frp.dynamic_set

import icesword.frp.*

class DynamicSetUnion<A>(
    private val sets: DynamicSet<DynamicSet<A>>,
) : SimpleDynamicSet<A>(tag = "DynamicSetUnion") {
//    private var mutableContent: MutableSet<A>? = null

    private var linksMap: MutableMap<A, MutableSet<DynamicSet<A>>>? = null

    override val volatileContentView: Set<A>
        get() = linksMap!!.keys

//    override val volatileContentView: Set<A>
//        get() = mutableContent!!

    private var subscriptionOuter: Subscription? = null

    private var subscriptionMap: MutableMap<DynamicSet<A>, Subscription>? = null


//    private var subscription2: Subscription? = null

//    override val content: Cell<Set<A>>
//        get() = RawCell(
//            { mutableContent!!.toSet() },
//            changes.map { mutableContent!!.toSet() },
//        )

    override fun onStart() {
        // Returns: true if value was actually added
        fun processAddedValue(innerSet: DynamicSet<A>, addedValue: A): Boolean {
            val links = linksMap!!.getOrPut(addedValue) { mutableSetOf() }
            return links.isEmpty().also { links.add(innerSet) }
        }

        fun processAddedValues(innerSet: DynamicSet<A>, addedValues: Set<A>): List<A> =
            addedValues.filter { addedValue -> processAddedValue(innerSet, addedValue) }

        // Returns: true if value was actually removed
        fun processRemovedValue(innerSet: DynamicSet<A>, removedValue: A): Boolean {
            val linksMap = this.linksMap!!
            val links = linksMap[removedValue]!!

            if (links.isEmpty()) {
                throw IllegalStateException("links.isEmpty()")
            }


            val wasThere = links.remove(innerSet)

            if (!wasThere) {
                throw IllegalStateException("!wasThere")
            }

            if (links.isEmpty()) {
                linksMap.remove(removedValue)
                return true
            } else {
                return false
            }
        }

        fun processRemovedValues(innerSet: DynamicSet<A>, removedValues: Set<A>): List<A> =
            removedValues.filter { removedValue -> processRemovedValue(innerSet, removedValue) }

        fun subscribeToSet(innerSet: DynamicSet<A>) {
            val subscription = innerSet.changes.subscribe { change ->
                val added = processAddedValues(innerSet, change.added).toSet()
                val removed = processRemovedValues(innerSet, change.removed).toSet()

                val outChange = SetChange(
                    added = added,
                    removed = removed,
                )

                notifyListeners(outChange)
            }

            if (subscriptionMap!!.put(innerSet, subscription) != null) {
                throw IllegalStateException("subscriptionMap!!.put(innerSet, subscription ) != null")
            }
        }

        fun unsubscribeFromSet(innerSet: DynamicSet<A>) {
            val subscription = subscriptionMap!![innerSet]!!

            subscription.unsubscribe()

            subscriptionMap!!.remove(innerSet)
        }

        fun processAddedSet(innerSet: DynamicSet<A>): List<A> {
            subscribeToSet(innerSet)

            return processAddedValues(innerSet, innerSet.volatileContentView)
        }

        // Returns: values that actually were added (not previously present in the union)
        fun processAddedSets(innerSets: Set<DynamicSet<A>>): Set<A> =
            innerSets.flatMap { processAddedSet(it) }.toSet()


        fun processRemovedSet(innerSet: DynamicSet<A>): List<A> {
            val removed = processRemovedValues(innerSet, innerSet.volatileContentView)

            unsubscribeFromSet(innerSet)

            return removed
        }

        // Returns: values that actually were removed (not present in other union sets)
        fun processRemovedSets(innerSets: Set<DynamicSet<A>>): Set<A> =
            innerSets.flatMap { processRemovedSet(it) }.toSet()

        subscriptionOuter = sets.changes.subscribe { outerChange ->
            val added = processAddedSets(outerChange.added)
            val removed = processRemovedSets(outerChange.removed)

            val outChange = SetChange(
                added = added,
                removed = removed,
            )

            notifyListeners(outChange)
        }

        this.linksMap = mutableMapOf()

        subscriptionMap = mutableMapOf()

        processAddedSets(sets.volatileContentView)
    }

//    private fun subscribeToSet(innerSet: DynamicSet<A>) {
//        val subscription = innerSet.changes.subscribe { change ->
//            val mappedChange = SetChange(
//                added = change.added.filter { !mutableContent!!.contains(it) }.toSet(),
//                removed = change.removed.filter { removedValue ->
//                    if (mutableContent!!.contains(removedValue)) {
//                        sets.volatileContentView.none { it != innerSet && it.containsNow(removedValue) }
//                    } else false
//                }.toSet()
//            )
//
//            mappedChange.applyTo(mutableContent!!)
//
//            notifyListeners(mappedChange)
//        }
//
//        if (subscriptionMap!!.put(innerSet, subscription) != null) {
//            throw IllegalStateException("subscriptionMap!!.put(innerSet, subscription ) != null")
//        }
//    }

    private fun unsubscribeFromSet(innerSet: DynamicSet<A>) {
        val subscription = subscriptionMap!![innerSet]!!

        subscription.unsubscribe()

        subscriptionMap!!.remove(innerSet)

//        mutableContent.remove(key)
    }

    override fun onStop() {
        linksMap = null

        subscriptionOuter!!.unsubscribe()
        subscriptionOuter = null

        subscriptionMap!!.forEach { (_, sub) ->
            sub.unsubscribe()
        }
        subscriptionMap = null
    }


}


