package icesword.frp.dynamic_list

import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.DynamicSet
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.divertMap
import icesword.frp.hold
import icesword.frp.map
import icesword.frp.mapNotNull
import icesword.frp.mapTillNext
import icesword.frp.switchMap
import icesword.frp.units
import icesword.frp.values

interface DynamicList<out E> {
    companion object {
        fun <E> of(list: List<E>): DynamicList<E> =
            ContentDynamicList(content = constant(list))

        fun <E> ofSingle(element: Cell<E>): DynamicList<E> =
            ContentDynamicList(content = element.map(::listOf))

        fun <E> merge(list: DynamicList<Stream<E>>): Stream<E> =
            list.content.divertMap { Stream.merge(it) }

        fun <E> diff(content: Cell<List<E>>): DynamicList<E> =
            ContentDynamicList(content = content)

        fun <E> diff(content: Cell<DynamicList<E>>): DynamicList<E> =
            ContentDynamicList(content = content.switchMap { it.content })

        fun <E> fuse(content: DynamicList<Cell<E>>): DynamicList<E> =
            ContentDynamicList(
                content = content.content.switchMap { cells ->
                    Cell.traverse(cells) { it }
                },
            )

        fun <E> fuse(content: List<Cell<E>>): DynamicList<E> =
            ContentDynamicList(
                content = Cell.traverse(content) { it },
            )

        fun <E> empty(): DynamicList<E> =
            ContentDynamicList(constant(emptyList()))
    }

    val content: Cell<List<E>>

    // May be view, may be copy
    val volatileContentView: List<E>
        get() = content.sample()
}

fun <E> staticListOf(vararg elements: E): DynamicList<E> =
    DynamicList.of(elements.toList())

class ContentDynamicList<out E>(
    override val content: Cell<List<E>>,
) : DynamicList<E> {
    override val volatileContentView: List<E>
        get() = content.sample()
}

fun <E> DynamicList<E>.changesUnits(): Stream<Unit> =
    content.values().units()

val <E> DynamicList<E>.size: Cell<Int>
    get() = content.map { it.size }

fun <E> DynamicList<E>.sampleContent(): List<E> =
    volatileContentView.toList()

fun <E : Any> DynamicList<E>.first(): Cell<E> =
    this.content.map { it.first() }

fun <E : Any> DynamicList<E>.last(): Cell<E> =
    this.content.map { it.last() }

fun <E : Any> DynamicList<E>.firstOrNull(): Cell<E?> =
    this.content.map { it.firstOrNull() }

fun <E> DynamicList<E>.firstOrNull(predicate: (E) -> Boolean): Cell<E?> =
    this.content.map { it.firstOrNull(predicate) }

fun <E : Any> DynamicList<E>.firstOrNullDynamic(predicate: (E) -> Cell<Boolean>): Cell<E?> =
    this.fuseBy { element ->
        predicate(element).map { flag -> element.takeIf { flag } }
    }.filterNotNull().firstOrNull()

fun <E> DynamicList<Cell<E>>.fuse(): DynamicList<E> =
    DynamicList.fuse(this)

fun <E, R> DynamicList<E>.fuseBy(transform: (E) -> Cell<R>): DynamicList<R> =
    this.map(transform).fuse()

fun <A : Any> DynamicList<A?>.filterNotNull(): DynamicList<A> =
    ContentDynamicList(content = this.content.map { it.filterNotNull() })

fun <E : Any> DynamicList<Cell<E?>>.fuseNotNull(): DynamicList<E> =
    this.fuse().filterNotNull()

fun <E : Any> DynamicList<E>.drop(n: Int): DynamicList<E> =
    ContentDynamicList(content = this.content.map { it.drop(n) })

fun <E> DynamicList<E>.indexOf(element: E): Cell<Int?> =
    content.map { content ->
        val i = content.indexOf(element)
        if (i >= 0) i else null
    }

fun <E> DynamicList<E>.get(index: Int, till: Till): Cell<E> {
    val steps = content.values().mapNotNull { it.getOrNull(index) }
    val initialValue = this.volatileContentView[index]
    return steps.hold(initialValue, till)
}

fun <E> DynamicList<E>.getOrNull(index: Int): Cell<E?> =
    content.map { content -> content.getOrNull(index) }

fun <E> DynamicList<E>.withAppended(element: Cell<E>): DynamicList<E> =
    ContentDynamicList(
        content = Cell.map2(
            content,
            element,
        ) { content, element ->
            content + element
        },
    )

fun <E> DynamicList<E>.lastNow(): E =
    volatileContentView.last()

fun <E> DynamicList<E>.toDynamicSet(): DynamicSet<E> =
    DynamicSet.diff(this.content.map { it.toSet() })

fun <E, R> DynamicList<E>.map(
    transform: (element: E) -> R,
): DynamicList<R> = ContentDynamicList(
    content = this.content.map { it.map(transform) },
)

fun <E, R> DynamicList<E>.mapIndexed(
    transform: (index: Int, element: E) -> R,
): DynamicList<R> = ContentDynamicList(
    content = this.content.map { it.mapIndexed(transform) },
)

fun <E, R> DynamicList<E>.mapIndexedDynamic(
    till: Till,
    transform: (index: Int, element: Cell<E>) -> R,
): DynamicList<R> = ContentDynamicList(
    content = this.size.map { size ->
        (0 until size).map { index -> transform(index, this.get(index, till)) }
    },
)

fun <E, R : Any> DynamicList<E>.mapNotNull(
    transform: (element: E) -> R?,
): DynamicList<R> = ContentDynamicList(
    content = this.content.map { it.mapNotNull(transform) },
)

fun <E, R> DynamicList<E>.mergeBy(
    transform: (element: E) -> Stream<R>,
): Stream<R> = DynamicList.merge(this.map(transform))

fun <A, R> DynamicList<A>.mapTillRemoved(
    tillAbort: Till,
    transform: (element: A, tillRemoved: Till) -> R,
): DynamicList<R> = ContentDynamicList(
    content = this.content.mapTillNext(tillAbort) { content, tillNext ->
        content.map { transform(it, tillNext) }
    },
)

fun <E, R> DynamicList<E>.zipWithNext(
    transform: (a: E, b: E) -> R,
): DynamicList<R> =
    ContentDynamicList(
        content = this.content.map { content ->
            content.zipWithNext(transform)
        }
    )

fun <E> DynamicList<E>.toSet(): DynamicSet<E> =
    DynamicSet.diff(
        content = this.content.map { it.toSet() },
    )
