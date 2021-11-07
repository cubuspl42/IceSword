@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.RezIndex
import icesword.editor.WapObjectPrototype.ElevatorPrototype
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.reactTill
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.Wwd
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class HorizontalRange(
    val minX: Int,
    val maxX: Int,
) {
    companion object {
        val ZERO = HorizontalRange(minX = 0, maxX = 0)
    }

    val width: Int
        get() = maxX - minX

    fun translate(tx: Int): HorizontalRange =
        HorizontalRange(minX + tx, maxX + tx)
}

class HorizontalElevator(
    rezIndex: RezIndex,
    initialPosition: IntVec2,
    initialRelativeMovementRange: HorizontalRange,
) : Entity(), WapObjectExportable {

    companion object {
        fun load(
            rezIndex: RezIndex,
            data: HorizontalElevatorData,
        ): HorizontalElevator =
            HorizontalElevator(
                rezIndex = rezIndex,
                initialPosition = data.position,
                initialRelativeMovementRange = data.relativeMovementRange,
            )
    }

    override val entityPosition: EntityPosition =
        EntityPixelPosition(
            initialPosition = initialPosition,
        )

    val wapSprite = WapSprite(
        rezIndex = rezIndex,
        imageSetId = ElevatorPrototype.imageSetId,
        position = entityPosition.position,
    )

    private val relativeMovementRange = MutCell(
        initialValue = initialRelativeMovementRange,
    )

    fun resizeMovementRangeMin(
        minDelta: Cell<Int>,
        till: Till,
    ) {
        resizeMovementRange(
            extremumDelta = minDelta,
            extremum = { it.minX },
            copy = { it, minX -> it.copy(minX = minX) },
            till = till,
        )
    }

    fun resizeMovementRangeMax(
        minDelta: Cell<Int>,
        till: Till,
    ) {
        resizeMovementRange(
            extremumDelta = minDelta,
            extremum = { it.maxX },
            copy = { it, maxX -> it.copy(maxX = maxX) },
            till = till,
        )
    }

    private fun resizeMovementRange(
        extremumDelta: Cell<Int>,
        extremum: (HorizontalRange) -> Int,
        copy: (it: HorizontalRange, extremum: Int) -> HorizontalRange,
        till: Till,
    ) {
        val initialRange = relativeMovementRange.sample()

        val initialExtremum = extremum(initialRange)
        val newExtremum = extremumDelta.map { initialExtremum + it }

        newExtremum.reactTill(till) {
            val currentRange = relativeMovementRange.sample()
            val newRange = copy(initialRange, it)

            if (newRange != currentRange) {
                relativeMovementRange.set(newRange)
            }
        }
    }

    val globalMovementRange = Cell.map2(
        entityPosition.position,
        relativeMovementRange,
    ) { ep, mr ->
        mr.translate(ep.x)
    }

    override fun isSelectableIn(area: IntRect): Boolean {
        val hitBox = wapSprite.boundingBox.sample()
        return hitBox.overlaps(area)
    }

    override fun exportWapObject(): Wwd.Object_ {
        val position = position.sample()

        val movementRange = globalMovementRange.sample()
        return ElevatorPrototype.wwdObjectPrototype.copy(
            x = position.x,
            y = position.y,
            xMin = movementRange.minX,
            xMax = movementRange.maxX,
        )
    }

    fun toData(): HorizontalElevatorData = HorizontalElevatorData(
        position = entityPosition.position.sample(),
        relativeMovementRange = relativeMovementRange.sample(),
    )
}

@Serializable
data class HorizontalElevatorData(
    val position: IntVec2,
    val relativeMovementRange: HorizontalRange = HorizontalRange.ZERO,
)
