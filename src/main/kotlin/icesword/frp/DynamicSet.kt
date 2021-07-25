package icesword.frp

interface DynamicSet<A> {
    companion object {
        fun <A> of(content: Set<A>): DynamicSet<A> =
            ContentDynamicSet(Cell.constant(content))

        fun <A> diff(content: Cell<Set<A>>): DynamicSet<A> =
            ContentDynamicSet(content)

        fun <A> union(sets: DynamicSet<Set<A>>): DynamicSet<A> = diff(
            sets.content.map { it.flatten().toSet() }
        )
    }

    val content: Cell<Set<A>>
}

fun <A> DynamicSet<A>.unionMap(transform: (A) -> Set<A>): DynamicSet<A> =
    DynamicSet.union(map(transform))

fun <A> DynamicSet<A>.trackContent(till: Till): Cell<Set<A>> = content

fun <A, R> DynamicSet<A>.map(transform: (A) -> R): DynamicSet<R> = DynamicSet.diff(
    content.map { it.map(transform).toSet() },
)

fun <A> DynamicSet<A>.changes(): Stream<Unit> =
    this.content.values().units()

fun <A> DynamicSet<A>.sample(): Set<A> = content.sample()

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
