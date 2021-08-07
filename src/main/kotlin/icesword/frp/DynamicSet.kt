package icesword.frp

interface DynamicSet<A> {
    companion object {
        fun <A> of(content: Set<A>): DynamicSet<A> =
            StaticDynamicSet(content)

        fun <A> diff(content: Cell<Set<A>>): DynamicSet<A> =
            ContentDynamicSet(content)

//        fun <A> union(sets: DynamicSet<Set<A>>): DynamicSet<A> = diff(
//            sets.content.map { it.flatten().toSet() }
//        )

        fun <A> union(sets: DynamicSet<DynamicSet<A>>): DynamicSet<A> = diff(
            sets.content.switchMap { outerSet ->
                Cell.traverse(outerSet) { innerDynSet ->
                    innerDynSet.content
                }.map { setOfSets ->
                    setOfSets.flatten().toSet()
                }
            }
        )

//        fun <A> union(sets: DynamicSet<DynamicSet<A>>): DynamicSet<A> = DynamicSetUnion()
    }

    val content: Cell<Set<A>>

    // May be view, may be copy
    val volatileContentView: Set<A>

    val changes: Stream<SetChange<A>>

    fun containsNow(a: A): Boolean {
        return content.sample().contains(a)
    }
}

//fun <K, V> DynamicMap<K, V>.get(key: K): Cell<V?> =
//    content.map { it[key] }

//fun <A, B> DynamicSet<A>.unionMap(transform: (A) -> Set<B>): DynamicSet<B> =
//    DynamicSet.union(map(transform))

fun <A, B> DynamicSet<A>.unionMapDynamic(transform: (A) -> DynamicSet<B>): DynamicSet<B> =
    DynamicSet.union(map(transform))

fun <A> DynamicSet<A>.trackContent(till: Till): Cell<Set<A>> = content

fun <A, R> DynamicSet<A>.map(transform: (A) -> R): DynamicSet<R> = DynamicSet.diff(
    content.map { it.map(transform).toSet() },
)

fun <A> DynamicSet<A>.changes(): Stream<Unit> =
    this.content.values().units()

fun <A> DynamicSet<A>.sample(): Set<A> = content.sample()

fun <K, V> DynamicSet<K>.associateWith(valueSelector: (K) -> V): DynamicMap<K, V> =
    DynamicMap.diff(content.map { it.associateWith(valueSelector) })

fun <K, V> DynamicSet<K>.associateWithDynamic(valueSelector: (K) -> Cell<V>): DynamicMap<K, V> =
    DynamicMap.diff(content.switchMap { content ->
        Cell.traverse(content) { key ->
            valueSelector(key).map { value -> key to value }
        }.map { it.toMap() }
    })

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

abstract class SimpleDynamicSet<A> : DynamicSet<A>, SimpleObservable<SetChange<A>>() {
//    override val content: Cell<Set<K, V>>
//        get() = TODO("Not yet implemented")

    override val changes: Stream<SetChange<A>>
        get() = Stream.source(this::subscribe)
}

class ContentDynamicSet<A>(
    override val content: Cell<Set<A>>,
) : DynamicSet<A> {
    override val changes: Stream<SetChange<A>>
        get() = TODO("Not yet implemented")

    override val volatileContentView: Set<A>
        get() = content.sample()
}

class StaticDynamicSet<A>(
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
) : DynamicSet<A> {
    companion object {
        fun <A> of(initialContent: Set<A>): MutableDynamicSet<A> =
            MutableDynamicSet(initialContent)
    }

    private val _content = MutCell(initialContent.toSet())

    private val _changes = StreamSink<SetChange<A>>()

    override val volatileContentView: Set<A>
        get() = _content.sample()

    fun add(element: A) {
        val oldContent = _content.sample()

        _content.set(oldContent + element)

        if (!oldContent.contains(element)) {
            _changes.send(
                SetChange(
                    added = setOf(element),
                    removed = emptySet(),
                ),
            )
        }

    }

    override val content: Cell<Set<A>>
        get() = _content

    override val changes: Stream<SetChange<A>>
        get() = _changes
}
