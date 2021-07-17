package icesword.frp

interface Cell<out A> : Observable<A> {
    fun sample(): A
}

fun <A> Cell<A>.values(): Stream<A> =
    Stream.source(this::subscribe)

fun <A, B> Cell<A>.map(f: (A) -> B): Cell<B> =
    CellMap(this, f)

fun <A> Cell<A>.reactTill(till: Till, handler: (A) -> Unit) {
    handler(sample())
    values().reactTill(till, handler)
}

fun <A> Cell<A>.syncTill(target: MutCell<in A>, till: Till) {
    this.reactTill(till, target::set)
}
