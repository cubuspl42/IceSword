@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.RezIndex
import icesword.editor.WapObjectPrototype.ElevatorPrototype
import icesword.frp.Cell
import icesword.frp.map
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.Geometry.Rectangle
import icesword.wwd.Wwd
import kotlinx.serialization.UseSerializers

sealed class Elevator<Range : AxisRange<Range>>(
    rezIndex: RezIndex,
    initialPosition: IntVec2,
    initialRelativeMovementRange: Range,
) :
    Entity(),
    EntityMovementRange<Range> by EntityMovementRangeMixin(
        initialRelativeMovementRange = initialRelativeMovementRange,
    ),
    WapObjectExportable {

    final override val entityPosition: EntityPosition =
        EntityPixelPosition(
            initialPosition = initialPosition,
        )

    val wapSprite = WapSprite.fromImageSet(
        rezIndex = rezIndex,
        imageSetId = ElevatorPrototype.imageSetId,
        position = entityPosition.position,
    )

    override val movementOrigin: Cell<IntVec2>
        get() = wapSprite.boundingBox.map { it.center }

    val globalMovementRange by lazy {
        Cell.map2(
            entityPosition.position,
            relativeMovementRange,
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

        return ElevatorPrototype.wwdObjectPrototype.copy(
            x = position.x,
            y = position.y,
            rangeRect = exportElevatorRangeRect(),
        )
    }
}
