package icesword.frp

interface Stream<A> : Observable<A> {
    companion object {
        fun <A> source(
            subscribeToSource: (notify: (a: A) -> Unit) -> Subscription,
        ): Stream<A> = SourceStream(subscribeToSource)
    }
}

fun <A> Stream<A>.until(till: Till): Stream<A> =
    StreamUntil(this, till)

fun <A> Stream<A>.tillNext(orTill: Till): Till =
    if (orTill.wasReached()) {
        Till.reached
    } else {
        TillOr(this, orTill)
    }

fun <A> Stream<A>.hold(initialValue: A, till: Till): Cell<A> =
    CellHold(this, initialValue, till)

fun <A, B> Stream<A>.map(f: (A) -> B): Stream<B> {
    return StreamMap(this, f)
}

@Suppress("UNCHECKED_CAST")
fun <A, B> Stream<A>.cast(): Stream<B> =
    this.map { it as B }

fun <A> Stream<A>.reactTill(till: Till, handler: (A) -> Unit) {
    subscribeTill(this, till, handler)
}

fun <A> Stream<A>.syncTill(target: MutCell<in A>, till: Till) {
    this.reactTill(till, target::set)
}
