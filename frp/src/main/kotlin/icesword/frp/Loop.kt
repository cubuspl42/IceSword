package icesword.frp

class Loop<A : Any> : Lazy<A> {
    companion object {
        fun <A : Any> looped(block: (Lazy<A>) -> A): A {
            val loop = Loop<A>()

            val result = block(loop)

            loop.close(result)

            return result
        }
    }

    private lateinit var _value: A

    fun close(value: A) {
        if (isInitialized()) {
            throw IllegalStateException("Loop is already closed")
        }

        this._value = value
    }

    override fun isInitialized(): Boolean =
        this::_value.isInitialized

    override val value: A
        get() = _value
}
