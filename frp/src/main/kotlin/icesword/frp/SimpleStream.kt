package icesword.frp

abstract class SimpleStream<A>(identity: Identity) : SimpleObservable<A>(identity = identity), Stream<A>