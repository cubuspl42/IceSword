package icesword.frp

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
