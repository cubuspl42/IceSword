package icesword.frp

class CellMap<A, B>(
    private val source: Cell<A>,
    private val f: (A) -> B,
) : CachingCell<B>(tag = "CellMap") {
    private var subscription: Subscription? = null

    override fun sampleUncached(): B =
        f(source.sample())

    override fun onStartUncached() {
        subscription = source.subscribe {
            cacheAndNotifyListeners(f(it))
        }
    }

    override fun onStopUncached() {
        subscription!!.unsubscribe()
        subscription = null
    }
}