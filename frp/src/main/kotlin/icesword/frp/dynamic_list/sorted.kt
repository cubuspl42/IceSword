package icesword.frp.dynamic_list

import icesword.frp.Cell
import icesword.frp.map


fun <E, R : Comparable<R>> DynamicList<E>.sortedBy(selector: (E) -> R): DynamicList<E> =
    DynamicList.diffIdentified(
        identifiedContent = identifiedContent.map { identifiedContentNow ->
            identifiedContentNow.sortedBy { selector(it.element) }
        },
    )

fun <E, R : Comparable<R>> DynamicList<E>.sortedByDynamic(selector: (E) -> Cell<R>): DynamicList<E> =
    fuseBy { e -> selector(e).map { s -> e to s } }.sortedBy { it.second }.map { it.first }
