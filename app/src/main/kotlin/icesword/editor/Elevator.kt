@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.RezIndex
import icesword.editor.WapObjectPrototype.ElevatorPrototype
import icesword.frp.Cell
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.Wwd
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class Elevator(
    rezIndex: RezIndex,
    initialPosition: IntVec2,
) : Entity(), WapObjectExportable {
    override val entityPosition: EntityPosition =
        EntityPixelPosition(
            initialPosition = initialPosition,
        )

    val wapObjectStem = WapObjectStem(
        rezIndex = rezIndex,
        wapObjectPrototype = ElevatorPrototype,
        position = entityPosition.position,
    )

    override fun isSelectableIn(area: IntRect): Boolean {
        val hitBox = wapObjectStem.boundingBox.sample()
        return hitBox.overlaps(area)
    }

    override fun exportWapObject(): Wwd.Object_ {
        TODO("Not yet implemented")
    }

    fun toData(): ElevatorData {
        TODO("Not yet implemented")
    }
}

@Serializable
data class ElevatorData(
    val position: IntVec2,
)
