package icesword.frp

private external class Set<A>(
    other: Set<A> = definedExternally,
) {
    fun add(value: A): Set<A>

    // Returns true if value was already in mySet; otherwise false.
    fun delete(value: A): Boolean

    fun has(value: A): Boolean

    fun forEach(callback: (value: A) -> Unit)

    val size: Int
}

private fun <A> Set<A>.isEmpty(): Boolean =
    this.size == 0

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

    private val listeners = Set<(A) -> Unit>()

    override fun subscribe(handler: (A) -> Unit): Subscription {
        addListener(handler)

        return object : Subscription {
            override fun unsubscribe() {
                this@SimpleObservable.removeListener(handler)
            }
        }
    }

    private fun addListener(h: (A) -> Unit) {
        val wasAdded = !listeners.has(h)

        listeners.add(h)

        if (!wasAdded) {
            throw IllegalStateException("Attempted to add same listener twice")
        }

        if (listeners.size == 1) {
            debugLog { "$name: starts" }
            onStart()
        }
    }

    private fun removeListener(h: (A) -> Unit) {
        val wasThere = listeners.delete(h)

        if (!wasThere) {
            throw IllegalStateException("Attempted to remove non-existing listener")
        }

        if (listeners.isEmpty()) {
            debugLog { "$name: stops" }
            onStop()
        }
    }

    protected fun notifyListeners(a: A) {
        val oldListeners = Set(listeners)

        debugLog { "$name: Starting notifying listeners (${oldListeners.size})..." }

        oldListeners.forEach {
            if (!listeners.has(it)) {
                debugLog { "$name: Notifying dead listener" }
            }
            it(a)
        }

//        debugLog { "$name: Ended notifying listeners" }
    }

    protected open fun onStart() {}

    protected open fun onStop() {}
}


