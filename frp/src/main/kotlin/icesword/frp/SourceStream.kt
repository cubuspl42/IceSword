package icesword.frp

class SourceStream<A>(
    private val subscribeToSource: (notify: (A) -> Unit) -> Subscription,
    tag: String,
) : SimpleStream<A>(
    identity = Identity.build(tag = "SourceStream"),
) {
    private var subscription: Subscription? = null

    override fun onStart() {
        subscription = subscribeToSource(this::notifyListeners)
    }

    override fun onStop() {
        subscription!!.unsubscribe()
        subscription = null
    }
}

class StreamUntil<A>(
    source: Stream<A>,
    till: Till,
) : SimpleStream<A>(identity = Identity.build("StreamUntil")) {
    init {
        subscribeTill(source, till, this::notifyListeners)
    }
}

class CellHold<A>(
    steps: Stream<A>,
    initialValue: A,
    till: Till,
) : SimpleCell<A>(
    identity = Identity.build("CellHold"),
) {
    private var _currentValue: A = initialValue

    override fun sample(): A = _currentValue

    init {
        subscribeTill(steps, till) {
            if (it != _currentValue) {
                val change = ValueChange(
                    oldValue = _currentValue,
                    newValue = it,
                )

                _currentValue = it

                notifyListeners(change)
            }
        }
    }
}
