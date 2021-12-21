package icesword.frp

import icesword.frp.dynamic_list.DynamicList

interface Cell<out A> : Behavior<A> {
    override fun sample(): A

    val changes: Stream<ValueChange<A>>

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

        fun <A> divert(ca: Cell<Stream<A>>): Stream<A> =
            CellDivert(ca)

        private fun <A> sequence(lca: List<Cell<A>>): Cell<List<A>> =
            CellSequenceList(lca)

        //
        fun <A, B> traverse(lca: Iterable<A>, f: (A) -> Cell<B>): Cell<List<B>> =
            sequence(lca.map { f(it) })

//        fun <A, B> traverse(sca: Set<A>, f: (A) -> Cell<B>): Cell<Set<B>> =
//            sequence(sca.toList().map { f(it) }).map { it.toSet() }
    }
}

data class ValueChange<out A>(
    val oldValue: A,
    val newValue: A,
)

class ConstCell<out A>(
    private val constant: A,
) : Cell<A> {
    override fun sample(): A = constant

    override val changes: Stream<ValueChange<A>> = Stream.never()
}

fun <A> Cell<A>.values(): Stream<A> =
    this.changes.map { it.newValue }

fun <A, B> Cell<A>.map(f: (A) -> B): Cell<B> =
    CellMap(this, f)

fun <A : Any, B> Cell<A?>.mapNested(f: (A) -> B): Cell<B?> =
    CellMap(this) { if (it != null) f(it) else null }

fun <A, B> Cell<A>.switchMap(transform: (A) -> Cell<B>): Cell<B> =
    Cell.switch(map(transform))

fun <A, B> Cell<A>.diffMap(transform: (A) -> List<B>): DynamicList<B> =
    DynamicList.diff(map(transform))

fun <A : Any, B> Cell<A?>.switchMapNested(transform: (A) -> Cell<B>?): Cell<B?> =
    this.mapNested(transform).switch()

fun <A> Cell<Cell<A>>.switch(): Cell<A> =
    Cell.switch(this)

fun <A> Cell<Stream<A>>.divert(): Stream<A> =
    Cell.divert(this)

fun <A : Any> Cell<Cell<A?>?>.switch(): Cell<A?> =
    Cell.switch(this.map { it ?: Cell.constant(null) })

fun <A, B> Cell<A?>.switchMapNotNull(transform: (A) -> Cell<B>): Cell<B?> =
    this.switchMap {
        if (it != null) transform(it)
        else Cell.constant(null)
    }

fun <A, B> Cell<A>.divertMap(transform: (A) -> Stream<B>): Stream<B> =
    Cell.divert(map(transform))

fun <A> Cell<A>.reactTill(till: Till, handler: (A) -> Unit) {
    handler(sample())
    values().reactTill(till, handler)
}

fun <A, B> Cell<A>.mapTillNext(
    tillFreeze: Till,
    transform: (value: A, tillNext: Till) -> B,
): Cell<B> =
    CellMapTillNext(
        source = this,
        f = transform,
        tillAbort = tillFreeze,
    )

fun <A> Cell<A>.reactTillNext(
    tillAbort: Till,
    handler: (value: A, tillNext: Till) -> Unit,
) {
    mapTillNext(
        tillFreeze = tillAbort,
        transform = handler,
    )
}

fun <A> Cell<A>.syncTill(target: MutCell<in A>, till: Till) {
    this.reactTill(till, target::set)
}

val <A> Cell<A>.asView: DynamicView<Behavior<A>>
    get() = DynamicView(
        updates = this.values().units(),
        view = this,
    )
