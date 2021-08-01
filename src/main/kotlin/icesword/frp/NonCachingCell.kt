package icesword.frp

abstract class NonCachingCell<A> : SimpleCell<A>() {
    final override fun sample(): A =
        sampleUncached()

    final override fun onStart() {

        this.onStartUncached()
    }

    final override fun onStop() {
        this.onStopUncached()
    }

    protected fun cacheAndNotifyListeners(value: A) {
        notifyListeners(value)
    }

    protected abstract fun sampleUncached(): A

    protected open fun onStartUncached() {}

    protected open fun onStopUncached() {}
}
