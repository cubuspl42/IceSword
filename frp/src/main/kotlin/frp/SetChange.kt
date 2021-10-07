package icesword.frp

import icesword.collections.SetFactory
import icesword.collections.minusThrough

data class SetChange<out A>(
    val added: Set<A>,
    val removed: Set<A>,
) {
    companion object {
        /// A no-op change with all groups being empty is valid.
        fun <A> empty(): SetChange<A> =
            SetChange(
                added = emptySet(),
                removed = emptySet(),
            )

        fun <A> diff(
            oldSet: Set<A>,
            newSet: Set<A>,
        ): SetChange<A> =
            SetChange(
                added = newSet.minus(oldSet),
                removed = oldSet.minus(newSet),
            )

        fun <A> diffThrough(
            through: SetFactory<A>,
            oldSet: Set<A>,
            newSet: Set<A>,
        ): SetChange<A> =
            SetChange(
                added = newSet.minusThrough(through, oldSet),
                removed = oldSet.minusThrough(through, newSet),
            )
    }

    fun <B> map(
        f: (A) -> B,
    ): SetChange<B> {
        val added = this.added.map(f).toSet()
        val removed = this.removed.map(f).toSet()

        return SetChange(
            added = added,
            removed = removed,
        )
    }

    fun isEmpty(): Boolean =
        added.isEmpty() && removed.isEmpty()
}

fun <A> SetChange<A>.applyTo(mutableSet: MutableSet<A>) {
    added.forEach { key ->
        mutableSet.add(key)
    }

    removed.forEach { key ->
        mutableSet.remove(key)
    }
}
