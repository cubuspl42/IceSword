package icesword.frp.dynamic_set

import icesword.frp.*

class DynamicSetUnion2_<A>(
    private val source1: DynamicSet<A>,
    private val source2: DynamicSet<A>,
) : SimpleDynamicSet<A>(tag = "DynamicSetUnion2") {

    private var mutableContent: MutableSet<A>? = null

    override val volatileContentView: Set<A>
        get() = mutableContent!!

//    override val volatileContentView: Set<A>
//        get() = mutableContent!!

    private var subscription1: Subscription? = null

    private var subscription2: Subscription? = null

//    override val content: Cell<Set<A>>
//        get() = RawCell(
//            { mutableContent!!.toSet() },
//            changes.map { mutableContent!!.toSet() },
//        )

    override val content: Cell<Set<A>>
        get() = RawCell(
            { mutableContent!! },
            changes.map { mutableContent!! },
        )

    override fun onStart() {
        subscription1 = source1.changes.subscribe { change ->
            val unionChange = SetChange(
                added = change.added.filter { key -> !source2.containsNow(key) }.toSet(),
                removed = change.removed.filter { key -> !source2.containsNow(key) }.toSet(),
            )

            unionChange.applyTo(mutableContent!!)

            notifyListeners(unionChange)
        }

        subscription2 = source2.changes.subscribe { change ->
            val unionChange = SetChange(
                added = change.added.filter { key -> !source1.containsNow(key) }.toSet(),
                removed = change.removed.filter { key -> !source1.containsNow(key) }.toSet(),
            )

            unionChange.applyTo(mutableContent!!)

            notifyListeners(unionChange)
        }

        mutableContent = (source1.volatileContentView + source2.volatileContentView).toMutableSet()
    }

    override fun onStop() {
        mutableContent = null

        subscription2!!.unsubscribe()
        subscription2 = null

        subscription1!!.unsubscribe()
        subscription1 = null
    }


}


