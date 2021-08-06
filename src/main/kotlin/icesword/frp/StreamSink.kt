package icesword.frp

class StreamSink<A> : SimpleStream<A>() {
    fun send(a: A) {
        notifyListeners(a)
    }
}
