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

@Serializable
data class CrumblingPegPrototype(
    val imageSetId: ImageSetId,
    val shortImageSetId: String,
)

class CrumblingPeg(
    rezIndex: RezIndex,
    retail: Retail,
    private val prototype: CrumblingPegPrototype,
    initialPosition: IntVec2,
    initialCanRespawn: Boolean,
) : Entity(),
    WapObjectExportable {

    companion object {
        fun load(
            rezIndex: RezIndex,
            retail: Retail,
            data: CrumblingPegData,
        ): CrumblingPeg = CrumblingPeg(
            rezIndex = rezIndex,
            retail = retail,
            prototype = data.prototype,
            initialPosition = data.position,
            initialCanRespawn = data.canRespawn,
        )
    }

    private val _canRespawn = MutCell(
        initialValue = initialCanRespawn,
    )

    val canRespawn: Cell<Boolean> = _canRespawn

    fun setCanRespawn(newValue: Boolean) {
        _canRespawn.set(newValue)
    }

    override val entityPosition = EntityPixelPosition(
        initialPosition = initialPosition,
    )

    val wapSprite = DynamicWapSprite.fromImageSet(
        rezIndex = rezIndex,
        imageSetId = expandImageSetId(
            retail = retail,
            shortImageSetId = prototype.shortImageSetId,
        ),
        position = position,
    )

    override fun isSelectableIn(area: IntRect): Boolean =
        wapSprite.isSelectableIn(area)

    override fun toEntityData() = CrumblingPegData(
        prototype = prototype,
        position = position.sample(),
        canRespawn = canRespawn.sample(),
    )

    override fun exportWapObject(): Wwd.Object_ {
        val position = position.sample()
        val canRespawn = canRespawn.sample()

        return WapObjectPropsData(
            logic = if (canRespawn) "CrumblingPeg" else "CrumblingPegNoRespawn",
            imageSet = prototype.shortImageSetId,
            x = position.x,
            y = position.y,
            i = -1,
        ).toWwdObject()
    }
}

@Serializable
@SerialName("CrumblingPeg")
data class CrumblingPegData(
    val prototype: CrumblingPegPrototype,
    val position: IntVec2,
    val canRespawn: Boolean,
) : EntityData()
