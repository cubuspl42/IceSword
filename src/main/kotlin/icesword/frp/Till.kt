package icesword.frp

interface Till : Observable<Unit> {
    companion object {
        val reached: Till = TillReached()
        val never: Till = TillUnreachable()
    }

    fun wasReached(): Boolean
}

class TillReached : Till {
    override fun wasReached(): Boolean = true

    override fun subscribe(handler: (Unit) -> Unit): Subscription =
        Subscription.noop()
}

class TillUnreachable : Till {
    override fun wasReached(): Boolean = false

    override fun subscribe(handler: (Unit) -> Unit): Subscription =
        Subscription.noop()
}

class TillOr(
    source1: Observable<Any?>,
    source2: Observable<Any?>,
) : SimpleObservable<Unit>(), Till {

    private var _wasReached: Boolean = false

    private fun handle() {
        _wasReached = true

        notifyListeners(Unit)

        subscription1!!.unsubscribe()
        subscription1 = null

        subscription2!!.unsubscribe()
        subscription2 = null

    }

    private var subscription1: Subscription? = source1.subscribe { handle() }

    private var subscription2: Subscription? = source2.subscribe { handle() }

    override fun subscribe(handler: (Unit) -> Unit): Subscription =
        if (!wasReached()) {
            super.subscribe(handler)
        } else {
            Subscription.noop()
        }

    override fun wasReached(): Boolean = _wasReached
}

fun <A> subscribeTill(
    observable: Observable<A>,
    till: Till,
    handle: (a: A) -> Unit,
) {
    if (!till.wasReached()) {
        val subscription: Subscription =
            observable.subscribe(handle)

        till.subscribe {
            subscription.unsubscribe()
        }
    }
}