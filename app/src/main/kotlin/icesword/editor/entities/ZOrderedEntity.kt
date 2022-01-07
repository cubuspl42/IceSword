package icesword.editor.entities

import icesword.frp.Cell
import icesword.frp.MutCell

interface ZOrderedEntity {
    companion object {
        const val defaultZOrder: Int = 5000
    }

    val zOrder: Cell<Int>

    fun setZOrder(newZOrder: Int)
}

class SimpleZOrderedEntity(
    initialZOrder: Int,
) : ZOrderedEntity {
    private val _zOrder = MutCell(initialZOrder)

    override val zOrder: Cell<Int>
        get() = _zOrder

    override fun setZOrder(newZOrder: Int) {
        _zOrder.set(newZOrder)
    }
}
