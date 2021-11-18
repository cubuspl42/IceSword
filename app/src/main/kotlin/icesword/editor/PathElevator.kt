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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class PathElevator(
    rezIndex: RezIndex,
    initialPosition: IntVec2,
) :
    Entity(),
    WapObjectExportable {

    override val entityPosition: EntityPosition =
        EntityPixelPosition(
            initialPosition = initialPosition,
        )

    val wapSprite = WapSprite(
        rezIndex = rezIndex,
        imageSetId = ElevatorPrototype.imageSetId,
        position = entityPosition.position,
    )

    override fun isSelectableIn(area: IntRect): Boolean {
        return false
    }

    override fun toEntityData(): EntityData? = null

    override fun exportWapObject(): Wwd.Object_ =
        ElevatorPrototype.wwdObjectPrototype
}
