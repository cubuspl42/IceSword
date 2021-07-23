package icesword.frp

class CellSwitch<A>(
    private val source: Cell<Cell<A>>,
) : SimpleCell<A>() {

    private var _subscriptionOuter: Subscription? = null

    private var _subscriptionInner: Subscription? = null


    override fun onStart(): Unit {
        fun addInnerListener(inner: Cell<A>): Unit {
            _subscriptionInner = inner.subscribe(this::notifyListeners)
        }

        fun reAddInnerListener(inner: Cell<A>): Unit {
            _subscriptionInner?.unsubscribe()
            _subscriptionInner = null

            addInnerListener(inner)
        }

        _subscriptionOuter = source.subscribe { inner ->
            reAddInnerListener(inner)

            notifyListeners(inner.sample())
        }

        addInnerListener(source.sample())
    }

    override fun onStop(): Unit {
        _subscriptionInner!!.unsubscribe()
        _subscriptionInner = null

        _subscriptionOuter!!.unsubscribe()
        _subscriptionOuter = null
    }

    override fun sample(): A = source.sample().sample()
}
