package icesword.editor

import icesword.frp.Cell
import icesword.frp.MutCell

abstract class Entity {
    open val isSelectedInitial: Boolean = false

    val isSelected: Cell<Boolean> by lazy { Cell.constant(isSelectedInitial) }
}
