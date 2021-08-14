package icesword.editor

import icesword.frp.*
import icesword.geometry.IntVec2
import icesword.tileAtPoint

abstract class Entity(
    initialTileOffset: IntVec2,
) {
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

    private val _tileOffset = MutCell(initialTileOffset)

    val tileOffset: Cell<IntVec2> = _tileOffset

    fun move(
        tileOffsetDelta: Cell<IntVec2>,
        tillStop: Till,
    ) {
        val initialTileOffset = _tileOffset.sample()
        val targetTileOffset = tileOffsetDelta.map { d -> initialTileOffset + d }

        targetTileOffset.reactTill(tillStop) {
            if (_tileOffset.sample() != it) {
                _tileOffset.set(it)
            }
        }
    }
}
