package icesword.frp

class MapUnion<K, V>(
    private val map1: Map<K, V>,
    private val map2: Map<K, V>,
) : Map<K, V> {
    override val entries: Set<Map.Entry<K, V>>
        get() = throw NotImplementedError()

    override val keys: Set<K>
        get() = throw NotImplementedError()

    override val size: Int
        get() = throw NotImplementedError()

    override val values: Collection<V>
        get() = throw NotImplementedError()

    override fun containsKey(key: K): Boolean {
        throw NotImplementedError()
    }

    override fun containsValue(value: V): Boolean {
        throw NotImplementedError()
    }

    override fun get(key: K): V? =
        map2[key] ?: map1[key]

    override fun isEmpty(): Boolean {
        throw NotImplementedError()
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
    DynamicMap.diff(
        content.map { it.mapKeys(transform) },
    )

fun <K, V, R> DynamicMap<K, V>.fuseMapKeys(transform: (Map.Entry<K, V>) -> Cell<R>): DynamicMap<R, V> =
    DynamicMap.diff(
        content.switchMap { content ->
            val dynamicEntries = content.entries.map { entry ->
                transform(entry).map { k2 -> k2 to entry.value }
            }

            Cell.sequence(dynamicEntries).map { entries -> entries.toMap() }
        },
    )

fun <K, V> DynamicMap<K, Cell<V>>.fuseValues(): DynamicMap<K, V> =
    DynamicMap.diff(
        content.switchMap { content ->
            val dynamicEntries = content.entries.map { (key, valueCell) ->
                valueCell.map { value -> key to value }
            }

            Cell.sequence(dynamicEntries).map { entries -> entries.toMap() }
        },
    )

fun <K, V> DynamicMap<K, V?>.filterKeysNotNull(): DynamicMap<K, V> =
    DynamicMap.diff(
        content.map { content ->
            content.entries.mapNotNull { (key, valueOrNull) ->
                valueOrNull?.let { value -> key to value }
            }.toMap()
        },
    )

val <K, V> DynamicMap<K, V>.keys: DynamicSet<K>
    get() = DynamicSet.diff(content.map { it.keys })

val <K, V> DynamicMap<K, V>.valuesSet: DynamicSet<V>
    get() = DynamicSet.diff(
        content = content.map { it.values.toSet() }
    )

class ContentDynamicMap<K, V>(
    override val content: Cell<Map<K, V>>,
) : DynamicMap<K, V>

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

