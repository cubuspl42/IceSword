package icesword.frp

import frp.cell.CorrelateCell
import icesword.frp.stream.FollowCell

interface Tilled<out A> {
    companion object {
        fun <A> pure(value: A): Tilled<A> = object : Tilled<A> {
            override fun build(till: Till): A = value
        }
    }

    fun build(till: Till): A
}

interface Stream<out A> : Observable<A> {
    companion object {
        fun <A> source(
            subscribeToSource: (notify: (a: A) -> Unit) -> Subscription,
            tag: String,
        ): Stream<A> = SourceStream(subscribeToSource, tag = tag)

        fun <A> merge(
            streams: Iterable<Stream<A>>,
        ): Stream<A> = StreamMerge(streams)

        fun <A> follow(
            initialValue: Tilled<A>,
            extractNext: (A) -> Stream<Tilled<A>>,
            till: Till,
        ): Cell<A> =
            FollowCell(
                initialValue = initialValue,
                extractNext = extractNext,
                till = till,
            )

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

fun <A> Stream<A>.correlate(sample: () -> A): Cell<A> =
    CorrelateCell(sampleValue = sample, steps = this)

fun <A, B> Stream<A>.map(f: (A) -> B): Stream<B> =
    StreamMap(this, f)

fun <A, B> Stream<A>.mapTo(b: B): Stream<B> =
    this.map { b }

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

fun <A> Stream<A>.reactIndefinitely(handler: (A) -> Unit) {
    subscribe(handler)
}

fun <A> Stream<A>.reactDynamicTill(till: Till, dynamicHandler: Cell<(A) -> Unit>) {
    dynamicHandler.mapTillNext(till) { handler, tillNext ->
        subscribeTill(this, tillNext, handler)
    }
}

typealias Handler<A> = (A) -> Unit

fun <A> Stream<A>.reactDynamicNotNullTill(dynamicHandler: Cell<Handler<A>?>, till: Till) {
    dynamicHandler.mapTillNext(till) { handler, tillNext ->
        if (handler != null) {
            subscribeTill(this, tillNext, handler)
        }
    }
}

fun <A> Stream<A>.syncTill(target: MutCell<in A>, till: Till) {
    this.reactTill(till, target::set)
}
