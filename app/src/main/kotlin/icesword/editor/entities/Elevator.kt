@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor.entities

import icesword.ImageSetId
import icesword.RezIndex
import icesword.editor.DynamicWapSprite
import icesword.editor.IntVec2Serializer
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.Geometry.Rectangle
import icesword.wwd.Wwd
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

data class ElevatorPrototype(
    val elevatorImageSetId: ImageSetId,
    val wwdObjectPrototype: Wwd.Object_,
)

enum class ElevatorMovementCondition {
    // It always moves (standard)
    MovesAlways,

    // Moving only when the player is standing on it
    MovesWithPlayer,

    // Moving only when the player is not standing on it
    MovesWithoutPlayer,

    // Starts to move once the player stands on it for the first time
    MovesOnceTriggered,
}

enum class ElevatorMovementPattern {
    // It moves from the start point to the end point and stops
    OneWay,

    // It repeatedly moves from the start point to the end point and comes back
    TwoWay,
}

class ElevatorProps(
    initialMovementCondition: ElevatorMovementCondition,
    initialMovementPattern: ElevatorMovementPattern,
) {
    companion object {
        fun load(data: ElevatorPropsData) = ElevatorProps(
            initialMovementCondition = data.movementCondition,
            initialMovementPattern = data.movementPattern,
        )
    }

    val movementCondition: MutCell<ElevatorMovementCondition> =
        MutCell(initialMovementCondition)

    val movementPattern: MutCell<ElevatorMovementPattern> =
        MutCell(initialMovementPattern)

    fun toData() = ElevatorPropsData(
        movementCondition = movementCondition.sample(),
        movementPattern = movementPattern.sample(),
    )
}

@Serializable
data class ElevatorPropsData(
    val movementCondition: ElevatorMovementCondition,
    val movementPattern: ElevatorMovementPattern,
) {
    companion object {
        val default = ElevatorPropsData(
            movementCondition = ElevatorMovementCondition.MovesAlways,
            movementPattern = ElevatorMovementPattern.TwoWay,
        )
    }
}

sealed class Elevator<Range : AxisRange<Range>>(
    rezIndex: RezIndex,
    private val prototype: ElevatorPrototype,
    initialPosition: IntVec2,
    initialZOrder: Int,
    initialRelativeMovementRange: Range,
    val props: ElevatorProps,
) :
    Entity(),
    WapObjectExportable {

    final override val entityPosition: EntityPosition =
        EntityPixelPosition(
            initialPosition = initialPosition,
        )

    final override val asZOrderedEntity: ZOrderedEntity = SimpleZOrderedEntity(
        initialZOrder = initialZOrder,
    )

    val wapSprite = DynamicWapSprite.fromImageSet(
        rezIndex = rezIndex,
        imageSetId = prototype.elevatorImageSetId,
        position = entityPosition.position,
        z = asZOrderedEntity.zOrder,
    )

    val movementRange = EntityMovementRangeMixin(
        movementOrigin = wapSprite.center,
        initialRelativeMovementRange = initialRelativeMovementRange,
    )

    val globalMovementRange by lazy {
        Cell.map2(
            entityPosition.position,
            movementRange.relativeMovementRange,
        ) { ep, mr ->
            mr.translate(ep)
        }
    }

    final override fun isSelectableIn(area: IntRect): Boolean {
        val hitBox = wapSprite.boundingBox.sample()
        return hitBox.overlaps(area)
    }

    abstract fun exportElevatorRangeRect(): Rectangle

    final override fun exportWapObject(): Wwd.Object_ {
        val position = position.sample()
        val zOrder = asZOrderedEntity.zOrder.sample()

        return prototype.wwdObjectPrototype.copy(
            x = position.x,
            y = position.y,
            z = zOrder,
            rangeRect = exportElevatorRangeRect(),
        )
    }
}
