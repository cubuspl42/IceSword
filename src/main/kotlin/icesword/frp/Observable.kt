package icesword.frp

interface Observable<out A> {
     fun subscribe(handler: (A) -> Unit): Subscription
}