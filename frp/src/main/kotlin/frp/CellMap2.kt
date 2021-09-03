package icesword.frp

class CellMap2<A, B, C>(
    private val ca: Cell<A>,
    private val cb: Cell<B>,
    private val f: (A, B) -> C
) : CachingCell<C>(tag = "CellMap2") {
    private var subscriptionA: Subscription? = null
    private var subscriptionB: Subscription? = null

    override fun sampleUncached(): C = f(ca.sample(), cb.sample())

    override fun onStartUncached(): Unit {
        subscriptionA = ca.subscribe { a ->
            cacheAndNotifyListeners(f(a, cb.sample()))
        }

        subscriptionB = cb.subscribe { b ->
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
