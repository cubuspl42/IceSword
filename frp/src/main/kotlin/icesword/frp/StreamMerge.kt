package icesword.frp

class StreamMerge<A>(
    private val sources: Iterable<Stream<A>>,
) : SimpleStream<A>(
    identity = Identity.build(tag = "StreamMerge"),
) {
    private var subscriptions: List<Subscription> = emptyList()

    override fun onStart() {
        subscriptions = sources.map { it.subscribe(this::notifyListeners) }
    }

    override fun onStop() {
        subscriptions.forEach { it.unsubscribe() }
        subscriptions = emptyList()
    }
}
