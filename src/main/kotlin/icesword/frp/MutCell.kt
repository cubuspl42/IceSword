package icesword.frp

class MutCell<A>(initialValue: A) : SimpleCell<A>() {
    private var _currentValue: A = initialValue

    override fun sample(): A = _currentValue

    fun set(newValue: A) {
        _currentValue = newValue
        notifyListeners(newValue)
    }
}
