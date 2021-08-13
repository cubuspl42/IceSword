package icesword.frp

class CellMapTillNext<A, B>(
    source: Cell<A>,
    f: (A, tillNext: Till) -> B,
    tillAbort: Till,
) : SimpleCell<B>(tag = "CellMapTillNext") {
    private var tillNextSink = StreamSink<Unit>()

    private var value: B

    init {
        source.values().reactTill(tillAbort) {
            tillNextSink.send(Unit)

            val newValue = f(
                it,
                tillNextSink.tillNext(tillAbort),
            )

            value = newValue

            notifyListeners(newValue)
        }

        val initialValue = f(
            source.sample(),
            tillNextSink.tillNext(tillAbort),
        )

        value = initialValue
    }


    override fun sample(): B = value
}
