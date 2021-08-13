package icesword.frp

import kotlinx.css.ol

data class SetChange<A>(
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
                added = newSet - oldSet,
                removed = oldSet - newSet,
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

    fun applyTo(mutableSet: MutableSet<A>) {
        added.forEach { key ->
            mutableSet.add(key)
        }

        removed.forEach { key ->
            mutableSet.remove(key)
        }
    }
}