package icesword.frp

import frpjs.Hash
import icesword.collections.DefaultSetFactory
import icesword.collections.FastSetFactory
import icesword.frp.dynamic_map.DynamicSetAssociateWith
import icesword.frp.dynamic_set.*

interface DynamicSet<out A> {
    companion object {
        fun <A> of(content: Set<A>): DynamicSet<A> =
            StaticDynamicSet(content)

//        fun <A> diff(content: Cell<Set<A>>): DynamicSet<A> =
//            ContentDynamicSet(content)

        fun <A> diff(content: Cell<Set<A>>, tag: String = "diff"): DynamicSet<A> =
            DiffDynamicSet(content, tag = tag)
                .validated("$tag-validated")

//        fun <A> union(sets: DynamicSet<Set<A>>): DynamicSet<A> = diff(
//            sets.content.map { it.flatten().toSet() }
//        )

//        fun <A> union(sets: DynamicSet<DynamicSet<A>>): DynamicSet<A> = diff(
//            sets.content.switchMap { outerSet ->
//                Cell.traverse(outerSet) { innerDynSet ->
//                    innerDynSet.content
//                }.map { setOfSets ->
//                    setOfSets.flatten().toSet()
//                }
//            }
//        )

        fun <A> union(sets: DynamicSet<DynamicSet<A>>): DynamicSet<A> =
            DynamicSetUnion(sets)
                .validated("union")

        fun <A> merge(
            streams: DynamicSet<Stream<A>>,
        ): Stream<A> = streams.content.divertMap {
            Stream.merge(it)
        }

//        fun <A> union(sets: DynamicSet<DynamicSet<A>>): DynamicSet<A> = DynamicSetUnion()

        // Probably, like `fuse`, it's semantically meaningless, because `DynamicView` isn't comparable
        fun <A> blend(dynamicViews: DynamicSet<DynamicView<A>>): DynamicView<Set<A>> =
            DynamicView(
                updates = merge(dynamicViews.map(tag = "blend") { it.updates }),
                view = DynamicSetBlendView(dynamicViews),
            )
    }

    val content: Cell<Set<A>>

    // May be view, may be copy
    val volatileContentView: Set<A>

    val changes: Stream<SetChange<A>>

    fun containsNow(a: @UnsafeVariance A): Boolean {
        return volatileContentView.contains(a)
    }
}

//fun <K, V> DynamicMap<K, V>.get(key: K): Cell<V?> =
//    content.map { it[key] }

fun <A, B> DynamicSet<A>.unionMap(
    tag: String,
    transform: (A) -> Set<B>,
): DynamicSet<B> =
    this.unionMapDynamic(tag = "unionMap") { DynamicSet.of(transform(it)) }

fun <A, B> DynamicSet<A>.unionMapDynamic(
    tag: String,
    transform: (A) -> DynamicSet<B>,
): DynamicSet<B> =
    DynamicSet.union(map(tag = "$tag/map", transform))

fun <A> DynamicSet<A>.unionWith(other: DynamicSet<A>): DynamicSet<A> =
    DynamicSet.union(
        DynamicSet.of(
            setOf(this, other)
        )
    )

fun <A> DynamicSet<A>.trackContent(till: Till): Cell<Set<A>> = content

//fun <A, R> DynamicSet<A>.map(transform: (A) -> R): DynamicSet<R> = DynamicSet.diff(
//    content.map { it.map(transform).toSet() },
//)

fun <A, R> DynamicSet<A>.map(
    tag: String,
    transform: (A) -> R,
): DynamicSet<R> =
    MapDynamicSet(
        // TODO: This validation shouldn't be needed, source should validate itself
        source = this.validated("$tag-this"),
        transform = transform,
        tag = tag,
    ).validated(tag)

fun <A, R> DynamicSet<A>.mapTillRemoved(
    tillAbort: Till,
    transform: (element: A, tillRemoved: Till) -> R,
): DynamicSet<R> =
    MapTillRemovedDynamicSet(
        source = this,
        transform = transform,
        tillAbort = tillAbort,
    ).validated("mapTillNext")

//fun <A, R> DynamicSet<A>.mapDynamic(transform: (A) -> Cell<R>): DynamicSet<R> =
//    DynamicSet.diff(tileOffset.map { tileOffset ->
//        localMetaTiles.keys.map { tileOffset + it }.toSet()
//    })


fun <A> DynamicSet<A>.changes(): Stream<Unit> =
    this.content.values().units()

fun <A> DynamicSet<A>.sample(): Set<A> = volatileContentView.toSet()


fun <K, B> DynamicSet<K>.fuseMap(transform: (K) -> Cell<B>): DynamicSet<B> =
    FuseMapDynamicSet(this, transform)

//fun <K, B> DynamicSet<K>.fuseMapDiff(transform: (K) -> Cell<B>): DynamicSet<B> =
//    DynamicSet.diff(
//        content.switchMap { content ->
//            Cell.traverse(content, transform).map { it.toSet() }
//        }
//    )

//fun <K, V> DynamicSet<K>.associateWith(valueSelector: (K) -> V): DynamicMap<K, V> =
//    DynamicMap.diff(content.map { it.associateWith(valueSelector) })

fun <K, V> DynamicSet<K>.associateWith(tag: String, valueSelector: (K) -> V): DynamicMap<K, V> =
    DynamicSetAssociateWith(this, valueSelector, tag = tag)
        .validated(tag = tag)

