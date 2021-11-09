@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.RezIndex
import icesword.geometry.IntVec2
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class HorizontalRange(
    val minX: Int,
    val maxX: Int,
) : AxisRange<HorizontalRange> {
    companion object {
        val ZERO = HorizontalRange(minX = 0, maxX = 0)
    }

    override val min: Int
        get() = minX

    override val max: Int
        get() = maxX

    val width: Int
        get() = maxX - minX

    override fun translate(tx: Int): HorizontalRange =
        HorizontalRange(minX + tx, maxX + tx)

    override fun copyWithMin(min: Int): HorizontalRange =
        this.copy(minX = min)

    override fun copyWithMax(max: Int): HorizontalRange =
        this.copy(maxX = max)
}

class HorizontalElevator(
    rezIndex: RezIndex,
    initialPosition: IntVec2,
    initialRelativeMovementRange: HorizontalRange,
) : Elevator<HorizontalRange>(
    rezIndex = rezIndex,
    initialPosition = initialPosition,
    initialRelativeMovementRange = initialRelativeMovementRange,
), WapObjectExportable {

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