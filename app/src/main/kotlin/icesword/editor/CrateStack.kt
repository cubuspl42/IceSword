@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.ImageSetId
import icesword.RezIndex
import icesword.editor.WapObjectPrototype.StackedCratesPrototype
import icesword.frp.Cell
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
import kotlinx.css.del
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

    data class OutputCrate(
        val position: IntVec2,
        val bounds: IntRect,
        val wapSprite: WapSprite,
    )

    data class OutputStack(
        val crates: List<OutputCrate>,
    )

    private val crateImageMetadata = rezIndex.getImageMetadata(
        imageSetId = crateImageSetId,
        i = -1,
    )!!

    private val _pickups = MutableDynamicList(
        initialContent = initialPickups,
    )

    val pickups: DynamicList<PickupKind> = _pickups

    fun pushCrate() {
        val pickupKinds = listOf(
            PickupKind.TreasureCoins,
            PickupKind.TreasureRingsRed,
            PickupKind.TreasureRingsGreen,
            PickupKind.TreasureRingsBlue,
            PickupKind.TreasureRingsPurple,
            PickupKind.TreasureCrossesRed,
            PickupKind.TreasureCrossesGreen,
            PickupKind.TreasureCrossesBlue,
            PickupKind.TreasureCrossesPurple,
            PickupKind.TreasureSceptersRed,
            PickupKind.TreasureSceptersGreen,
            PickupKind.TreasureSceptersBlue,
            PickupKind.TreasureSceptersPurple,
            PickupKind.TreasureGeckosRed,
            PickupKind.TreasureGeckosGreen,
            PickupKind.TreasureGeckosBlue,
            PickupKind.TreasureGeckosPurple,
            PickupKind.TreasureChalicesRed,
            PickupKind.TreasureChalicesGreen,
            PickupKind.TreasureChalicesBlue,
            PickupKind.TreasureChalicesPurple,
            PickupKind.TreasureCrownsRed,
            PickupKind.TreasureCrownsGreen,
            PickupKind.TreasureCrownsBlue,
            PickupKind.TreasureCrownsPurple,
            PickupKind.TreasureSkullRed,
            PickupKind.TreasureSkullGreen,
            PickupKind.TreasureSkullBlue,
            PickupKind.TreasureSkullPurple,
        )

        val pickupKind = pickupKinds.shuffled().first()

        if (pickups.size.sample() < pickupCountLimit) {
            _pickups.add(pickupKind)
        }
    }

    fun popCrate() {
        if (pickups.size.sample() > 1) {
            _pickups.removeLast()
        }
    }

    fun setPickups(
        pickups: List<PickupKind>,
    ) {
        _pickups.replaceContent(pickups)
    }

    override val entityPosition: EntityPosition =
        EntityPixelPosition(
            initialPosition = initialPosition,
        )

    val wapSprite = WapSprite(
        imageMetadata = crateImageMetadata,
        position = entityPosition.position,
    )

    private fun buildCrates(
        size: Int,
        position: IntVec2,
    ): List<OutputCrate> {
        val crate = OutputCrate(
            position = position,
            bounds = calculateWapSpriteBounds(
                imageMetadata = crateImageMetadata,
                position = position,
            ),
            wapSprite = WapSprite(
                imageMetadata = crateImageMetadata,
                position = Cell.constant(position),
            ),
        )

        val delta = IntVec2(x = 0, y = -42)

        val tail = if (size > 1) buildCrates(
            size = size - 1,
            position = position + delta,
        ) else emptyList()

        return listOf(crate) + tail
    }

    val outputStack = Cell.map2(
        entityPosition.position,
        pickups.size,
    ) { position, size ->
        OutputStack(
            crates = buildCrates(
                size = size,
                position = position,
            ),
        )
    }

    val boundingBox = outputStack.map { stack ->
        IntRect.enclosing(rects = stack.crates.map { it.bounds })
    }

    override fun isSelectableIn(area: IntRect): Boolean =
        boundingBox.sample().overlaps(area)

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
