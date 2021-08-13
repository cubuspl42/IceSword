package icesword.editor

import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.geometry.IntVec2
import icesword.tileAtPoint

abstract class Entity {
    private val _isSelected = MutCell(false)

    val isSelected: Cell<Boolean>
        get() = _isSelected

    fun select() {
        _isSelected.set(true)
    }

    fun unselect() {
        _isSelected.set(false)
    }

    abstract fun isSelectableAt(worldPoint: IntVec2): Boolean
}
