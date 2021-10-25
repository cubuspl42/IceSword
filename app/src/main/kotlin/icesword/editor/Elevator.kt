@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.RezIndex
import icesword.editor.WapObjectPrototype.ElevatorPrototype
import icesword.frp.Cell
import icesword.frp.map
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.Wwd
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

data class VerticalRange(
    val minX: Int,
    val maxX: Int,
) {
    val width: Int
        get() = maxX - minX
}


class Elevator(
    rezIndex: RezIndex,
    initialPosition: IntVec2,
) : Entity(), WapObjectExportable {
    companion object {
        private const val rangeRadius = 48
    }

    override val entityPosition: EntityPosition =
        EntityPixelPosition(
            initialPosition = initialPosition,
        )

    val wapObjectStem = WapObjectStem(
        rezIndex = rezIndex,
        wapObjectPrototype = ElevatorPrototype,
        position = entityPosition.position,
    )

    val relativeMovementRange = VerticalRange(
        minX = -rangeRadius,
        maxX = rangeRadius
    )

    val globalMovementRange = entityPosition.position.map {
        VerticalRange(
            minX = it.x - rangeRadius,
            maxX = it.x + rangeRadius
        )
    }

    override fun isSelectableIn(area: IntRect): Boolean {
        val hitBox = wapObjectStem.boundingBox.sample()
        return hitBox.overlaps(area)
    }

    override fun exportWapObject(): Wwd.Object_ {
        val position = position.sample()

        return ElevatorPrototype.wwdObjectPrototype.copy(
            x = position.x,
            y = position.y,
        )
    }

    fun toData(): ElevatorData = ElevatorData(
        position = entityPosition.position.sample(),
    )
}

@Serializable
data class ElevatorData(
    val position: IntVec2,
)
