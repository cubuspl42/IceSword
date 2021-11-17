package icesword.frp

abstract class SimpleCell<A>(
    tag: String,
) : SimpleObservable<ValueChange<A>>(
    tag = tag
), Cell<A> {
    override val changes: Stream<ValueChange<A>>
        get() = Stream.source(this::subscribe, tag = "Cell.changes")

    override fun toString(): String = "SimpleCell $tag #$id)"
}
