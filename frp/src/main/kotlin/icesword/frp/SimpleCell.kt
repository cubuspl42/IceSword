package icesword.frp

abstract class SimpleCell<A>(
    tag: String,
) : SimpleObservable<A>(
    tag = tag
), Cell<A> {
    override fun toString(): String = "SimpleCell $tag #$id)"
}
