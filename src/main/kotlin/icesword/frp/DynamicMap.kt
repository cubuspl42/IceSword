package icesword.frp

import icesword.frp.dynamic_map.DynamicMapFuseValues
import icesword.frp.dynamic_map.DynamicMapMapKeys
import icesword.frp.dynamic_map.DynamicMapUnion
import icesword.frp.dynamic_map.MapValuesNotNullDynamicMap

interface DynamicMap<K, V> {
    val content: Cell<Map<K, V>>

    // May be view, may be copy
    val volatileContentView: Map<K, V>

    val changes: Stream<MapChange<K, V>>

    fun containsKeyNow(key: K): Boolean {
        return volatileContentView.containsKey(key)
    }

    companion object {
        fun <K, V> of(content: Map<K, V>): DynamicMap<K, V> =
            StaticDynamicMap(content)

//        fun <K, V> diff(content: Cell<Map<K, V>>): DynamicMap<K, V> =
//            DiffDynamicMap(content)

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

fun <K, V> DynamicMap<K, V>.changesUnits(): Stream<Unit> =
    changes.map { }

fun <K, V> DynamicMap<K, V>.sample(): Map<K, V> = volatileContentView.toMap()

fun <K, V> DynamicMap<K, V>.union(other: DynamicMap<K, V>): DynamicMap<K, V> =
    DynamicMapUnion(this, other)

fun <K, V, R> DynamicMap<K, V>.mapKeys(transform: (Map.Entry<K, V>) -> R): DynamicMap<R, V> =
    DynamicMapMapKeys(this, transform)

//fun <K, V, R> DynamicMap<K, V>.fuseMapKeys(transform: (Map.Entry<K, V>) -> Cell<R>): DynamicMap<R, V> =
//    DynamicMap.diff(
//        content.switchMap { content ->
//            val dynamicEntries = content.entries.map { entry ->
//                transform(entry).map { k2 -> k2 to entry.value }
//            }
//
//            Cell.sequence(dynamicEntries).map { entries -> entries.toMap() }
//        },
//    )

//fun <K, V> DynamicMap<K, Cell<V>>.fuseValues(): DynamicMap<K, V> =
//    DynamicMap.diff(
//        content.switchMap { content ->
//            val dynamicEntries = content.entries.map { (key, valueCell) ->
//                valueCell.map { value -> key to value }
//            }
//
//            Cell.sequence(dynamicEntries).map { entries -> entries.toMap() }
//        },
//    )

fun <K, V> DynamicMap<K, Cell<V>>.fuseValues(): DynamicMap<K, V> =
    DynamicMapFuseValues(this)

//fun <K, V, V2 : Any> DynamicMap<K, V>.mapValuesNotNull(transform: (K, V) -> V2?): DynamicMap<K, V2> =
//    DynamicMap.diff(
//        content.map { content ->
//            content.entries.mapNotNull { (key, value) ->
//                transform(key, value)?.let { key to it }
//            }.toMap()
//        },
//    )

fun <K, V, V2 : Any> DynamicMap<K, V>.mapValuesNotNull(transform: (Map.Entry<K, V>) -> V2?): DynamicMap<K, V2> =
    MapValuesNotNullDynamicMap(this, transform)

//fun <K, V> DynamicMap<K, V?>.filterValuesNotNull(): DynamicMap<K, V> =
//    DynamicMap.diff(
//        content.map { content ->
//            content.entries.mapNotNull { (key, valueOrNull) ->
//                valueOrNull?.let { value -> key to value }
//            }.toMap()
//        },
//    )

fun <K, V : Any> DynamicMap<K, V?>.filterValuesNotNull(): DynamicMap<K, V> =
    this.mapValuesNotNull { (_, value) -> value }

//val <K, V> DynamicMap<K, V>.keys: DynamicSet<K>
//    get() = DynamicSet.diff(content.map { it.keys })

val <K, V> DynamicMap<K, V>.valuesSet: DynamicSet<V>
    get() = DynamicSet.diff(
        content = content.map { it.values.toSet() }
    )

fun <K, V> DynamicMap<K, V>.getNow(key: K): V? = this.volatileContentView[key]

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

    override val volatileContentView: Map<K, V>
        get() = mutableContent ?: inputContent.sample()

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

//class ContentDynamicMap<K, V>(
//    override val content: Cell<Map<K, V>>,
//) : DynamicMap<K, V> {
//    override val changes: Stream<MapChange<K, V>>
//        get() = TODO()
//
//    override val volatileContentView: Map<K, V>
//        get() = content.sample()
//
//}

class StaticDynamicMap<K, V>(
    private val staticContent: Map<K, V>,
) : DynamicMap<K, V> {
    override val changes: Stream<MapChange<K, V>>
        get() = Stream.never()

    override val volatileContentView: Map<K, V>
        get() = staticContent

    override val content: Cell<Map<K, V>>
        get() = Cell.constant(staticContent)
}

class MutableDynamicMap<K, V>(
    initialContent: Map<K, V>,
) : DynamicMap<K, V> {
    companion object {
        fun <K, V> of(content: Map<K, V>): MutableDynamicMap<K, V> =
            MutableDynamicMap(content)
    }

    private val _content = MutCell(initialContent.toMap())

    override val volatileContentView: Map<K, V>
        get() = _content.sample()

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

