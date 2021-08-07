package icesword.editor

import icesword.frp.Cell
import icesword.frp.MutCell

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
}
