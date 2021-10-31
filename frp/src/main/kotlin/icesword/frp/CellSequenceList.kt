package icesword.frp

class CellSequenceList<A>(
    private val sources: List<Cell<A>>,
) : CachingCell<List<A>>(tag = "CellSequenceList") {
    private var subscriptions: List<Subscription> = emptyList()

    override fun onStartUncached() {
        subscriptions = sources.map {
            it.subscribe {
                cacheAndNotifyListeners(sampleUncached())
            }
        }
    }

    override fun onStopUncached() {
        subscriptions.forEach { it.unsubscribe() }
        subscriptions = emptyList()
    }


    override fun sampleUncached(): List<A> = sources.map { it.sample() }
}
