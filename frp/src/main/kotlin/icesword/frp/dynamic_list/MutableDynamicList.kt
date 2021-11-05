package icesword.frp.dynamic_list

import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.update
import icesword.utils.updated

class MutableDynamicList<A>(
    initialContent: List<A>,
) : DynamicList<A> {
    private val mutableContent = MutCell(initialContent.toList())

    override val content: Cell<List<A>>
        get() = mutableContent

    override val volatileContentView: List<A>
        get() = mutableContent.sample()

    fun set(index: Int, element: A) {
        mutableContent.update { it.updated(index, element) }
    }

    fun add(element: A) {
        mutableContent.update { it + element }
    }

    fun remove(element: A) {
        mutableContent.update { content -> content.filter { it != element } }
    }
}