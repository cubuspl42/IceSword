package icesword.frp

class MutCell<A>(initialValue: A) : SimpleCell<A>(tag = "MutCell") {
    private var _currentValue: A = initialValue

    override fun sample(): A = _currentValue

    fun set(newValue: A) {
        _currentValue = newValue
        notifyListeners(newValue)
    }
}

fun <A> MutCell<A>.update(transform: (A) -> A) {
    set(transform(sample()))
}
