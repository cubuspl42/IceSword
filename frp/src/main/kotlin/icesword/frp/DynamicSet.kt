package icesword.frp

import frpjs.Hash
import icesword.collections.DefaultSetFactory
import icesword.collections.FastSetFactory
import icesword.frp.dynamic_list.ContentDynamicList
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_map.DynamicSetAssociateWith
import icesword.frp.dynamic_set.*

interface DynamicSet<out A> {
    companion object {
        fun <A> of(content: Set<A>): DynamicSet<A> =
            StaticDynamicSet(content)

        fun <A> ofSingle(element: Cell<A?>): DynamicSet<A> =
            diff(
                content = element.map { e -> e?.let { setOf(it) } ?: emptySet() },
            )

        fun <A> diff(content: Cell<Set<A>>, tag: String = "diff"): DynamicSet<A> =
            DiffDynamicSet(
                inputContent = content,
                identity = SimpleObservable.Identity.build(tag),
            ).validated("$tag-validated")

        fun <A> diff(content: Cell<DynamicSet<A>>, tag: String = "diff-dynamic"): DynamicSet<A> =
            DynamicDiffDynamicSet(
                inputContent = content,
                identity = SimpleObservable.Identity.build(tag = tag),
            )

        fun <A> union(sets: DynamicSet<DynamicSet<A>>): DynamicSet<A> =
            DynamicSetUnion(sets)
                .validated("union")

        fun <A> union2(set1: DynamicSet<A>, set2: DynamicSet<A>): DynamicSet<A> =
            DynamicSet.union(
                DynamicSet.of(setOf(
                    set1,
                    set2,
                ))
            )

        fun <A> merge(
            streams: DynamicSet<Stream<A>>,
        ): Stream<A> = streams.content.divertMap {
            Stream.merge(it)
        }

        // Probably, like `fuse`, it's semantically meaningless, because `DynamicView` isn't comparable
        fun <A> blend(dynamicViews: DynamicSet<DynamicView<A>>): DynamicView<Set<A>> =
            DynamicView(
                updates = merge(dynamicViews.distinctMap(tag = "blend") { it.updates }),
                view = DynamicSetBlendView(dynamicViews),
            )

        fun <A> empty(): DynamicSet<A> = of(emptySet())
    }

    val content: Cell<Set<A>>

    // May be view, may be copy
    val volatileContentView: Set<A>

    val changes: Stream<SetChange<A>>

    // Elements of this set in order that is used by the underlying data structure. It is not meaningful and can be
    // considered arbitrary. This is appropriate when one wants to convert a set to a list and doesn't care about the
    // actual order.
    val internalOrder: DynamicList<A>
        get() = ContentDynamicList(
            content = this.content.map { it.toList() },
        )

    fun containsNow(a: @UnsafeVariance A): Boolean {
        return volatileContentView.contains(a)
    }
}

fun <A> staticSetOf(vararg elements: A): DynamicSet<A> =
    DynamicSet.of(setOf(*elements))

fun <A, B> DynamicSet<A>.unionMap(
    tag: String,
    transform: (A) -> Set<B>,
): DynamicSet<B> =
    this.unionMapDynamic(tag = "unionMap") { DynamicSet.of(transform(it)) }

fun <A, B> DynamicSet<A>.unionMapDynamic(
    tag: String,
    transform: (A) -> DynamicSet<B>,
): DynamicSet<B> =
    DynamicSet.union(distinctMap(tag = "$tag/map", transform))

fun <A> DynamicSet<A>.unionWith(other: DynamicSet<A>): DynamicSet<A> =
    DynamicSet.union(
        DynamicSet.of(
            setOf(this, other)
        )
    )

fun <A> DynamicSet<A>.trackContent(till: Till): Cell<Set<A>> = content

fun <A, R> DynamicSet<A>.distinctMap(
    tag: String = "map",
    transform: (A) -> R,
): DynamicSet<R> =
    DistinctMapDynamicSet(
        // TODO: This validation shouldn't be needed, source should validate itself
        source = this.validated("$tag-this"),
        transform = transform,
        identity = SimpleObservable.Identity.build(tag = tag),
    ).validated(tag)

fun <A : Any> DynamicSet<A?>.filterNotNull(): DynamicSet<A> =
    this.filter { it != null }.distinctMap(tag = "filterNotNull") { it!! }

