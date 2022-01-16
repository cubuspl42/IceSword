package icesword.frp.dynamic_list

import icesword.frp.Cell
import icesword.frp.DynamicSet
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.dynamic_list.DynamicList.IdentifiedElement
import icesword.frp.filter
import icesword.frp.hold
import icesword.frp.map
import icesword.frp.mapNotNull
import icesword.frp.mapTillNext
import icesword.frp.tillNext
import icesword.frp.units
import icesword.frp.values

fun <E> DynamicList<E>.changesUnits(): Stream<Unit> =
    content.values().units()

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

fun <E : Any> DynamicList<E?>.firstNotNullOrNull(): Cell<E?> = filterNotNull().firstOrNull()
fun <E> DynamicList<Cell<E>>.fuse(): DynamicList<E> =
    DynamicList.fuse(this)

fun <E, R> DynamicList<E>.fuseBy(transform: (E) -> Cell<R>): DynamicList<R> =
    this.map(transform).fuse()

fun <A : Any> DynamicList<A?>.filterNotNull(): DynamicList<A> =
    DynamicList.Companion.diff(content = this.content.map { it.filterNotNull() })

fun <E : Any> DynamicList<Cell<E?>>.fuseNotNull(): DynamicList<E> =
    this.fuse().filterNotNull()

fun <E> DynamicList<E>.concatWith(other: DynamicList<E>): DynamicList<E> =
    DynamicList.concat(listOf(this, other))

fun <E : Any> DynamicList<E>.drop(n: Int): DynamicList<E> =
    DynamicList.Companion.diff(content = this.content.map { it.drop(n) })

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
    DynamicList.diff(
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
    DynamicSet.Companion.diff(this.content.map { it.toSet() })

fun <E> DynamicList<E>.withIndex(): DynamicList<IndexedValue<E>> = DynamicList.Companion.diff(
    content = this.content.map { it.withIndex().toList() },
)

fun <E, R> DynamicList<E>.mapIndexed(
    transform: (index: Int, element: E) -> R,
): DynamicList<R> = DynamicList.Companion.diff(
    content = this.content.map { it.mapIndexed(transform) },
)

fun <E, R> DynamicList<E>.mapIndexedDynamic(
    till: Till,
    transform: (index: Int, element: Cell<E>) -> R,
): DynamicList<R> = DynamicList.Companion.diff(
    content = this.size.mapTillNext(till) { size, tillNext ->
        (0 until size).map { index -> transform(index, this.get(index, tillNext)) }
    },
)

fun <E, R : Any> DynamicList<E>.mapNotNull(
    transform: (element: E) -> R?,
): DynamicList<R> = DynamicList.Companion.diff(
    content = this.content.map { it.mapNotNull(transform) },
)

fun <E, R> DynamicList<E>.mergeBy(
    transform: (element: E) -> Stream<R>,
): Stream<R> = DynamicList.merge(this.map(transform))

fun <A, R> DynamicList<A>.mapTillRemoved(
    tillAbort: Till,
    transform: (element: A, tillRemoved: Till) -> R,
): DynamicList<R> = this.transform(tillAbort) { element, identity ->
    val tillRemoved = this.changes.filter { listChange ->
        listChange.removed.any {
            it.removedElement.identity == identity
        }
    }.tillNext(orTill = tillAbort)

    transform(element, tillRemoved)
}


fun <A, R> DynamicList<A>.transform(
    tillAbort: Till,
    transform: (element: A, identity: DynamicList.ElementIdentity) -> R,
): DynamicList<R> {
    TODO()
}


fun <A, R> DynamicList<A>.mapTillRemovedIndexed(
    tillAbort: Till,
    transform: (index: Int, element: A, tillRemoved: Till) -> R,
): DynamicList<R> = this.withIndex().mapTillRemoved(tillAbort) { indexedElement, tillRemoved ->
    transform(indexedElement.index, indexedElement.value, tillRemoved)
}

fun <E, R> DynamicList<E>.zipWithNext(
    transform: (a: E, b: E) -> R,
): DynamicList<R> =
    DynamicList.Companion.diff(
        content = this.content.map { content ->
            content.zipWithNext(transform)
        }
    )

fun <E> DynamicList<E>.toSet(): DynamicSet<E> =
    DynamicSet.Companion.diff(
        content = this.content.map { it.toSet() },
    )

fun <S, T : S> DynamicList<T>.reduce(operation: (acc: S, T) -> S): Cell<S> =
    this.content.map { content -> content.reduce(operation) }
