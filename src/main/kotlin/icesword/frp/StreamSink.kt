package icesword.frp

class StreamSink<A> : SimpleStream<A>(tag = "StreamSink") {
    fun send(a: A) {
        notifyListeners(a)
    }
}
