package icesword.frp

interface Subscription {
    companion object {
        fun noop(): Subscription = object : Subscription {
            override fun unsubscribe() {
            }
        }
    }

    fun unsubscribe()
}
