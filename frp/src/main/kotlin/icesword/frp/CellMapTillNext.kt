package icesword.frp

class CellMapTillNext<A, B>(
    source: Cell<A>,
    f: (A, tillNext: Till) -> B,
    tillAbort: Till,
) : SimpleCell<B>(tag = "CellMapTillNext") {
    private var tillNextSink = StreamSink<Unit>()

    private var value: B = f(
        source.sample(),
        tillNextSink.tillNext(tillAbort),
    )

    init {
        source.values().reactTill(tillAbort) {
            tillNextSink.send(Unit)

            val newValue = f(
                it,
                tillNextSink.tillNext(tillAbort),
            )

            val change = ValueChange(
                oldValue = value,
                newValue = newValue,
            )

            value = newValue

            notifyListeners(change)
        }
    }


    override fun sample(): B = value
}
