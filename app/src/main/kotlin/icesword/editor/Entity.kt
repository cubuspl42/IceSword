package icesword.editor

import icesword.TILE_SIZE
import icesword.frp.*
import icesword.geometry.IntVec2
import icesword.tileTopLeftCorner

interface EntityTileOffset {
    val tileOffset: Cell<IntVec2>

    val position: Cell<IntVec2>

    fun setPosition(newPosition: IntVec2)
}

class SimpleEntityTileOffset(
    initialTileOffset: IntVec2,
) : EntityTileOffset {
    private val _tileOffset = MutCell(initialTileOffset)

    override val tileOffset: Cell<IntVec2>
        get() = _tileOffset

    override val position: Cell<IntVec2> by lazy {
        tileOffset.map { tileTopLeftCorner(it) }
    }

    override fun setPosition(newPosition: IntVec2) {
        _tileOffset.set(newPosition.divRound(TILE_SIZE))
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
        positionDelta: Cell<IntVec2>,
        tillStop: Till,
    ) {
        println("Starting to move entity...")


        val initialPosition = position.sample()
        val targetPosition = positionDelta.map { d -> initialPosition + d }

        targetPosition.reactTill(tillStop) {
//            println("Setting position: $it")

            if (position.sample() != it) {
                setPosition(it)
            }
        }
    }
}
