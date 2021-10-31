package icesword.frp

import icesword.frp.dynamic_map.DynamicSetAssociateWith
import icesword.frp.dynamic_set.*

interface DynamicList<out A> {
    companion object {
//        fun <A> blend(dynamicList: DynamicList<DynamicView<A>>): DynamicView<List<A>> =
//            TODO()
    }
}

//fun <A, R> DynamicList<A>.map(transform: (A) -> R): DynamicList<R> =
//    TODO()

//fun <A, R> DynamicList<A>.blendMap(transform: (A) -> DynamicView<R>): DynamicView<List<R>> =
//    DynamicList.blend(this.map(transform))
//
//val <A> DynamicList<A>.contentView: Cell<List<A>>
//    get() = TODO()
//
//val <A> DynamicList<A>.contentDynamicView: DynamicView<List<A>>
//    get() = TODO()
