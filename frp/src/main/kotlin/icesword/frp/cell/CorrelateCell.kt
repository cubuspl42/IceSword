package icesword.frp.cell

import icesword.frp.SimpleCell
import icesword.frp.Stream
import icesword.frp.Subscription
import icesword.frp.ValueChange

class CorrelateCell<A>(
    private val sampleValue: () -> A,
    private val steps: Stream<A>,
) : SimpleCell<A>(
    identity = Identity.build(tag = "CorrelateCell"),
) {
    private var _currentValue: A = sampleValue()

    private var subscription: Subscription? = null

    override fun sample(): A = _currentValue ?: sampleValue()

    override fun onStart() {
        subscription = steps.subscribe {
            val change = ValueChange(
                oldValue = _currentValue,
                newValue = it,
            )

            _currentValue = it

            notifyListeners(change)
        }

        _currentValue = sampleValue()
    }

    override fun onStop() {
        subscription!!.unsubscribe()
        subscription = null
    }
}
