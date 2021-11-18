@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.RezIndex
import icesword.frp.Cell
import icesword.frp.map
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.Geometry.Rectangle
import icesword.wwd.Wwd
import kotlinx.serialization.UseSerializers

class Enemy(
    rezIndex: RezIndex,
    private val wapObjectPrototype: WapObjectPrototype,
    initialPosition: IntVec2,
    initialRelativeMovementRange: HorizontalRange,
) :
    Entity(),
    EntityMovementRange<HorizontalRange> by EntityMovementRangeMixin(
        initialRelativeMovementRange = initialRelativeMovementRange,
    ),
    WapObjectExportable {

    final override val entityPosition: EntityPosition =
        EntityPixelPosition(
            initialPosition = initialPosition,
        )

    val wapSprite = WapSprite(
        rezIndex = rezIndex,
        imageSetId = wapObjectPrototype.imageSetId,
        position = entityPosition.position,
    )

    override val movementOrigin: Cell<IntVec2>
        get() = wapSprite.boundingBox.map { it.center }

    final override fun isSelectableIn(area: IntRect): Boolean {
        val hitBox = wapSprite.boundingBox.sample()
        return hitBox.overlaps(area)
    }

    final override fun exportWapObject(): Wwd.Object_ {
        val position = position.sample()
        val movementRange = relativeMovementRange.sample()
        val globalMovementRange = movementRange.translate(position)

        return wapObjectPrototype.wwdObjectPrototype.copy(
            x = position.x,
            y = position.y,
            rangeRect = Rectangle(
                left = globalMovementRange.minX,
                right = globalMovementRange.maxX,
                top = 0,
                bottom = 0,
            ),
        )
    }
}
