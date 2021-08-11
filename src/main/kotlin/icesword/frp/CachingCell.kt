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
        this.onStartUncached()
        cache = Cache.FullCache(sampleUncached())

    }

    final override fun onStop() {
        cache = Cache.EmptyCache()
        this.onStopUncached()
    }

    protected fun cacheAndNotifyListeners(value: A) {
        (cache as? Cache.FullCache)?.let { fullCache ->
            if (fullCache.cachedValue != value) {
                cache = Cache.FullCache(value)

                notifyListeners(value)
            }
        }
    }

    protected abstract fun sampleUncached(): A

    protected open fun onStartUncached() {}

    protected open fun onStopUncached() {}
}
