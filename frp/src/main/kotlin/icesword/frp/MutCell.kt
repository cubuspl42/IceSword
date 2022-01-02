package icesword.frp

class MutCell<A>(initialValue: A) : SimpleCell<A>(
    identity = Identity.build(tag = "MutCell"),
) {
    private var _currentValue: A = initialValue

    override fun sample(): A = _currentValue

    fun set(newValue: A) {
        val change = ValueChange(
            oldValue = _currentValue,
            newValue = newValue,
        )

        _currentValue = newValue

        notifyListeners(change)
    }
}

fun <A> MutCell<A>.update(transform: (A) -> A) {
    set(transform(sample()))
}
