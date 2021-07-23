package icesword.frp

class MapUnion<K, V>(
    private val map1: Map<K, V>,
    private val map2: Map<K, V>,
) : Map<K, V> {
    override val entries: Set<Map.Entry<K, V>>
        get() = TODO("Not yet implemented")

    override val keys: Set<K>
        get() = TODO("Not yet implemented")

    override val size: Int
        get() = TODO("Not yet implemented")

    override val values: Collection<V>
        get() = TODO("Not yet implemented")

    override fun containsKey(key: K): Boolean {
        TODO("Not yet implemented")
    }

    override fun containsValue(value: V): Boolean {
        TODO("Not yet implemented")
    }

    override fun get(key: K): V? =
        map2[key] ?: map1[key]

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }
}

interface DynamicMap<K, V> {

    val content: Cell<Map<K, V>>


    companion object {
        fun <K, V> of(content: Map<K, V>): DynamicMap<K, V> =
            ContentDynamicMap(Cell.constant(content))

        fun <K, V> diff(content: Cell<Map<K, V>>): DynamicMap<K, V> =
            ContentDynamicMap(content)
    }
}



fun <K, V> DynamicMap<K, V>.changes(): Stream<Unit> =
    content.values().map { }

fun <K, V> DynamicMap<K, V>.sample(): Map<K, V> = content.sample()

fun <K, V> DynamicMap<K, V>.union(other: DynamicMap<K, V>): DynamicMap<K, V> =
    DynamicMap.diff(
        Cell.map2(content, other.content) { self, other -> MapUnion(self, other) },
    )

fun <K, V, R> DynamicMap<K, V>.mapKeys(transform: (Map.Entry<K, V>) -> R): DynamicMap<R, V> =
    DynamicMap.   diff(
        content.map { it.mapKeys(transform) },
    )

class ContentDynamicMap<K, V>(
    override val content: Cell<Map<K, V>>,
) : DynamicMap<K, V> {

}

class MutableDynamicMap<K, V>(
    initialContent: Map<K, V>,
) : DynamicMap<K, V> {
    companion object {
        fun <K, V> of(content: Map<K, V>): MutableDynamicMap<K, V> =
            MutableDynamicMap(content)
    }

    private val _content = MutCell(initialContent.toMap())

    fun put(key: K, value: V) {
        val oldContent = _content.sample()
        _content.set(oldContent + (key to value))
    }

    override val content: Cell<Map<K, V>>
        get() = _content
}
