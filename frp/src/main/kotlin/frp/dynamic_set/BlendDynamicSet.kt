package icesword.frp.dynamic_set

import icesword.frp.*

class DynamicSetBlendView<A>(
    private val dynamicViews: DynamicSet<DynamicView<A>>
) : Set<A> {
    override val size: Int
        get() = dynamicViews.volatileContentView.size

    override fun contains(element: A): Boolean =
        dynamicViews.volatileContentView.any { it.view == element }

    override fun containsAll(elements: Collection<A>): Boolean =
        elements.all { contains(it) }

    override fun isEmpty(): Boolean =
        size == 0

    override fun iterator(): Iterator<A> =
        dynamicViews.volatileContentView.map { it.view }.iterator()
}
