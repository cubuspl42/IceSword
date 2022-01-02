package icesword.frp

class CellMap2<A, B, C>(
    private val ca: Cell<A>,
    private val cb: Cell<B>,
    private val f: (A, B) -> C,
) : CachingCell<C>(
    identity = Identity.build(tag = "CellMap2"),
) {
    private var subscriptionA: Subscription? = null
    private var subscriptionB: Subscription? = null

    override fun sampleUncached(): C = f(ca.sample(), cb.sample())

    override fun onStartUncached(): Unit {
        subscriptionA = ca.values().subscribe { a ->
            cacheAndNotifyListeners(f(a, cb.sample()))
        }

        subscriptionB = cb.values().subscribe { b ->
            cacheAndNotifyListeners(f(ca.sample(), b))
        }
    }

    override fun onStopUncached(): Unit {
        subscriptionB!!.unsubscribe()
        subscriptionB = null

        subscriptionA!!.unsubscribe()
        subscriptionA = null
    }
}

class CellMap3<A, B, C, D>(
    private val ca: Cell<A>,
    private val cb: Cell<B>,
    private val cc: Cell<C>,
    private val f: (A, B, C) -> D,
) : CachingCell<D>(
    identity = Identity.build(tag = "CellMap3"),
) {
    private var subscriptionA: Subscription? = null
    private var subscriptionB: Subscription? = null
    private var subscriptionC: Subscription? = null

    override fun sampleUncached(): D = f(
        ca.sample(),
        cb.sample(),
        cc.sample(),
    )

    override fun onStartUncached(): Unit {
        subscriptionA = ca.values().subscribe {
            cacheAndNotifyListeners(f(it, cb.sample(), cc.sample()))
        }

        subscriptionB = cb.values().subscribe {
            cacheAndNotifyListeners(f(ca.sample(), it, cc.sample()))
        }

        subscriptionC = cc.values().subscribe {
            cacheAndNotifyListeners(f(ca.sample(), cb.sample(), it))
        }

    }

    override fun onStopUncached() {
        subscriptionC!!.unsubscribe()
        subscriptionC = null

        subscriptionB!!.unsubscribe()
        subscriptionB = null

        subscriptionA!!.unsubscribe()
        subscriptionA = null
    }
}


class CellMap4<A, B, C, D, E>(
    private val ca: Cell<A>,
    private val cb: Cell<B>,
    private val cc: Cell<C>,
    private val cd: Cell<D>,
    private val f: (A, B, C, D) -> E,
) : CachingCell<E>(
    identity = Identity.build(tag = "CellMap4"),
) {
    private var subscriptionA: Subscription? = null
    private var subscriptionB: Subscription? = null
    private var subscriptionC: Subscription? = null
    private var subscriptionD: Subscription? = null

    override fun sampleUncached(): E = f(
        ca.sample(),
        cb.sample(),
        cc.sample(),
        cd.sample(),
    )

    override fun onStartUncached(): Unit {
        subscriptionA = ca.values().subscribe {
            cacheAndNotifyListeners(f(it, cb.sample(), cc.sample(), cd.sample()))
        }

        subscriptionB = cb.values().subscribe {
            cacheAndNotifyListeners(f(ca.sample(), it, cc.sample(), cd.sample()))
        }

        subscriptionC = cc.values().subscribe {
            cacheAndNotifyListeners(f(ca.sample(), cb.sample(), it, cd.sample()))
        }

        subscriptionD = cd.values().subscribe {
            cacheAndNotifyListeners(f(ca.sample(), cb.sample(), cc.sample(), it))
        }
    }

    override fun onStopUncached() {
        subscriptionD!!.unsubscribe()
        subscriptionD = null

        subscriptionC!!.unsubscribe()
        subscriptionC = null

        subscriptionB!!.unsubscribe()
        subscriptionB = null

        subscriptionA!!.unsubscribe()
        subscriptionA = null
    }
}
