package icesword.frp

interface Cell<out A> : Observable<A> {
    fun sample(): A

    companion object {
        fun <A> constant(a: A): Cell<A> =
            ConstCell(a)

        fun <A, B, C> map2(
            ca: Cell<A>,
            cb: Cell<B>,
            f: (A, B) -> C,
        ): Cell<C> =
            CellMap2(ca, cb, f)

        fun <A> switch(ca: Cell<Cell<A>>): Cell<A> =
            CellSwitch(ca)

        private fun <A> sequence(lca: List<Cell<A>>): Cell<List<A>> =
            CellSequenceList(lca)
//
//        fun <A, B> traverse(lca: List<A>, f: (A) -> Cell<B>): Cell<List<B>> =
//            sequence(lca.map { f(it) })

//        fun <A, B> traverse(sca: Set<A>, f: (A) -> Cell<B>): Cell<Set<B>> =
//            sequence(sca.toList().map { f(it) }).map { it.toSet() }
    }
}

class ConstCell<out A>(
    private val constant: A,
) : Cell<A> {
    override fun sample(): A = constant

    override fun subscribe(handler: (A) -> Unit): Subscription =
        Subscription.noop()
}

fun <A> Cell<A>.values(): Stream<A> =
    Stream.source(this::subscribe)

fun <A, B> Cell<A>.map(f: (A) -> B): Cell<B> =
    CellMap(this, f)

fun <A, B> Cell<A>.switchMap(transform: (A) -> Cell<B>): Cell<B> =
    Cell.switch(map(transform))


fun <A> Cell<A>.reactTill(till: Till, handler: (A) -> Unit) {
    handler(sample())
    values().reactTill(till, handler)
}

fun <A, B> Cell<A>.mapTillNext(
    tillAbort: Till,
    transform: (value: A, tillNext: Till) -> B,
): Cell<B> =
    CellMapTillNext(
        source = this,
        f = transform,
        tillAbort = tillAbort,
    )

fun <A> Cell<A>.reactTillNext(
    tillAbort: Till,
    handler: (value: A, tillNext: Till) -> Unit,
) {
    mapTillNext(
        tillAbort = tillAbort,
        transform = handler,
    )
}

fun <A> Cell<A>.syncTill(target: MutCell<in A>, till: Till) {
    this.reactTill(till, target::set)
}
