package icesword.collections

import frpjs.Hash
import frpjs.fastSetOf

interface SetFactory<E> {
    fun create(): MutableSet<E>
}

class DefaultSetFactory<E> : SetFactory<E> {
    override fun create(): MutableSet<E> =
        mutableSetOf()
}

value class FastSetFactory<E>(
    private val hash: Hash<E>,
) : SetFactory<E> {
    override fun create(): MutableSet<E> =
        fastSetOf(hash = hash)
}



fun <T> Set<T>.minusThrough(through: SetFactory<T>, other: Set<T>): Set<T> = when {
    other.isEmpty() -> this.toSet()
    else -> this.filterNotTo(through.create()) { it in other }
}


