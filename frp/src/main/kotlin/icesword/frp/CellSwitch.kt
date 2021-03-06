package icesword.frp

class CellSwitch<A>(
    private val source: Cell<Cell<A>>,
) : CachingCell<A>(
    identity = Identity.build(tag = "CellSwitch"),
) {

    private var _subscriptionOuter: Subscription? = null

    private var _subscriptionInner: Subscription? = null


    override fun onStartUncached(): Unit {
        fun addInnerListener(inner: Cell<A>): Unit {
            _subscriptionInner = inner.values().subscribe(this::cacheAndNotifyListeners)
        }

        fun reAddInnerListener(inner: Cell<A>): Unit {
            _subscriptionInner?.unsubscribe()
            _subscriptionInner = null

            addInnerListener(inner)
        }

        _subscriptionOuter = source.values().subscribe { inner ->
            reAddInnerListener(inner)

            cacheAndNotifyListeners(inner.sample())
        }

        addInnerListener(source.sample())
    }

    override fun onStopUncached(): Unit {
        _subscriptionInner!!.unsubscribe()
        _subscriptionInner = null

        _subscriptionOuter!!.unsubscribe()
        _subscriptionOuter = null
    }

    override fun sampleUncached(): A = source.sample().sample()
}
