package icesword.collections

fun <K, V, R> Map<K, V>.mapValuesLazy(transform: (Map.Entry<K, V>) -> R): Map<K, R> {
    return ContentMap(this, transform)
}

class ContentMap<K, V, R>(
    private val source: Map<K, V>,
    private val transform: (Map.Entry<K, V>) -> R,
) : Map<K, R> {
    override val entries: Set<Map.Entry<K, R>>
        get() = EntrySet(source, transform)

    override val keys: Set<K>
        get() = source.keys

    override val size: Int
        get() = source.size

    override val values: Collection<R>
        get() = source.entries.map { transform(it) }

    override fun containsKey(key: K): Boolean =
        source.containsKey(key)

    override fun containsValue(value: R): Boolean =
        source.entries.any { transform(it) == value }

    override fun get(key: K): R? =
        source[key]?.let { value ->
            transform(Entry(key, value))
        }

    override fun isEmpty(): Boolean =
        source.isEmpty()
}

private class EntrySet<K, V, R>(
    private val source: Map<K, V>,
    private val transform: (Map.Entry<K, V>) -> R,
) : Set<Map.Entry<K, R>> {
    override val size: Int
        get() = TODO("Not yet implemented")

    override fun contains(element: Map.Entry<K, R>): Boolean =
        source.any { it.key == element.key && it.value == element.value }

    override fun containsAll(elements: Collection<Map.Entry<K, R>>): Boolean =
        elements.all(this::contains)

    override fun isEmpty(): Boolean =
        source.isEmpty()

    override fun iterator(): Iterator<Map.Entry<K, R>> =
        sequence<Map.Entry<K, R>> {
            for (entry in source) {
                val key = entry.key
                val value = transform(entry)

                yield(Entry(key, value))
            }
        }.iterator()
}

private data class Entry<K, V>(
    override val key: K,
    override val value: V,
) : Map.Entry<K, V>
