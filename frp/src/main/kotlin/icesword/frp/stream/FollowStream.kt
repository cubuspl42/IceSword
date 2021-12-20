package icesword.frp.stream

import icesword.frp.SimpleCell
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.TillMarker
import icesword.frp.Tilled
import icesword.frp.ValueChange
import icesword.frp.or
import icesword.frp.subscribeTill

class FollowTillNextCell<A>(
    initialValue: Tilled<A>,
    private val extractNext: (A) -> Stream<Tilled<A>>,
    private val till: Till,
) : SimpleCell<A>(tag = "FollowCell") {
    private var _currentValue: A = buildValueTillNext(initialValue)

    override fun sample(): A = _currentValue

    private fun buildValueTillNext(tilled: Tilled<A>): A {
        val tillNext = TillMarker()
        val till = tillNext.or(till)

        val value = tilled.build(till)
        val nextTilled: Stream<Tilled<A>> = extractNext(value)

        subscribeTill(nextTilled, till) {
            tillNext.markReached()

            val nextValue = buildValueTillNext(it)

            val change = ValueChange(
                oldValue = _currentValue,
                newValue = nextValue,
            )

            _currentValue = nextValue

            notifyListeners(change)
        }

        return value
    }
}
