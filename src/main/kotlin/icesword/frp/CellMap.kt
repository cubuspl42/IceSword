package icesword.frp

class CellMap<A, B>(
    private val source: Cell<A>,
    private val f: (A) -> B,
) : SimpleCell<B>() {
    private var subscription: Subscription? = null

    override fun sample(): B =
        f(source.sample())

    override fun onStart() {
        subscription = source.subscribe {
            notifyListeners(f(it))
        }
    }

    override fun onStop() {
        subscription!!.unsubscribe()
        subscription = null
    }
}
