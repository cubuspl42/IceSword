@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.RezIndex
import icesword.geometry.IntVec2
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class VerticalRange(
    val minY: Int,
    val maxY: Int,
) : AxisRange<VerticalRange> {
    companion object {
        val ZERO = VerticalRange(minY = 0, maxY = 0)
    }

    override val min: Int
        get() = minY

    override val max: Int
        get() = maxY

    val width: Int
        get() = maxY - minY

    override fun translate(tx: Int): VerticalRange =
        VerticalRange(minY + tx, maxY + tx)

    override fun copyWithMin(min: Int): VerticalRange =
        this.copy(minY = min)

    override fun copyWithMax(max: Int): VerticalRange =
        this.copy(maxY = max)
}

class VerticalElevator(
    rezIndex: RezIndex,
    initialPosition: IntVec2,
    initialRelativeMovementRange: VerticalRange,
) : Elevator<VerticalRange>(
    rezIndex = rezIndex,
    initialPosition = initialPosition,
    initialRelativeMovementRange = initialRelativeMovementRange,
), WapObjectExportable {

    companion object {
        fun load(
            rezIndex: RezIndex,
            data: VerticalElevatorData,
        ): VerticalElevator =
            VerticalElevator(
                rezIndex = rezIndex,
                initialPosition = data.position,
                initialRelativeMovementRange = data.relativeMovementRange,
            )
    }

    fun toData(): VerticalElevatorData = VerticalElevatorData(
        position = entityPosition.position.sample(),
        relativeMovementRange = relativeMovementRange.sample(),
    )
}

@Serializable
data class VerticalElevatorData(
    val position: IntVec2,
    val relativeMovementRange: VerticalRange = VerticalRange.ZERO,
)
