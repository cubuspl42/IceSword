package icesword.frp

import icesword.editor.MetaTile
import icesword.geometry.IntVec2

interface DynamicSet<A> {
    companion object {
        fun <A> of(content: Set<A>): DynamicSet<A> =
            ContentDynamicSet(Cell.constant(content))

        fun <A> diff(content: Cell<Set<A>>): DynamicSet<A> =
            ContentDynamicSet(content)

        fun <A> union(sets: DynamicSet<Set<A>>): DynamicSet<A> = diff(
            sets.content.map { it.flatten().toSet() }
        )

        fun <A> union(sets: DynamicSet<DynamicSet<A>>): DynamicSet<A> = diff(
            sets.content.switchMap { outerSet ->
                Cell.traverse(outerSet) { innerDynSet ->
                    innerDynSet.content
                }.map { setOfSets ->
                    setOfSets.flatten().toSet()
                }
            }
        )
    }

    val content: Cell<Set<A>>
}

fun <K, V> DynamicMap<K, V>.get(key: K): Cell<V?> =
    content.map { it[key] }

fun <A, B> DynamicSet<A>.unionMap(transform: (A) -> Set<B>): DynamicSet<B> =
    DynamicSet.union(map(transform))

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


class ContentDynamicSet<A>(
    override val content: Cell<Set<A>>,
) : DynamicSet<A>

class MutableDynamicSet<A>(
    initialContent: Set<A>,
) : DynamicSet<A> {
    companion object {
        fun <A> of(content: Set<A>): MutableDynamicSet<A> =
            MutableDynamicSet(content)
    }

    private val _content = MutCell(initialContent.toSet())

    fun add(element: A) {
        val oldContent = _content.sample()
        _content.set(oldContent + element)
    }

    override val content: Cell<Set<A>>
        get() = _content
}
