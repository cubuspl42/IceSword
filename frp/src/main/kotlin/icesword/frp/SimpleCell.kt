package icesword.frp

abstract class SimpleCell<A>(
    identity: Identity,
) : SimpleObservable<ValueChange<A>>(
    identity = identity
), Cell<A> {
    override val changes: Stream<ValueChange<A>>
        get() = Stream.source(this::subscribe, tag = "Cell.changes")

    override fun toString(): String = "SimpleCell $tag #$id)"
}
