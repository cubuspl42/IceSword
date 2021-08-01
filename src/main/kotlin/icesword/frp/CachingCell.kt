package icesword.frp

private sealed class Cache<A> {
    data class FullCache<A>(val cachedValue: A) : Cache<A>()
    class EmptyCache<A> : Cache<A>()
}

abstract class CachingCell<A> : SimpleCell<A>() {
    private var cache: Cache<A> = Cache.EmptyCache()

    final override fun sample(): A =
        when (val c = cache) {
            is Cache.FullCache -> c.cachedValue
            is Cache.EmptyCache -> sampleUncached()
        }

    final override fun onStart() {
        cache = Cache.FullCache(sampleUncached())

        this.onStartUncached()
    }

    final override fun onStop() {
        this.onStopUncached()
        cache = Cache.EmptyCache()
    }

    protected fun cacheAndNotifyListeners(value: A) {
        cache = Cache.FullCache(value)

        notifyListeners(value)
    }

    protected abstract fun sampleUncached(): A

    protected open fun onStartUncached() {}

    protected open fun onStopUncached() {}
}
