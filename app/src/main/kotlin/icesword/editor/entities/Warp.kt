@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor.entities

import icesword.ImageSetId
import icesword.RezIndex
import icesword.editor.DynamicWapSprite
import icesword.editor.IntVec2Serializer
import icesword.editor.retails.Retail
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.Wwd
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
sealed class WarpPrototype {
    abstract val logic: String

    abstract val imageSetId: ImageSetId

    abstract val shortImageSetId: String
}

class Warp(
    rezIndex: RezIndex,
    private val prototype: WarpPrototype,
    initialPosition: IntVec2,
    initialZOrder: Int,
    initialTargetPosition: IntVec2,
) : Entity(),
    WapObjectExportable {

    @Serializable
    @SerialName("HorizontalWarp")
    object HorizontalWarpPrototype : WarpPrototype() {
        override val logic: String = "SpecialPowerup"

        override val imageSetId = ImageSetId("GAME_IMAGES_WARP")

        override val shortImageSetId: String = "GAME_WARP"
    }

    @Serializable
    @SerialName("VerticalWarp")
    object VerticalWarpPrototype : WarpPrototype() {
        override val logic: String = "SpecialPowerup"

        override val imageSetId = ImageSetId("GAME_IMAGES_VERTWARP")

        override val shortImageSetId: String = "GAME_VERTWARP"
    }

    @Serializable
    @SerialName("BossWarp")
    object BossWarpPrototype : WarpPrototype() {
        override val logic: String = "BossWarp"

        override val imageSetId = ImageSetId("GAME_IMAGES_BOSSWARP")

        override val shortImageSetId: String = "GAME_BOSSWARP"
    }

    companion object {
        fun load(
            rezIndex: RezIndex,
            data: WarpData,
        ): Warp = Warp(
            rezIndex = rezIndex,
            prototype = data.prototype,
            initialPosition = data.position,
            initialZOrder = data.zOrder,
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

    override val asZOrderedEntity: ZOrderedEntity = SimpleZOrderedEntity(
        initialZOrder = initialZOrder,
    )

    val wapSprite = DynamicWapSprite.fromImageSet(
        rezIndex = rezIndex,
        imageSetId = prototype.imageSetId,
        position = position,
        z = asZOrderedEntity.zOrder,
    )

    override fun isSelectableIn(area: IntRect): Boolean =
        wapSprite.isSelectableIn(area)

    override fun toEntityData() = WarpData(
        prototype = prototype,
        position = position.sample(),
        targetPosition = targetPosition.sample(),
    )

    override fun exportWapObject(): Wwd.Object_ {
        val position = position.sample()
        val zOrder = asZOrderedEntity.zOrder.sample()
        val targetPosition = targetPosition.sample()

        return WapObjectPropsData(
            logic = "SpecialPoweruo",
            imageSet = "GAME_WARP",
            x = position.x,
            y = position.y,
            z = zOrder,
            i = -1,
            speedX = targetPosition.x,
            speedY = targetPosition.y,
        ).toWwdObject()
    }
}

@Serializable
@SerialName("Warp")
data class WarpData(
    val prototype: WarpPrototype,
    val position: IntVec2,
    val zOrder: Int = 0,
    val targetPosition: IntVec2,
) : EntityData()
