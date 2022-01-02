package icesword.frp

class CellDivert<A>(
    private val source: Cell<Stream<A>>,
) : SimpleStream<A>(
    identity = Identity.build(
        tag = "CellDivert",
    ),
) {

    private var _subscriptionOuter: Subscription? = null

    private var _subscriptionInner: Subscription? = null


    override fun onStart(): Unit {
        fun addInnerListener(inner: Stream<A>): Unit {
            _subscriptionInner = inner.subscribe(this::notifyListeners)
        }

        fun reAddInnerListener(inner: Stream<A>): Unit {
            _subscriptionInner?.unsubscribe()
            _subscriptionInner = null

            addInnerListener(inner)
        }

        _subscriptionOuter = source.values().subscribe { inner ->
            reAddInnerListener(inner)
        }

        addInnerListener(source.sample())
    }

    override fun onStop(): Unit {
        _subscriptionInner!!.unsubscribe()
        _subscriptionInner = null

        _subscriptionOuter!!.unsubscribe()
        _subscriptionOuter = null
    }
}
