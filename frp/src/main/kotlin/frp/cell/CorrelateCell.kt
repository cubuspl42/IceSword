package frp.cell

import icesword.frp.SimpleCell
import icesword.frp.Stream
import icesword.frp.Subscription

class CorrelateCell<A>(
    private val sampleValue: () -> A,
    private val steps: Stream<A>,
) : SimpleCell<A>(tag = "CorrelateCell") {
    private var _currentValue: A? = null

    private var subscription: Subscription? = null

    override fun sample(): A = _currentValue ?: sampleValue()

    override fun onStart() {
        subscription = steps.subscribe {
            _currentValue = it
            notifyListeners(it)
        }
    }

    override fun onStop() {
        _currentValue = null

        subscription!!.unsubscribe()
        subscription = null
    }
}
