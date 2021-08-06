package icesword.frp

class StreamNever<A> : Stream<A> {
    override fun subscribe(handler: (A) -> Unit): Subscription = Subscription.noop()
}
