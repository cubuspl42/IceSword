package icesword.frp

class StreamSink<A> : SimpleStream<A>(tag = "StreamSink") {
    fun send(a: A) {
        notifyListeners(a)
    }

    operator fun invoke(a: A) {
        send(a)
    }
}
