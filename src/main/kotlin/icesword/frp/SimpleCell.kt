package icesword.frp

abstract class SimpleCell<A> : SimpleObservable<A>(), Cell<A> {
    override fun subscribe(handler: (A) -> Unit): Subscription {
        handler(sample())
        return super.subscribe(handler)
    }
}
