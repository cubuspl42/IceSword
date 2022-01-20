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

class RopePrototype(
    retail: Retail,
) {
    val imageSetId = ImageSetId(fullyQualifiedId = "LEVEL${retail.naturalIndex}_IMAGES_ROPE")
}

class Rope(
    rezIndex: RezIndex,
    prototype: RopePrototype,
    initialPosition: IntVec2,
    initialSwingDurationMs: Int,
) :
    Entity(),
    WapObjectExportable {

    companion object {
        private val wwdObjectPrototype = Wwd.Object_.empty().copy(
            logic = encode("AniRope"),
            imageSet = encode("LEVEL_ROPE"),
        )

        fun load(
            rezIndex: RezIndex,
            retail: Retail,
            data: RopeData,
        ): Rope =
            Rope(
                rezIndex = rezIndex,
                prototype = RopePrototype(retail = retail),
                initialPosition = data.position,
                initialSwingDurationMs = data.swingDurationMs,
            )
    }

    override val entityPosition: EntityPosition =
        EntityPixelPosition(
            initialPosition = initialPosition,
        )

    override val zOrder: Cell<Int> = Cell.constant(0)

    override val asZOrderedEntity: ZOrderedEntity = SimpleZOrderedEntity(
        initialZOrder = 0,
    )

    val wapSprite = DynamicWapSprite.fromImageSet(
        rezIndex = rezIndex,
        imageSetId = prototype.imageSetId,
        position = entityPosition.position,
        z = asZOrderedEntity.zOrder,
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

        return wwdObjectPrototype.copy(
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
