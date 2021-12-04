@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.RezIndex
import icesword.geometry.IntLineSeg
import icesword.geometry.IntVec2
import icesword.wwd.Geometry
import icesword.wwd.Geometry.Rectangle
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

    override fun translate(t: IntVec2): VerticalRange =
        VerticalRange(minY + t.y, maxY + t.y)

    override fun copyWithMin(min: Int): VerticalRange =
        this.copy(minY = min)

    override fun copyWithMax(max: Int): VerticalRange =
        this.copy(maxY = max)

    override fun toLineSeg(origin: IntVec2): IntLineSeg = IntLineSeg(
        pointA = origin + IntVec2(0, minY),
        pointB = origin + IntVec2(0, maxY),
    )
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
        relativeMovementRange = movementRange.relativeMovementRange.sample(),
    )

    override fun exportElevatorRangeRect(): Rectangle {
        val movementRange = globalMovementRange.sample()
        return Rectangle(
            top = movementRange.minY,
            bottom = movementRange.maxY,
            left = 0,
            right = 0,
        )
    }
}

@Serializable
data class VerticalElevatorData(
    val position: IntVec2,
    val relativeMovementRange: VerticalRange = VerticalRange.ZERO,
)
