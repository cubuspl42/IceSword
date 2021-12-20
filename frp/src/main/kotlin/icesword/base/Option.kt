package icesword.base

// Option type can be useful in a null-safe language like Kotlin, for example
// when dealing with a generic type T without any bounds.
sealed interface Option<A> {
    fun <B> fold(ifNone: () -> B, ifSome: (A) -> B): B
}

data class Some<A>(val value: A) : Option<A> {
    override fun <B> fold(ifNone: () -> B, ifSome: (A) -> B): B =
        ifSome(value)
}

fun <A> some(value: A): Option<A> = Some(value)

fun <A> none(): Option<A> = None()

class None<A> : Option<A> {
    override fun <B> fold(ifNone: () -> B, ifSome: (A) -> B): B =
        ifNone()

    override fun equals(other: Any?): Boolean =
        other is None<*>

    override fun hashCode(): Int = 0
}

inline fun <T, R> Option<T>.ifSome(crossinline block: (T) -> R): Unit =
    this.fold(
        ifNone = { },
        { block(it) },
    )

inline fun <T, R> Iterable<T>.mapSome(transform: (T) -> Option<R>): List<R> {
    val destination = mutableListOf<R>()

    forEach { element ->
        transform(element).ifSome(destination::add)
    }

    return destination
}
