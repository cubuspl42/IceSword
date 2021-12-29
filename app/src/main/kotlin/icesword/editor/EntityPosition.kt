package icesword.editor

import icesword.TILE_SIZE
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.reactTill
import icesword.geometry.IntVec2
import icesword.tileTopLeftCorner

interface EntityPosition {
    val position: Cell<IntVec2>

    fun setPosition(newPosition: IntVec2)
}

fun EntityPosition.move(
    positionDelta: Cell<IntVec2>,
    tillStop: Till,
) {
    val initialPosition = position.sample()
    val targetPosition = positionDelta.map { d -> initialPosition + d }

    targetPosition.reactTill(tillStop) {
        if (position.sample() != it) {
            setPosition(it)
        }
    }
}

class EntityPixelPosition(
    initialPosition: IntVec2,
) : EntityPosition {
    private val _position = MutCell(initialPosition)

    override val position: Cell<IntVec2>
        get() = _position

    override fun setPosition(newPosition: IntVec2) {
        _position.set(newPosition)
    }
}

abstract class EntityTilePosition : EntityPosition {
    abstract val tileOffset: Cell<IntVec2>

    abstract fun setTileOffset(tileOffset: IntVec2)

    final override val position: Cell<IntVec2> by lazy {
        tileOffset.map { tileTopLeftCorner(it) }
    }

    final override fun setPosition(newPosition: IntVec2) {
        setTileOffset(newPosition.divRound(TILE_SIZE))
    }
}

class SimpleEntityTilePosition(
    initialTileOffset: IntVec2,
) : EntityTilePosition() {
    private val _tileOffset = MutCell(initialTileOffset)

    override val tileOffset: Cell<IntVec2>
        get() = _tileOffset

    override fun setTileOffset(newTileOffset: IntVec2) {
        _tileOffset.set(newTileOffset)
    }
}
