package icesword.frp

class StreamLoop<A> {
    private val _stream = MutCell(Stream.never<A>())

    private var isClosed = false

    val asStream: Stream<A> = _stream.divert()

    fun close(stream: Stream<A>) {
        if (isClosed) {
            throw IllegalStateException()
        }

        _stream.set(stream)

        isClosed = true
    }
}
