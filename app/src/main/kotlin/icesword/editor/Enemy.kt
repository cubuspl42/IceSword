@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.RezIndex
import icesword.frp.Cell
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.MutableDynamicList
import icesword.frp.dynamic_list.size
import icesword.frp.map
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
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
) :
    Entity(),
    EntityMovementRange<HorizontalRange> by EntityMovementRangeMixin(
        initialRelativeMovementRange = initialRelativeMovementRange,
    ),
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
            )
    }

    private val _pickups = MutableDynamicList(
        initialContent = listOf(
            PickupKind.TreasureCoins,
            PickupKind.TreasureCoins,
            PickupKind.TreasureRingsGreen,
        ),
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

    val wapSprite = WapSprite.fromImageSet(
        rezIndex = rezIndex,
        imageSetId = imageSetId,
        position = entityPosition.position,
    )

    override val movementOrigin: Cell<IntVec2>
        get() = wapSprite.boundingBox.map { it.center }

    override fun isSelectableIn(area: IntRect): Boolean {
        val hitBox = wapSprite.boundingBox.sample()
        return hitBox.overlaps(area)
    }

    override fun toEntityData(): EnemyData = EnemyData(
        objectPrototype = wapObjectPrototype,
        position = position.sample(),
        relativeMovementRange = relativeMovementRange.sample(),
    )

    override fun exportWapObject(): Wwd.Object_ {
        val position = position.sample()
        val movementRange = relativeMovementRange.sample()
        val globalMovementRange = movementRange.translate(position)

        return wapObjectPrototype.wwdObjectPrototype.copy(
            x = position.x,
            y = position.y,
            rangeRect = Rectangle.zero.copy(
                left = globalMovementRange.minX,
                right = globalMovementRange.maxX,
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
) : EntityData()
