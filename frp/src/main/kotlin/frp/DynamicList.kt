package icesword.frp

import icesword.frp.dynamic_map.DynamicSetAssociateWith
import icesword.frp.dynamic_set.*

interface DynamicList<out A> {
}

fun <A, R> DynamicList<A>.map(transform: (A) -> R): DynamicList<R> =
    TODO()

val <A> DynamicList<A>.contentView: Cell<List<A>>
    get() = TODO()
