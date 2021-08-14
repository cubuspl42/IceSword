package icesword.frp

import icesword.frp.DynamicMap.Companion.fromEntries
import icesword.frp.dynamic_map.*

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

        fun <K, V> diff(content: Cell<Map<K, V>>): DynamicMap<K, V> =
            DiffDynamicMap(content)

        fun <K, V> fromEntries(
            entries: DynamicSet<Pair<K, V>>
        ): DynamicMap<K, V> =
            diff(
                entries.content.map { staticEntries ->
                    staticEntries.associate { it }
                }
            )

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

val <K, V> DynamicMap<K, V>.entries: DynamicSet<Map.Entry<K, V>>
    get() = DynamicSet.diff(
        content.map { it.entries }
    )

fun <K, V> DynamicMap<K, V>.changesUnits(): Stream<Unit> =
    changes.map { }

fun <K, V> DynamicMap<K, V>.sample(): Map<K, V> = volatileContentView.toMap()

fun <K, V> DynamicMap<K, V>.union(other: DynamicMap<K, V>, tag: String): DynamicMap<K, V> =
    DynamicMapUnion(this, other, tag = tag)

fun <K, V, R> DynamicMap<K, V>.mapKeys(tag: String, transform: (Map.Entry<K, V>) -> R): DynamicMap<R, V> =
    DynamicMapMapKeys(this, transform, tag = tag)

fun <K, V, K2> DynamicMap<K, V>.mapKeysDynamic(
    transform: (Map.Entry<K, V>) -> Cell<K2>,
): DynamicMap<K2, V> =
    fromEntries(
        this.entries.fuseMap { entry ->
            transform(entry).map { key -> key to entry.value }
        }
    )

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

fun <K, V> DynamicMap<K, Cell<V>>.fuseValues(tag: String? = null): DynamicMap<K, V> =
    DynamicMapFuseValues(this, tag = tag ?: "fuseValues")

fun <K, V, V2 : Any> DynamicMap<K, V>.mapValuesNotNull(
    tag: String,
    transform: (Map.Entry<K, V>) -> V2?
): DynamicMap<K, V2> =
    DynamicMap.diff(
        content.map { content ->
            content.entries.mapNotNull { e ->
                transform(e)?.let { e.key to it }
            }.toMap()
        },
    )

//fun <K, V, V2 : Any> DynamicMap<K, V>.mapValuesNotNull(
//    tag: String? = null,
//    transform: (Map.Entry<K, V>) -> V2?,
//): DynamicMap<K, V2> =
//    MapValuesNotNullDynamicMap(this, transform, tag = tag ?: "mapValuesNotNull")

//fun <K, V> DynamicMap<K, V?>.filterValuesNotNull(): DynamicMap<K, V> =
//    DynamicMap.diff(
//        content.map { content ->
//            content.entries.mapNotNull { (key, valueOrNull) ->
//                valueOrNull?.let { value -> key to value }
//            }.toMap()
//        },
//    )

fun <K, V : Any> DynamicMap<K, V?>.filterValuesNotNull(
    tag: String? = null,
): DynamicMap<K, V> =
    this.mapValuesNotNull(tag = tag ?: "filterValuesNotNull") { (_, value) -> value }

//val <K, V> DynamicMap<K, V>.keys: DynamicSet<K>
//    get() = DynamicSet.diff(content.map { it.keys })


val <K, V> DynamicMap<K, V>.keys: DynamicSet<K>
    get() = DynamicSet.diff(
        content = content.map { it.keys }
    )

val <K, V> DynamicMap<K, V>.valuesSet: DynamicSet<V>
    get() = DynamicSet.diff(
        content = content.map { it.values.toSet() }
    )

//val <K, V> DynamicMap<K, V>.valuesSet: DynamicSet<V>
//    get() = ValuesSetDynamicSet(this)

fun <K, V> DynamicMap<K, V>.getNow(key: K): V? = this.volatileContentView[key]

abstract class SimpleDynamicMap<K, V>(
    tag: String,
) : DynamicMap<K, V>, SimpleObservable<MapChange<K, V>>(
    tag = tag,
) {
//    override val content: Cell<Map<K, V>>
//        get() = TODO("Not yet implemented")

    override val changes: Stream<MapChange<K, V>>
        get() = Stream.source(this::subscribe)
}

class RawCell<A>(
    private val sampleValue: () -> A,
    private val changes: Stream<A>,
) : CachingCell<A>(tag = "RawCell") {
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
) : SimpleDynamicMap<K, V>("DiffDynamicMap") {
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
) : SimpleDynamicMap<K, V>(tag = "MutableDynamicMap") {
    companion object {
        fun <K, V> of(content: Map<K, V>): MutableDynamicMap<K, V> =
            MutableDynamicMap(content)
    }

    private var mutableContent: Map<K, V> = initialContent

    override val volatileContentView: Map<K, V>
        get() = mutableContent

    override val content: Cell<Map<K, V>>
        get() = RawCell(
            { mutableContent },
            changes.map { mutableContent },
        )


    fun put(key: K, value: V) {
        val oldContent = mutableContent
        val newContent = oldContent + (key to value)
        mutableContent = newContent

        val change = MapChange.diff(oldContent, newContent)

        if (change.added.keys.intersect(oldContent.keys).isNotEmpty()) {
            throw IllegalStateException("MutableDynamicMap: change.added.keys.intersect")
        }

        notifyListeners(change)
    }

}

