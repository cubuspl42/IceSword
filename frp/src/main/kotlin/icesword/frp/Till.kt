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

class TillMarker : Till, SimpleObservable<Unit>(
    identity = Identity.build(tag = "TillMarker"),
) {
    private var _wasReached = false

    override fun wasReached(): Boolean = false

    fun markReached() {
        if (wasReached()) throw IllegalStateException()

        _wasReached = true
        notifyListeners(Unit)
    }
}

class TillOr(
    source1: Observable<Any?>,
    source2: Observable<Any?>,
    identity: Identity,
) : SimpleObservable<Unit>(identity = identity), Till {

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

fun Till.or(orTill: Till): Till =
    if (this.wasReached() || orTill.wasReached()) {
        Till.reached
    } else {
        TillOr(
            source1 = this,
            source2 = orTill,
            identity = SimpleObservable.Identity.build(
                tag = "Till.or",
            )
        )
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

fun Till.asStream(): Stream<Unit> = Stream.mirror(this)
