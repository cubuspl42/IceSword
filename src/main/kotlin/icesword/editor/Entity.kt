package icesword.editor

import icesword.frp.*
import icesword.geometry.IntVec2

interface EntityTileOffset {
    val tileOffset: Cell<IntVec2>

    fun setTileOffset(newOffset: IntVec2)
}

class SimpleEntityTileOffset(
    initialTileOffset: IntVec2,
) : EntityTileOffset {
    private val _tileOffset = MutCell(initialTileOffset)

    override val tileOffset: Cell<IntVec2>
        get() = _tileOffset

    override fun setTileOffset(newOffset: IntVec2) {
        _tileOffset.set(newOffset)
    }
}

abstract class Entity : EntityTileOffset {
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

//    private val _tileOffset = MutCell(initialTileOffset)
//
//    val tileOffset: Cell<IntVec2> = _tileOffset

    fun move(
        tileOffsetDelta: Cell<IntVec2>,
        tillStop: Till,
    ) {
        val initialTileOffset = tileOffset.sample()
        val targetTileOffset = tileOffsetDelta.map { d -> initialTileOffset + d }

        targetTileOffset.reactTill(tillStop) {
            if (tileOffset.sample() != it) {
                setTileOffset(it)
            }
        }
    }
}
