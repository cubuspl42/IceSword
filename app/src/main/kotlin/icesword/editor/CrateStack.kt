@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.ImageSetId
import icesword.RezIndex
import icesword.editor.WapObjectPrototype.StackedCratesPrototype
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.MutableDynamicList
import icesword.frp.dynamic_list.sampleContent
import icesword.frp.dynamic_list.size
import icesword.frp.map
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.Geometry
import icesword.wwd.Geometry.Rectangle
import icesword.wwd.Wwd
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class CrateStack(
    rezIndex: RezIndex,
    initialPosition: IntVec2,
    initialPickups: List<PickupKind>,
) : Entity(),
    WapObjectExportable {

    companion object {
        const val pickupCountLimit: Int = 8

        val crateImageSetId: ImageSetId = StackedCratesPrototype.imageSetId

        val crateObjectPrototype = Wwd.Object_.empty().copy(
            logic = encode("StackedCrates"),
            imageSet = encode("LEVEL_CRATE"),
            speedX = 1750,
        )

        fun load(
            rezIndex: RezIndex,
            data: CrateStackData,
        ): CrateStack =
            CrateStack(
                rezIndex = rezIndex,
                initialPosition = data.position,
                initialPickups = data.pickups.take(pickupCountLimit),
            )
    }

    private val crateImageMetadata = rezIndex.getImageMetadata(
        imageSetId = crateImageSetId,
        i = -1,
    )!!

    private val _pickups = MutableDynamicList(
        initialContent = initialPickups,
    )

    val pickups: DynamicList<PickupKind> = _pickups

    fun removePickupAt(pickupIndex: Int) {
        _pickups.removeAt(index = pickupIndex)
    }

    fun addPickup(pickup: PickupKind) {
        if (pickups.size.sample() < pickupCountLimit) {
            _pickups.add(pickup)
        }
    }

    override val entityPosition: EntityPosition =
        EntityPixelPosition(
            initialPosition = initialPosition,
        )

    val wapSprite = WapSprite(
        imageMetadata = crateImageMetadata,
        position = entityPosition.position,
    )

    val boundingBox = wapSprite.boundingBox

    override fun isSelectableIn(area: IntRect): Boolean {
        val hitBox = wapSprite.boundingBox.sample()
        return hitBox.overlaps(area)
    }

    override fun toEntityData(): CrateStackData = CrateStackData(
        position = position.sample(),
        pickups = pickups.sampleContent(),
    )

    override fun exportWapObject(): Wwd.Object_ {
        fun encodePickups(
            pickup1: PickupKind?,
            pickup2: PickupKind?,
            pickup3: PickupKind?,
            pickup4: PickupKind?,
        ): Geometry.Rectangle = Geometry.Rectangle(
            // Encoding order is taken from OpenClaw, maybe there was a reason
            left = pickup1?.code ?: 0,
            top = pickup4?.code ?: 0,
            right = pickup2?.code ?: 0,
            bottom = pickup3?.code ?: 0,
        )

        val position = position.sample()
        val pickups = pickups.volatileContentView

        return crateObjectPrototype.copy(
            x = position.x,
            y = position.y,
            userRect1 = encodePickups(
                pickup4 = pickups.getOrNull(0),
                pickup1 = pickups.getOrNull(1),
                pickup2 = pickups.getOrNull(2),
                pickup3 = pickups.getOrNull(3)
            ),
            userRect2 = encodePickups(
                pickup4 = pickups.getOrNull(4),
                pickup1 = pickups.getOrNull(5),
                pickup2 = pickups.getOrNull(6),
                pickup3 = pickups.getOrNull(7),
            ),
        )
    }
}

@Serializable
@SerialName("CrateStack")
data class CrateStackData(
    val position: IntVec2,
    val pickups: List<PickupKind> = emptyList(),
) : EntityData()
