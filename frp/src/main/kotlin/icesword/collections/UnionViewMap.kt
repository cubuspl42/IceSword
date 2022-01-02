package icesword.collections

fun <K, V> Map<K, V>.unionView(otherView: Map<K, V>): Map<K, V> =
    UnionViewMap(this, otherView)

private class UnionViewMap<K, V>(
    private val source1: Map<K, V>,
    private val source2: Map<K, V>,
) : Map<K, V> {
    override val entries: Set<Map.Entry<K, V>>
        get() = (source1 + source2).entries

    override val keys: Set<K>
        get() = (source1 + source2).keys

    override val size: Int
        get() = (source1 + source2).size

    override val values: Collection<V>
        get() = (source1 + source2).values

    override fun containsKey(key: K): Boolean =
        source1.containsKey(key) || source2.containsKey(key)

    override fun containsValue(value: V): Boolean =
        values.any { it == value }

    override fun get(key: K): V? =
        source2[key] ?: source1[key]

    override fun isEmpty(): Boolean =
        source1.isEmpty() && source2.isEmpty()
}
