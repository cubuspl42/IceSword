package icesword.frp

abstract class SimpleObservable<A>(
    val tag: String,
) : Observable<A> {
    companion object {
        private var nextId = 0

        fun assignId(): Int = ++nextId
    }

    val id = assignId()

    val name: String
        get() = "Observable $tag #$id#"

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
            throw IllegalStateException("Attempted to add same listener twice")
        }

        if (listeners.size == 1) {
            debugLog { "$name: starts" }
            onStart()
        }
    }

    private fun removeListener(h: (A) -> Unit) {
        val wasThere = listeners.remove(h)

        if (!wasThere) {
            throw IllegalStateException("Attempted to remove non-existing listener")
        }

        if (listeners.isEmpty()) {
            debugLog { "$name: stops" }
            onStop()
        }
    }

    protected fun notifyListeners(a: A): Unit {
        val oldListeners = listeners.toList()

        debugLog { "$name: Starting notifying listeners..." }

        oldListeners.forEach {
            if (!listeners.contains(it)) {
                debugLog { "$name: Notifying dead listener"}
            }
            it(a)
        }


        debugLog { "$name: Ended notifying listeners" }
    }

    protected open fun onStart() {}

    protected open fun onStop() {}
}