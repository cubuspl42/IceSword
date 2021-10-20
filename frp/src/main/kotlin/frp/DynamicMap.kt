package icesword.frp

import icesword.collections.MapFactory
import icesword.frp.dynamic_map.*
import icesword.frp.dynamic_set.KeysDynamicSet

interface DynamicMap<K, V> {
    val content: Cell<Map<K, V>>

    // May be view, may be copy
    val volatileContentView: Map<K, V>

    val changes: Stream<MapChange<K, V>>

    fun containsKeyNow(key: K): Boolean {
        return volatileContentView.containsKey(key)
    }

    fun get(key: K): Cell<V?> =
        this.changes
            .filter { it.containsKey(key) }
            .map { it.added[key] ?: it.updated[key] }
            .correlate { getNow(key) }

    fun getNow(key: K): V? {
        return volatileContentView[key]
    }

    companion object {
        fun <K, V> of(content: Map<K, V>): DynamicMap<K, V> =
            StaticDynamicMap(content)

        fun <K, V> diff(
            content: Cell<Map<K, V>>,
            tag: String,
        ): DynamicMap<K, V> =
            DiffDynamicMap(
                content,
                tag = "$tag/diff",
            )
                .validated(tag = tag)

        fun <K, V> fromEntries(
            entries: DynamicSet<Pair<K, V>>
        ): DynamicMap<K, V> =
            FromEntriesDynamicMap(entries, tag = "fromEntries")

//        fun <K, V> fromEntriesDiff(
//            entries: DynamicSet<Pair<K, V>>
//        ): DynamicMap<K, V> =
//            diff(
//                entries.content.map { staticEntries ->
//                    staticEntries.associate { it }
//                },
//                tag = "fromEntries",
//            )

        // FIXME: V/R / merge mess...
        fun <K, V, R> unionMerge(
            through: MapFactory<K, V>,
            maps: DynamicSet<DynamicMap<K, V>>,
            merge: (Set<V>) -> R,
            tag: String,
        ): DynamicMap<K, V> =
            unionMerge_(
                through = through,
                maps = maps,
                merge = { it.iterator().next() },
                tag = tag,
            )
//                .also {
//                    it.changes.subscribe { change ->
//                        println("unionMerge change: $change")
//                    }
//                }

        fun <K, V, R> unionMerge_(
            through: MapFactory<K, R>,
            maps: DynamicSet<DynamicMap<K, V>>,
            merge: (Set<V>) -> R,
            tag: String,
        ): DynamicMap<K, R> =
            DynamicMapUnionMerge(
                through = through,
                maps = maps,
                merge = merge,
                tag = tag,
            )
                .validated(tag = tag)

//        private fun <K, V, R> unionMergeDiff(
//            maps: DynamicSet<DynamicMap<K, V>>,
//            merge: (Set<V>) -> R,
//            tag: String,
//        ): DynamicMap<K, V> =
//            diff(
//                maps.content.switchMap { sd ->
//                    val clm: Cell<List<Map<K, V>>> =
//                        Cell.traverse(sd) { it.content }
//                    val cm = clm.map { lm ->
//                        lm.fold(emptyMap<K, V>()) { acc, m -> acc + m }
//                    }
//                    cm
//                },
//                tag = "$tag/unionMerge",
//            ).also {
//                println("unionMerge called")
//            }


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

//val <K, V> DynamicMap<K, V>.entries: DynamicSet<Map.Entry<K, V>>
//    get() = DynamicSet.diff(
//        content.map { it.entries }
//    )

fun <K, V> DynamicMap<K, V>.changesUnits(): Stream<Unit> =
    changes.map { }

fun <K, V> DynamicMap<K, V>.sample(): Map<K, V> = volatileContentView.toMap()

//fun <K, V> DynamicMap<K, V>.unionMerge(other: DynamicMap<K, V>, tag: String): DynamicMap<K, V> =
//    DynamicMapUnion(this, other, tag = tag)
//        .validated(tag = tag)

//fun <K, V, R> DynamicMap<K, V>.mapKeys(tag: String, transform: (Map.Entry<K, V>) -> R): DynamicMap<R, V> =
//    DynamicMapMapKeys(this, transform, tag = tag)
//        .validated(tag = tag)

//fun <K, V, K2> DynamicMap<K, V>.mapKeysDynamic(
//    transform: (Map.Entry<K, V>) -> Cell<K2>,
//): DynamicMap<K2, V> =
//    fromEntries(
//        this.entries.fuseMap { entry ->
//            transform(entry).map { key -> key to entry.value }
//        }
//    )

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

fun <K, V> DynamicMap<K, Cell<V>>.fuseValues(tag: String? = null): DynamicMap<K, V> {
    val effectiveTag = tag ?: "fuseValues"
    return DynamicMapFuseValues(this, tag = effectiveTag)
        .validated(tag = effectiveTag)
}

fun <K, V, K2, V2> DynamicMap<K, V>.project(
    projectKey: (K) -> Iterable<K2>,
    buildValue: (K2, Map<K, V>) -> V2,
    tag: String = "project",
): DynamicMap<K2, V2> =
    ProjectDynamicMap(
        source = this,
        projectKey = projectKey,
        buildValue = buildValue,
        tag = tag,
    ).validated(tag = tag)

private const val enableMapValidation = true

fun <K, V> DynamicMap<K, V>.validated(tag: String): DynamicMap<K, V> =
    if (enableMapValidation) ValidatedDynamicMap(this, sourceTag = tag)
    else this

fun <K, V, V2 : Any> DynamicMap<K, V>.mapValuesNotNull(
    tag: String,
    transform: (Map.Entry<K, V>) -> V2?,
): DynamicMap<K, V2> =
    MapValuesNotNullDynamicMap(
        source = this,
        transform = transform,
        tag = tag,
    )

//fun <K, V, V2 : Any> DynamicMap<K, V>.mapValuesNotNullDiff(
//    tag: String,
//    transform: (Map.Entry<K, V>) -> V2?
//): DynamicMap<K, V2> =
//    DynamicMap.diff(
//        content.map { content ->
//            content.entries.mapNotNull { e ->
//                transform(e)?.let { e.key to it }
//            }.toMap()
//        },
//        tag = "mapValuesNotNull",
//    )

//

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


fun <K, V, V2> DynamicMap<K, V>.fuseMapValues(f: (K, V) -> V2): DynamicMap<K, V2> =
    TODO()

fun <K, V> DynamicMap<K, V>.getKeys(tag: String = "getKeys"): DynamicSet<K> =
    KeysDynamicSet(this, tag = tag)

//val <K, V> DynamicMap<K, V>.contentView: Cell<Map<K, V>>
//    get() = TODO()

val <K, V> DynamicMap<K, V>.contentDynamicView: DynamicView<Map<K, V>>
    get() = DynamicView(
        updates = this.changesUnits(),
        // FIXME: This is glitch-prone, as that view is actually volatile (unreliable)
        view = this.volatileContentView,
    )

val <K, V> DynamicMap<K, V>.valuesSet: DynamicSet<V>
    get() = DynamicSet.diff(
        content = content.map { it.values.toSet() }
    )

//val <K, V> DynamicMap<K, V>.valuesSet: DynamicSet<V>
//    get() = ValuesSetDynamicSet(this)


fun <K, V> DynamicMap<K, V>.getNow(key: K): V? = this.volatileContentView[key]

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
    tag: String,
) : SimpleDynamicMap<K, V>(tag) {
    private var mutableContent: MutableMap<K, V>? = null

    override val volatileContentView: Map<K, V>
        get() = mutableContent ?: inputContent.sample()

    private var subscription: Subscription? = null

    override val changes: Stream<MapChange<K, V>> by lazy {
        Stream.source(this::subscribe, tag = "$tag.changes")
    }

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

class MutableDynamicMap<K, V : Any>(
    initialContent: Map<K, V>,
) : SimpleDynamicMap<K, V>(tag = "MutableDynamicMap") {
    companion object {
        fun <K, V : Any> of(content: Map<K, V>): MutableDynamicMap<K, V> =
            MutableDynamicMap(content)
    }

    private val mutableContent: MutableMap<K, V> = initialContent.toMutableMap()

    override val volatileContentView: Map<K, V>
        get() = mutableContent

    fun put(key: K, value: V) {
        val oldValue = mutableContent.put(key, value)

        if (oldValue == null) {
            notifyListeners(
                MapChange(
                    added = mapOf(key to value),
                    updated = emptyMap(),
                    removedEntries = emptyMap(),
                ),
            )
        } else {
            notifyListeners(
                MapChange(
                    added = emptyMap(),
                    updated = mapOf(key to value),
                    removedEntries = emptyMap(),
                ),
            )
        }
    }

    fun applyChange(change: MapChange<K, V>) {
        change.applyToChecked(mutableContent)
        notifyListeners(change)
    }

    fun remove(key: K) {
        if (mutableContent.containsKey(key)) {
            val oldValue = mutableContent.remove(key) ?: throw IllegalStateException()

            val change = MapChange(
                added = emptyMap(),
                updated = emptyMap(),
                removedEntries = mapOf(key to oldValue),
            )

            notifyListeners(change)
        }
    }
}

