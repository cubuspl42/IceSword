package icesword.frp

interface Stream<out A> : Observable<A> {
    companion object {
        fun <A> source(
            subscribeToSource: (notify: (a: A) -> Unit) -> Subscription,
        ): Stream<A> = SourceStream(subscribeToSource)

        fun <A> merge(
            streams: Iterable<Stream<A>>,
        ): Stream<A> = StreamMerge(streams)

        fun <A> never(): Stream<A> = StreamNever()
    }
}

fun <A> Stream<A>.until(till: Till): Stream<A> =
    StreamUntil(this, till)

fun <A> Stream<A>.tillNext(orTill: Till): Till =
    if (orTill.wasReached()) {
        Till.reached
    } else {
        TillOr(this, orTill, tag = "tillNext")
    }

fun <A> Stream<A>.hold(initialValue: A, till: Till): Cell<A> =
    CellHold(this, initialValue, till)

fun <A, B> Stream<A>.map(f: (A) -> B): Stream<B> =
    StreamMap(this, f)

fun <A> Stream<A>.units(): Stream<Unit> =
    this.map { }

fun <A> Stream<A>.filter(predicate: (A) -> Boolean): Stream<A> =
    StreamFilter(this, predicate)

fun <A> Stream<A>.mergeWith(other: Stream<A>): Stream<A> =
    StreamMerge2(this, other)

@Suppress("UNCHECKED_CAST")
fun <A, B> Stream<A>.cast(): Stream<B> =
    this.map { it as B }

fun <A> Stream<A>.reactTill(till: Till, handler: (A) -> Unit) {
    subscribeTill(this, till, handler)
}

fun <A> Stream<A>.syncTill(target: MutCell<in A>, till: Till) {
    this.reactTill(till, target::set)
}
