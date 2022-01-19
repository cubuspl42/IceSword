package icesword.frp.dynamic_list

import icesword.frp.BehaviorComputation
import icesword.frp.Cell
import icesword.frp.RawCell
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.mapTill

@BehaviorComputation
fun <E, R> DynamicList<E>.processOf(
    tillFreeze: Till,
    @BehaviorComputation
    transform: (element: E) -> R,
): DynamicList<R> =
    DynamicList.store(
        initialIdentifiedContent = {
            volatileIdentifiedContentView.map {
                it.map(transform)
            }
        },
        buildChanges = { contentView ->
            changes.map { change ->
                change.processOf(contentView(), transform)
            }
        },
        tillFreeze = tillFreeze,
    )

private fun <E, R> ListChange<E>.processOf(
    contentView: List<DynamicList.IdentifiedElement<R>>,
    transform: (E) -> R,
): ListChange<R> =
    ListChange(
        pushIns = pushIns.map { pushIn ->
            pushIn.mapIdentified { identifiedElement ->
                if (addedElements.contains(identifiedElement)) transform(identifiedElement.element)
                else {
                    val pullOut = pullOuts.find { it.pulledOutElement == identifiedElement }
                        ?: throw RuntimeException("Can't find a pull-out for pushed-in but not added element: $identifiedElement")
                    contentView[pullOut.indexBefore].element
                }
            }
        }.toSet(),
        pullOuts = pullOuts.map { pullOut ->
            pullOut.map { contentView[pullOut.indexBefore].element }
        }.toSet(),
    )

private fun <E, R> ListChange.PushIn<E>.mapIdentified(
    transform: (DynamicList.IdentifiedElement<E>) -> R,
): ListChange.PushIn<R> =
    ListChange.PushIn(
        indexBefore = indexBefore,
        indexAfter = indexAfter,
        pushedInElements = pushedInElements.map { identifiedElement ->
            identifiedElement.map { transform(identifiedElement) }
        },
    )

private fun <E, R> ListChange.PullOut<E>.map(transform: (E) -> R): ListChange.PullOut<R> =
    ListChange.PullOut(
        indexBefore = indexBefore,
        pulledOutElement = pulledOutElement.map(transform),
    )
