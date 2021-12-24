package icesword.frp

interface Behavior<out A> {
    fun sample(): A
}

class DynamicView<A>(
    val updates: Stream<Unit>,
    val view: A,
) {
    companion object {
        fun <A> static(
            view: A,
        ): DynamicView<A> = DynamicView(
            updates = Stream.never(),
            view = view,
        )

        fun <A, B, C> map2(
            da: DynamicView<A>,
            db: DynamicView<B>,
            f: (va: A, vb: B) -> C,
        ): DynamicView<C> =
            DynamicView(
                updates = Stream.merge(
                    listOf(
                        da.updates,
                        db.updates,
                    ),
                ),
                view = f(
                    da.view,
                    db.view,
                ),
            )
    }
}

fun <A, R> DynamicView<A>.map(transform: (A) -> R): DynamicView<R> =
    DynamicView(
        updates = this.updates,
        view = transform(this.view),
    )
