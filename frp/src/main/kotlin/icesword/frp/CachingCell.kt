package icesword.frp

private sealed class Cache<A> {
    data class FullCache<A>(val cachedValue: A) : Cache<A>()
    class EmptyCache<A> : Cache<A>()
}

abstract class CachingCell<A>(identity: Identity) : SimpleCell<A>(identity = identity) {
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
        when (val c = this.cache) {
            is Cache.FullCache -> if (c.cachedValue != value) {
                val change = ValueChange(
                    oldValue = c.cachedValue,
                    newValue = value,
                )
                cache = Cache.FullCache(value)
                notifyListeners(change)
            }
            is Cache.EmptyCache -> {
//                println("$name: Cache is empty! Doing nothing.")
            }
        }
    }

    protected abstract fun sampleUncached(): A

    protected open fun onStartUncached() {}

    protected open fun onStopUncached() {}
}
