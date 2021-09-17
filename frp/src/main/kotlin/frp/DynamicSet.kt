package icesword.frp

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
                updates = merge(dynamicViews.map { it.updates }),
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

//fun <A, B> DynamicSet<A>.unionMap(transform: (A) -> Set<B>): DynamicSet<B> =
//    DynamicSet.union(map(transform))

fun <A, B> DynamicSet<A>.unionMapDynamic(transform: (A) -> DynamicSet<B>): DynamicSet<B> =
    DynamicSet.union(map(transform))

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

fun <A, R> DynamicSet<A>.map(transform: (A) -> R): DynamicSet<R> =
    MapDynamicSet(this.validated("map-this"), transform)
        .validated("map")

//fun <A, R> DynamicSet<A>.mapDynamic(transform: (A) -> Cell<R>): DynamicSet<R> =
//    DynamicSet.diff(tileOffset.map { tileOffset ->
//        localMetaTiles.keys.map { tileOffset + it }.toSet()
//    })


fun <A> DynamicSet<A>.changes(): Stream<Unit> =
    this.content.values().units()

fun <A> DynamicSet<A>.sample(): Set<A> = content.sample()


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
    DynamicSet.blend(this.map(transform))

//fun <A, B> DynamicSet<A>.mapNotNull(transform: (A) -> B?): DynamicSet<B> {
//
//}
//
//fun <A> DynamicSet<A?>.filterNotNull(): DynamicSet<A> {
//
//}
//
//
//fun <A, B> DynamicSet<A>.fuseMapNotNull(transform: (A) -> Cell<B>): Any {
//
//}

const val enableSetValidation: Boolean = false

fun <A> DynamicSet<A>.validated(tag: String): DynamicSet<A> =
    if (enableSetValidation) ValidatedDynamicSet(this, tag = tag)
    else this


abstract class SimpleDynamicSet<A>(
    tag: String,
) : DynamicSet<A>, SimpleObservable<SetChange<A>>(
    tag = tag,
) {
//    override val content: Cell<Set<K, V>>
//        get() = TODO("Not yet implemented")

    override val changes: Stream<SetChange<A>>
        get() = Stream.source(this::subscribe, tag = "SimpleDynamicSet.changes")

    override fun toString(): String = "SimpleDynamicSet(name = $name)"
}

//class ContentDynamicSet<A>(
//    override val content: Cell<Set<A>>,
//) : DynamicSet<A> {
//    override val changes: Stream<SetChange<A>>
//        get() = TODO("Not yet implemented")
//
//    override val volatileContentView: Set<A>
//        get() = content.sample()
//}

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

class MutableDynamicSet<A>(
    initialContent: Set<A>,
) : SimpleDynamicSet<A>(tag = "MutableDynamicSet") {
    companion object {
        fun <A> of(initialContent: Set<A>): MutableDynamicSet<A> =
            MutableDynamicSet(initialContent)
    }

//    private val _content = MutCell(initialContent.toSet())


    private var mutableContent: Set<A> = initialContent

    override val content: Cell<Set<A>>
        get() = RawCell(
            { mutableContent.toSet() },
            changes.map { mutableContent.toSet() },
        )

    override val volatileContentView: Set<A>
        get() = mutableContent

    fun add(element: A) {
        val oldContent = mutableContent

        mutableContent = oldContent + element

        if (!oldContent.contains(element)) {
            val change = SetChange(
                added = setOf(element),
                removed = emptySet(),
            )

            if (change.added.intersect(oldContent).isNotEmpty()) {
                throw IllegalStateException("MutableDynamicSet: change.added.intersect")
            }

            notifyListeners(change)
        }

    }

    fun remove(element: A) {
        val oldContent = mutableContent

        if (oldContent.contains(element)) {

            mutableContent = oldContent + element

            val change = SetChange(
                added = emptySet(),
                removed = setOf(element),
            )

            notifyListeners(change)
        }
    }

//    override val content: Cell<Set<A>>
//        get() = _content
}
