package icesword.frp.dynamic_list

import icesword.frp.Till
import icesword.frp.filter
import icesword.frp.tillNext

fun <A, R> DynamicList<A>.mapTillRemoved(
    tillAbort: Till,
    transform: (element: A, tillRemoved: Till) -> R,
): DynamicList<R> = this.withIdentity().processOf(tillAbort) { identifiedElement ->
    val tillRemoved = this.changes.filter { listChange ->
        listChange.removedElements.any {
            it.identity == identifiedElement.identity
        }
    }.tillNext(orTill = tillAbort)

    transform(identifiedElement.element, tillRemoved)
}
