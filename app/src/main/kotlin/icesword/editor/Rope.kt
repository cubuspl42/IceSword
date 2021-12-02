@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.ImageSetId
import icesword.RezIndex
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.Wwd
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class Rope(
    rezIndex: RezIndex,
    initialPosition: IntVec2,
    initialSwingDurationMs: Int,
) :
    Entity(),
    WapObjectExportable {

    companion object {
        val imageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL3_IMAGES_ROPE",
        )

        fun load(
            rezIndex: RezIndex,
            data: RopeData,
        ): Rope =
            Rope(
                rezIndex = rezIndex,
                initialPosition = data.position,
                initialSwingDurationMs = data.swingDurationMs,
            )
    }

    override val entityPosition: EntityPosition =
        EntityPixelPosition(
            initialPosition = initialPosition,
        )

    val wapSprite = WapSprite.fromImageSet(
        rezIndex = rezIndex,
        imageSetId = imageSetId,
        position = entityPosition.position,
    )

    private val _swingDurationMs = MutCell(initialSwingDurationMs)

    // How much it takes the rope to swing from one side to another
    val swingDurationMs: Cell<Int>
        get() = _swingDurationMs

    fun setSwingDuration(newSwingDurationMs: Int) {
        _swingDurationMs.set(newSwingDurationMs)
    }

    override fun isSelectableIn(area: IntRect): Boolean {
        val hitBox = wapSprite.boundingBox.sample()
        return hitBox.overlaps(area)
    }

    override fun toEntityData(): RopeData = RopeData(
        position = position.sample(),
        swingDurationMs = swingDurationMs.sample(),
    )

    override fun exportWapObject(): Wwd.Object_ {
        val position = position.sample()
        val speed = swingDurationMs.sample()

        return WapObjectPrototype.RopePrototype.wwdObjectPrototype.copy(
            x = position.x,
            y = position.y,
            speedX = speed,
        )
    }
}

@Serializable
@SerialName("Rope")
data class RopeData(
    val position: IntVec2,
    val swingDurationMs: Int,
) : EntityData()
