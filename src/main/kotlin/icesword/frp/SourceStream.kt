package icesword.frp

class SourceStream<A>(
    private val subscribeToSource: (notify: (A) -> Unit) -> Subscription,
) : SimpleStream<A>() {
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
) : SimpleStream<A>() {
    init {
        subscribeTill(source, till, this::notifyListeners)
    }
}

class CellHold<A>(
    steps: Stream<A>,
    initialValue: A,
    till: Till,
) : SimpleCell<A>() {
    private var _currentValue: A = initialValue

    override fun sample(): A = _currentValue

    init {
        subscribeTill(steps, till) {
            _currentValue = it
            notifyListeners(it)
        }
    }
}
