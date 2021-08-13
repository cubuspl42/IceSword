package icesword.frp

abstract class SimpleStream<A>(tag: String) : SimpleObservable<A>(tag = tag), Stream<A>