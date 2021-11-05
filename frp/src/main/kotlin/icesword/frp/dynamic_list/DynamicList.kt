package icesword.frp.dynamic_list

import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.DynamicSet
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.map

interface DynamicList<out E> {
    val content: Cell<List<E>>

    // May be view, may be copy
    val volatileContentView: List<E>
        get() = content.sample()
}

class ContentDynamicList<out E>(
    override val content: Cell<List<E>>,
) : DynamicList<E> {
    override val volatileContentView: List<E>
        get() = content.sample()
}

val <E> DynamicList<E>.size: Cell<Int>
    get() = content.map { it.size }

fun <E> DynamicList<E>.sampleContent(): List<E> =
    volatileContentView.toList()

fun <E : Any> DynamicList<E>.firstOrNull(): Cell<E?> =
    this.content.map { it.firstOrNull() }

fun <E : Any> DynamicList<E>.drop(n: Int): DynamicList<E> =
    ContentDynamicList(content = this.content.map { it.drop(n) })

fun <E> DynamicList<E>.lastNow(): E =
    volatileContentView.last()

fun <A, R> DynamicList<A>.mapTillRemoved(
    tillAbort: Till,
    transform: (element: A, tillRemoved: Till) -> R,
): DynamicList<R> = ContentDynamicList(
    content = this.content.map { content ->
        content.map { transform(it, tillAbort) }
    }
)