//fun <K, V> DynamicSet<K>.associateWithDynamic(valueSelector: (K) -> Cell<V>): DynamicMap<K, V> =
//    DynamicMap.diff(content.switchMap { content ->
//        Cell.traverse(content) { key ->
//            valueSelector(key).map { value -> key to value }
//        }.map { it.toMap() }
//    })

fun <K, V> DynamicSet<K>.associateWithDynamic(tag: String, valueSelector: (K) -> Cell<V>): DynamicMap<K, V> =
    this.associateWith(tag = "$tag/associateWithDynamic/associateWith", valueSelector)
        .fuseValues(tag = "$tag/associateWithDynamic/fuseValues")

//class Ordered<A, K : Comparable<K>>(val value: A, val key: K)
//
//class OrderedDynamic<A, K : Comparable<K>>(val value: A, val key: Cell<K>)


//fun <A, K> DynamicSet<A>.sortedByDynamic(keySelector: (A) -> Cell<K>): DynamicList<A> =
//    TODO()


//fun <A, K : Comparable<K>> DynamicSet<OrderedDynamic<A, K>>.ordered(): DynamicList<A> =
//    TODO()

//fun <A, B, K : Comparable<K>> DynamicSet<A>.fuseMapOrdered(f: (A) -> OrderedDynamic<Cell<B>, K>): DynamicList<B> =
//    this.fuseMap { a ->
//        val od: OrderedDynamic<Cell<B>, K> = f(a)
//        val cb: Cell<B> = od.value
//        cb.map { b ->
//            OrderedDynamic(value = b, key = od.key)
//        }
//    }.ordered()

//fun <A, B, K : Comparable<K>> DynamicSet<A>.blendMapOrdered(
//    f: (A) -> OrderedDynamic<DynamicView<B>, K>,
//): DynamicList<B> {
//    val blendMap = this.blendMap { a ->
//        val od: OrderedDynamic<DynamicView<B>, K> = f(a)
//        val cb: DynamicView<B> = od.value
//        cb.map { b ->
//            OrderedDynamic(value = b, key = od.key)
//        }
//    }
//    return blendMap.ordered()
//}

//val <A> DynamicSet<A>.contentView: Cell<Set<A>>
//    get() = TODO()

fun <A, R> DynamicSet<A>.blendMap(transform: (A) -> DynamicView<R>): DynamicView<Set<R>> =
    DynamicSet.blend(this.map(tag = "blendMap/map", transform))

fun <A, B> DynamicSet<A>.adjust(
    hash: Hash<A>? = null,
    adjustment: Cell<B>,
    combine: (A, B) -> A,
): DynamicSet<A> =
    AdjustDynamicSet(
        through = hash?.let { FastSetFactory(it) } ?: DefaultSetFactory(),
        source = this,
        adjustment = adjustment,
        combine = combine,
        tag = "adjust",
    )
        .validated(sourceTag = "adjust")

fun <A> DynamicSet<A>.memorized(): DynamicSet<A> =
    MemorizedDynamicSet(this)

const val enableSetValidation: Boolean = false

fun <A> DynamicSet<A>.validated(sourceTag: String): DynamicSet<A> =
    if (enableSetValidation) ValidatedDynamicSet(this, sourceTag = sourceTag)
    else this


fun <A> MutableDynamicSet<A>.validatedMutable(tag: String): MutableDynamicSet<A> =
    if (enableSetValidation) MutableValidatedDynamicSet(this, sourceTag = tag)
    else this


abstract class SimpleDynamicSet<A>(
    tag: String,
) : DynamicSet<A>, SimpleObservable<SetChange<A>>(
    tag = tag,
) {
    override val content: Cell<Set<A>>
        get() = RawCell(
            { volatileContentView.toSet() },
            changes.map { volatileContentView.toSet() },
        )


    override val changes: Stream<SetChange<A>>
        get() = Stream.source(this::subscribe, tag = "SimpleDynamicSet.changes")

    override fun toString(): String = "SimpleDynamicSet(name = $name)"
}

data class StaticDynamicSet<A>(
    private val staticContent: Set<A>,
) : DynamicSet<A> {
    override val changes: Stream<SetChange<A>>
        get() = Stream.never()

    override val volatileContentView: Set<A>
        get() = staticContent

    override val content: Cell<Set<A>>
        get() = Cell.constant(staticContent)
}

interface MutableDynamicSet<A> : DynamicSet<A> {
    companion object {
        fun <A> of(
            initialContent: Set<A>,
            tag: String = "MutableDynamicSet.of",
        ): MutableDynamicSet<A> =
            MutableDynamicSetImpl(initialContent)
                .validatedMutable(tag = tag)
    }

    fun add(element: A)
    fun remove(element: A)
}

private class MutableDynamicSetImpl<A>(
    initialContent: Set<A>,
    tag: String = "MutableDynamicSet",
) : SimpleDynamicSet<A>(tag = tag), MutableDynamicSet<A> {
    private val mutableContent: MutableSet<A> = initialContent.toMutableSet()

    override val volatileContentView: Set<A>
        get() = mutableContent

    override fun add(element: A) {
        val wasAdded = mutableContent.add(element)

        if (wasAdded) {
            val change = SetChange(
                added = setOf(element),
                removed = emptySet(),
            )

            notifyListeners(change)
        }
    }

    override fun remove(element: A) {
        val wasThere = mutableContent.remove(element)

        if (wasThere) {
            val change = SetChange(
                added = emptySet(),
                removed = setOf(element),
            )

            notifyListeners(change)
        }
    }
}
