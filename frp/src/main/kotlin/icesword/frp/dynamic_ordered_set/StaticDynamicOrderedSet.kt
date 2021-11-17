package icesword.frp.dynamic_ordered_set

import icesword.frp.Stream
import icesword.frp.Subscription

class StaticDynamicOrderedSet<A>(
    private val content: List<A>,
) : DynamicOrderedSet<A> {
    override val volatileContentView: List<A>
        get() = content

    override val changes: Stream<OrderedSetChange<A>>
        get() = Stream.never()
}