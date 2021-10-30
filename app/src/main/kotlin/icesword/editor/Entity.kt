package icesword.editor

import icesword.TILE_SIZE
import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.tileTopLeftCorner

//sealed interface PositionedEntity {
//    val pixelOffset: Cell<IntVec2>
//
//    interface PixelAlignedEntity : PositionedEntity
//
//    interface TileAlignedEntity : PositionedEntity {
//        val tileOffset: Cell<IntVec2>
//    }
//}

interface EntityPosition {
    val position: Cell<IntVec2>

    fun setPosition(newPosition: IntVec2)
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

class EntityTilePosition(
    initialTileOffset: IntVec2,
) : EntityPosition {
    private val _tileOffset = MutCell(initialTileOffset)

    val tileOffset: Cell<IntVec2>
        get() = _tileOffset

    override val position: Cell<IntVec2> by lazy {
        tileOffset.map { tileTopLeftCorner(it) }
    }

    override fun setPosition(newPosition: IntVec2) {
        _tileOffset.set(newPosition.divRound(TILE_SIZE))
    }
}

sealed class Entity {
    abstract val entityPosition: EntityPosition

    val position by lazy { entityPosition.position }

    fun setPosition(newPosition: IntVec2) {
        entityPosition.setPosition(newPosition)
    }

    abstract fun isSelectableIn(area: IntRect): Boolean

    fun move(
        positionDelta: Cell<IntVec2>,
        tillStop: Till,
    ) {
        println("Starting to move entity...")

        val initialPosition = position.sample()
        val targetPosition = positionDelta.map { d -> initialPosition + d }

        targetPosition.reactTill(tillStop) {
            if (position.sample() != it) {
                setPosition(it)
            }
        }
    }
}
