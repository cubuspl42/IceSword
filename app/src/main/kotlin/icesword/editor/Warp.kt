@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.ImageSetId
import icesword.RezIndex
import icesword.editor.retails.Retail
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.Wwd
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class Warp(
    rezIndex: RezIndex,
    initialPosition: IntVec2,
    initialTargetPosition: IntVec2,
) : Entity(),
    WapObjectExportable {

    companion object {
        val imageSetId = ImageSetId("GAME_IMAGES_WARP")

        fun load(
            rezIndex: RezIndex,
            retail: Retail,
            data: WarpData,
        ): Warp = Warp(
            rezIndex = rezIndex,
            initialPosition = data.position,
            initialTargetPosition = data.targetPosition,
        )
    }


    private val _targetPosition = MutCell(
        initialValue = initialTargetPosition,
    )

    val targetPosition: Cell<IntVec2> = _targetPosition

    fun setTargetPosition(newValue: IntVec2) {
        _targetPosition.set(newValue)
    }

    override val entityPosition = EntityPixelPosition(
        initialPosition = initialPosition,
    )

    val wapSprite = DynamicWapSprite.fromImageSet(
        rezIndex = rezIndex,
        imageSetId = imageSetId,
        position = position,
    )

    override fun isSelectableIn(area: IntRect): Boolean =
        wapSprite.isSelectableIn(area)

    override fun toEntityData() = WarpData(
        position = position.sample(),
        targetPosition = targetPosition.sample(),
    )

    override fun exportWapObject(): Wwd.Object_ {
        val position = position.sample()
        val targetPosition = targetPosition.sample()

        return WapObjectPropsData(
            logic = "SpecialPoweruo",
            imageSet = "GAME_WARP",
            x = position.x,
            y = position.y,
            i = -1,
            speedX = targetPosition.x,
            speedY = targetPosition.y,
        ).toWwdObject()
    }
}

@Serializable
@SerialName("Warp")
data class WarpData(
    val position: IntVec2,
    val targetPosition: IntVec2,
) : EntityData()
