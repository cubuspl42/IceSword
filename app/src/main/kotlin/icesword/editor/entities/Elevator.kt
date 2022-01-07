@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor.entities

import icesword.ImageSetId
import icesword.RezIndex
import icesword.editor.DynamicWapSprite
import icesword.editor.IntVec2Serializer
import icesword.frp.Cell
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.Geometry.Rectangle
import icesword.wwd.Wwd
import kotlinx.serialization.UseSerializers

data class ElevatorPrototype(
    val elevatorImageSetId: ImageSetId,
    val wwdObjectPrototype: Wwd.Object_,
)

sealed class Elevator<Range : AxisRange<Range>>(
    rezIndex: RezIndex,
    private val prototype: ElevatorPrototype,
    initialPosition: IntVec2,
    initialRelativeMovementRange: Range,
) :
    Entity(),
    WapObjectExportable {

    final override val entityPosition: EntityPosition =
        EntityPixelPosition(
            initialPosition = initialPosition,
        )

    override val zOrder: Cell<Int> = Cell.constant(2000)

    val wapSprite = DynamicWapSprite.fromImageSet(
        rezIndex = rezIndex,
        imageSetId = prototype.elevatorImageSetId,
        position = entityPosition.position,
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

        return prototype.wwdObjectPrototype.copy(
            x = position.x,
            y = position.y,
            rangeRect = exportElevatorRangeRect(),
        )
    }
}
