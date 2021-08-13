package icesword.frp

class StreamFilter<A>(
    private val source: Stream<A>,
    private val predicate: (A) -> Boolean,
) : SimpleStream<A>(tag = "StreamFilter") {
    private var subscription: Subscription? = null

    override fun onStart() {
        subscription = source.subscribe {
            if (predicate(it)) {
                notifyListeners(it)
            }
        }
    }

    override fun onStop() {
        subscription!!.unsubscribe()
        subscription = null
    }
}
