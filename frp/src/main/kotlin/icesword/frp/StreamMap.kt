package icesword.frp

class StreamMap<A, B>(
    private val source: Stream<A>,
    private val f: (A) -> B,
) : SimpleStream<B>(
    identity = SimpleObservable.Identity.build(tag = "StreamMap"),
) {
    private var subscription: Subscription? = null

    override fun onStart() {
        subscription = source.subscribe {
            notifyListeners(f(it))
        }
    }

    override fun onStop() {
        subscription!!.unsubscribe()
        subscription = null
    }
}
