package icesword.frp.dynamic_list

import icesword.frp.Cell
import icesword.frp.IndexedIdentity
import icesword.frp.NestedIdentity
import icesword.frp.map
import icesword.frp.switchMap


fun <E> DynamicList.Companion.concat(lists: Iterable<DynamicList<E>>): DynamicList<E> =
    concat(of(lists.toList()))

fun <E> DynamicList.Companion.concat(lists: DynamicList<DynamicList<E>>): DynamicList<E> {
    val identifiedContent = lists.identifiedContent.switchMap { innerListsNow ->
        Cell.traverse(innerListsNow) { identifiedInnerList ->
            val innerList = identifiedInnerList.element

            innerList.identifiedContent.map { identifiedContentNow ->
                identifiedContentNow.map { identifiedElement ->
                    DynamicList.IdentifiedElement(
                        element = identifiedElement.element,
                        identity = NestedIdentity(
                            outerIdentity = identifiedInnerList.identity,
                            innerIdentity = identifiedElement.identity,
                        )
                    )
                }
            }
        }.map { it.flatten() }
    }

    return DynamicList.diffIdentified(
        identifiedContent = identifiedContent,
    )
}

fun <E> DynamicList.Companion.concat(vararg lists: DynamicList<E>): DynamicList<E> =
    concat(lists.toList())

fun <E> DynamicList<E>.concatWith(other: DynamicList<E>): DynamicList<E> =
    DynamicList.concat(listOf(this, other))

fun <E, R> DynamicList<E>.concatOf(transform: (E) -> DynamicList<R>): DynamicList<R> =
    DynamicList.concat(this.map(transform))
