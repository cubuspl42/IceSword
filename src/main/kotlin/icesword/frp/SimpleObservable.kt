package icesword.frp

abstract class SimpleObservable<A> : Observable<A> {
    private val listeners = hashSetOf<(A) -> Unit>()

    override fun subscribe(handler: (A) -> Unit): Subscription {
        addListener(handler)

        return object : Subscription {
            override fun unsubscribe() {
                this@SimpleObservable.removeListener(handler)
            }
        }
    }

    private fun addListener(h: (A) -> Unit) {
        val wasAdded = listeners.add(h)

        if (!wasAdded) {
            throw  IllegalStateException("Attempted to add same listener twice")
        }

        if (listeners.size == 1) {
            onStart()
        }
    }

    private fun removeListener(h: (A) -> Unit) {
        val wasThere = listeners.remove(h)

        if (!wasThere) {
            throw  IllegalStateException("Attempted to remove non-existing listener")
        }

        if (listeners.isEmpty()) {
            onStop()
        }
    }

    protected fun notifyListeners(a: A): Unit {
        val oldListeners = listeners.toList()
        oldListeners.forEach { it(a) }
    }

    protected open fun onStart() {}

    protected open fun onStop() {}
}