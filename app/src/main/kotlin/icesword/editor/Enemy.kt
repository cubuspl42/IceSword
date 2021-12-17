@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.RezIndex
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.MutableDynamicList
import icesword.frp.dynamic_list.sampleContent
import icesword.frp.dynamic_list.size
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.Geometry
import icesword.wwd.Geometry.Rectangle
import icesword.wwd.Wwd
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class Enemy(
    rezIndex: RezIndex,
    private val wapObjectPrototype: WapObjectPrototype,
    initialPosition: IntVec2,
    initialRelativeMovementRange: HorizontalRange,
    initialPickups: List<PickupKind>,
) : Entity(),
    WapObjectExportable {

    companion object {
        const val pickupCountLimit: Int = 9

        fun load(
            rezIndex: RezIndex,
            data: EnemyData,
        ): Enemy =
            Enemy(
                rezIndex = rezIndex,
                wapObjectPrototype = data.objectPrototype,
                initialPosition = data.position,
                initialRelativeMovementRange = data.relativeMovementRange,
                initialPickups = data.pickups.take(pickupCountLimit),
            )
    }

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

    val imageSetId = wapObjectPrototype.imageSetId

    val wapSprite = DynamicWapSprite.fromImageSet(
        rezIndex = rezIndex,
        imageSetId = imageSetId,
        position = entityPosition.position,
    )

    val movementRange = EntityMovementRangeMixin(
        movementOrigin = wapSprite.center,
        initialRelativeMovementRange = initialRelativeMovementRange,
    )

    override fun isSelectableIn(area: IntRect): Boolean {
        val hitBox = wapSprite.boundingBox.sample()
        return hitBox.overlaps(area)
    }

    override fun toEntityData(): EnemyData = EnemyData(
        objectPrototype = wapObjectPrototype,
        position = position.sample(),
        relativeMovementRange = movementRange.relativeMovementRange.sample(),
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
        val movementRange = movementRange.relativeMovementRange.sample()
        val globalMovementRange = movementRange.translate(position)
        val pickups = pickups.volatileContentView

        return wapObjectPrototype.wwdObjectPrototype.copy(
            x = position.x,
            y = position.y,
            rangeRect = Rectangle.zero.copy(
                left = globalMovementRange.minX,
                right = globalMovementRange.maxX,
            ),
            powerUp = pickups.getOrNull(0)?.code ?: 0,
            userRect1 = encodePickups(
                pickup1 = pickups.getOrNull(1),
                pickup2 = pickups.getOrNull(2),
                pickup3 = pickups.getOrNull(3),
                pickup4 = pickups.getOrNull(4),
            ),
            userRect2 = encodePickups(
                pickup1 = pickups.getOrNull(5),
                pickup2 = pickups.getOrNull(6),
                pickup3 = pickups.getOrNull(7),
                pickup4 = pickups.getOrNull(8),
            ),
        )
    }
}

@Serializable
@SerialName("Enemy")
data class EnemyData(
    val objectPrototype: WapObjectPrototype,
    val position: IntVec2,
    val relativeMovementRange: HorizontalRange = HorizontalRange.ZERO,
    val pickups: List<PickupKind> = emptyList(),
) : EntityData()