fun <A, R : Any> DynamicSet<A>.distinctMapNotNull(
    tag: String = "distinctMapNotNull",
    transform: (A) -> R?,
): DynamicSet<R> =
    this.distinctMap(tag = tag) { transform(it) }.filterNotNull()

fun <A, R> DynamicSet<A>.mapTillRemoved(
    tillAbort: Till,
    transform: (element: A, tillRemoved: Till) -> R,
): DynamicSet<R> =
    MapTillRemovedDynamicSet(
        source = this,
        transform = transform,
        tillAbort = tillAbort,
    ).validated("mapTillNext")

fun <A> DynamicSet<A>.pin(
    till: Till,
): DynamicSet<A> {
    changes.reactTill(till) { }
    return this
}

fun <A> DynamicSet<A>.sample(): Set<A> = volatileContentView.toSet()

fun <K, B> DynamicSet<K>.fuseMap(transform: (K) -> Cell<B>): DynamicSet<B> =
    FuseMapDynamicSet(this, transform)

fun <K, V> DynamicSet<K>.associateWith(tag: String, valueSelector: (K) -> V): DynamicMap<K, V> =
    DynamicSetAssociateWith(
        source = this,
        valueSelector = valueSelector,
        identity = SimpleObservable.Identity.build("associateWith"),
    ).validated(tag = tag)

fun <K, V> DynamicSet<K>.associateWithDynamic(tag: String, valueSelector: (K) -> Cell<V>): DynamicMap<K, V> =
    this.associateWith(tag = "$tag/associateWithDynamic/associateWith", valueSelector)
        .fuseValues(tag = "$tag/associateWithDynamic/fuseValues")

fun <A, R> DynamicSet<A>.blendMap(transform: (A) -> DynamicView<R>): DynamicView<Set<R>> =
    DynamicSet.blend(this.distinctMap(tag = "blendMap/map", transform))

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
        identity = SimpleObservable.Identity.build(tag = "adjust"),
    )
        .validated(sourceTag = "adjust")

fun <A> DynamicSet<A>.memorized(): DynamicSet<A> =
    MemorizedDynamicSet(this).validated("memorized")

fun <A> DynamicSet<A>.contains(element: A): Cell<Boolean> =
    this.content.map { it.contains(element) }

fun <A> DynamicSet<A>.filter(tag: String = "filter", test: (A) -> Boolean): DynamicSet<A> =
    FilterDynamicSet(
        source = this,
        test = test,
        identity = SimpleObservable.Identity.build(tag = tag),
    ).validated(sourceTag = tag)

fun <A> DynamicSet<A>.filterDynamic(test: (A) -> Cell<Boolean>): DynamicSet<A> =
    this.fuseMap { test(it).map { t -> Pair(it, t) } }
        .filter { it.second }
        .distinctMap(tag = "") { it.first }

fun <A> DynamicSet<A>.filterDiffed(test: Cell<(A) -> Boolean>): DynamicSet<A> =
    DynamicSet.diff(
        test.map { testNow -> this.filter(test = testNow) },
    )

inline fun <A, reified B : A> DynamicSet<A>.filterType(): DynamicSet<B> =
    this.distinctMapNotNull { it as? B }

const val enableSetValidation: Boolean = true

fun <A> DynamicSet<A>.validated(sourceTag: String): DynamicSet<A> =
    if (enableSetValidation) ValidatedDynamicSet(this, sourceTag = sourceTag)
    else this

fun <A> MutableDynamicSet<A>.validatedMutable(tag: String): MutableDynamicSet<A> =
    if (enableSetValidation) MutableValidatedDynamicSet(this, sourceTag = tag)
    else this

abstract class SimpleDynamicSet<A>(
    identity: Identity,
) : DynamicSet<A>, SimpleObservable<SetChange<A>>(
    identity = identity,
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

    fun removeAll(elements: Set<A>)
}

private class MutableDynamicSetImpl<A>(
    initialContent: Set<A>,
    tag: String = "MutableDynamicSet",
) : SimpleDynamicSet<A>(
    identity = Identity.build(
        tag = "MutableDynamicSet",
    ),
), MutableDynamicSet<A> {
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

    override fun removeAll(elements: Set<A>) {
        val allWereThere = elements.all { mutableContent.contains(it) }

        if (!allWereThere) {
            throw IllegalArgumentException("Some elements to remove aren't present in the set")
        }

        val anyWasThere = mutableContent.removeAll(elements)

        if (anyWasThere) {
            val change = SetChange(
                added = emptySet(),
                removed = elements,
            )

            notifyListeners(change)
        }
    }
}
