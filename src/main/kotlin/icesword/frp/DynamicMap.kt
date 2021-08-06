package icesword.frp

import icesword.frp.dynamic_map.DynamicMapMapKeys

class MapUnion<K, V>(
    private val map1: Map<K, V>,
    private val map2: Map<K, V>,
) : Map<K, V> {
    private val content: Map<K, V>
        get() = map1 + map2

    override val entries: Set<Map.Entry<K, V>>
        get() = content.entries

    override val keys: Set<K>
        get() = throw NotImplementedError()

    override val size: Int
        get() = throw NotImplementedError()

    override val values: Collection<V>
        get() = throw NotImplementedError()

    override fun containsKey(key: K): Boolean {
        return map1.containsKey(key) || map2.containsKey(key)
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

    val changes: Stream<MapChange<K, V>>

    companion object {
        fun <K, V> of(content: Map<K, V>): DynamicMap<K, V> =
            ContentDynamicMap(Cell.constant(content))

        fun <K, V> diff(content: Cell<Map<K, V>>): DynamicMap<K, V> =
            DiffDynamicMap(content)

//        fun <K, V> diffDynamic(vm: Cell<Map<K, V>>): DynamicMap<K, V> {
//            val initialContent = vm.sample();
//
//            val changes = vm.values().map { valueChange ->
//                val oldMap = valueChange.oldValue;
//                val newMap = valueChange.newValue;
//                icesword.frp.MapChange.diff(oldMap, newMap);
//            }
//
//            return holdRc(
//                initialContent,
//                changes = changes,
//            );
//        }


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
    DynamicMapMapKeys(this, transform)

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

abstract class SimpleDynamicMap<K, V> : DynamicMap<K, V>, SimpleObservable<MapChange<K, V>>() {
//    override val content: Cell<Map<K, V>>
//        get() = TODO("Not yet implemented")

    override val changes: Stream<MapChange<K, V>>
        get() = Stream.source(this::subscribe)
}

class RawCell<A>(
    private val sampleValue: () -> A,
    private val changes: Stream<A>,
) : CachingCell<A>() {
    private var subscription: Subscription? = null

    override fun sampleUncached(): A = this.sampleValue()

    override fun onStartUncached() {
        subscription = changes.subscribe {
            cacheAndNotifyListeners(it)
        }
    }

    override fun onStopUncached() {
        subscription!!.unsubscribe()

        subscription = null
    }
}

class DiffDynamicMap<K, V>(
    private val inputContent: Cell<Map<K, V>>,
) : SimpleDynamicMap<K, V>() {
    private var mutableContent: MutableMap<K, V>? = null

    private var subscription: Subscription? = null

    override val changes: Stream<MapChange<K, V>>
        get() = Stream.source(this::subscribe)

    override val content: Cell<Map<K, V>>
        get() = RawCell(
            { mutableContent!!.toMap() },
            changes.map { mutableContent!!.toMap() },
        )

    override fun onStart() {
        subscription = inputContent.subscribe { newContent ->
            val change = MapChange.diff(mutableContent!!, newContent)
            change.applyTo(mutableContent!!)
            notifyListeners(change)
        }

        mutableContent = inputContent.sample().toMutableMap()
    }

    override fun onStop() {
        mutableContent = null

        subscription!!.unsubscribe()
    }
}

class ContentDynamicMap<K, V>(
    override val content: Cell<Map<K, V>>,
) : DynamicMap<K, V> {
    override val changes: Stream<MapChange<K, V>>
        get() = TODO()

}

class MutableDynamicMap<K, V>(
    initialContent: Map<K, V>,
) : DynamicMap<K, V> {
    companion object {
        fun <K, V> of(content: Map<K, V>): MutableDynamicMap<K, V> =
            MutableDynamicMap(content)
    }

    private val _content = MutCell(initialContent.toMap())

    private val _changes = StreamSink<MapChange<K, V>>()

    fun put(key: K, value: V) {
        val oldContent = _content.sample()
        val newContent = oldContent + (key to value)
        _content.set(newContent)

        val change = MapChange.diff(oldContent, newContent)
        _changes.send(change)
    }

    override val content: Cell<Map<K, V>>
        get() = _content

    override val changes: Stream<MapChange<K, V>>
        get() = _changes
}

