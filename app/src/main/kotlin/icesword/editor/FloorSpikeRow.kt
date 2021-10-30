@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.RezIndex
import icesword.editor.WapObjectPrototype.ElevatorPrototype
import icesword.editor.WapObjectPrototype.FloorSpikePrototype
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.reactTill
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.Wwd
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class FloorSpikeRow(
    rezIndex: RezIndex,
    initialPosition: IntVec2,
) : Entity(), WapObjectExportable {

    companion object {
        fun load(
            rezIndex: RezIndex,
            data: FloorSpikeRowData,
        ): FloorSpikeRow =
            FloorSpikeRow(
                rezIndex = rezIndex,
                initialPosition = data.position,
            )
    }

    override val entityPosition: EntityPosition =
        EntityPixelPosition(
            initialPosition = initialPosition,
        )

    val wapSprite = WapSprite(
        rezIndex = rezIndex,
        imageSetId = FloorSpikePrototype.imageSetId,
        position = entityPosition.position,
    )

    override fun isSelectableIn(area: IntRect): Boolean {
        val hitBox = wapSprite.boundingBox.sample()
        return hitBox.overlaps(area)
    }

    override fun exportWapObject(): Wwd.Object_ {
        val position = position.sample()

        return FloorSpikePrototype.wwdObjectPrototype.copy(
            x = position.x,
            y = position.y,
        )
    }

    fun toData(): FloorSpikeRowData = FloorSpikeRowData(
        position = entityPosition.position.sample(),
    )
}

@Serializable
data class FloorSpikeRowData(
    val position: IntVec2,
)
