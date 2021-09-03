package icesword.frp

class StreamMerge2<A>(
    private val source1: Stream<A>,
    private val source2: Stream<A>,
) : SimpleStream<A>(tag = "StreamMerge2") {
    private var subscription1: Subscription? = null

    private var subscription2: Subscription? = null

    override fun onStart() {
        subscription1 = source1.subscribe(this::notifyListeners)
        subscription2 = source2.subscribe(this::notifyListeners)
    }

    override fun onStop() {
        subscription1!!.unsubscribe()
        subscription1 = null

        subscription2!!.unsubscribe()
        subscription2 = null
    }
}
