@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor.entities

import icesword.RezIndex
import icesword.editor.IntVec2Serializer
import icesword.editor.retails.Retail
import icesword.geometry.IntLineSeg
import icesword.geometry.IntVec2
import icesword.wwd.Geometry
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

    override fun translate(t: IntVec2): HorizontalRange =
        HorizontalRange(minX + t.x, maxX + t.x)

    override fun copyWithMin(min: Int): HorizontalRange =
        this.copy(minX = min)

    override fun copyWithMax(max: Int): HorizontalRange =
        this.copy(maxX = max)

    override fun toLineSeg(origin: IntVec2): IntLineSeg = IntLineSeg(
        pointA = origin + IntVec2(minX, 0),
        pointB = origin + IntVec2(maxX, 0),
    )
}

class HorizontalElevator(
    rezIndex: RezIndex,
    prototype: ElevatorPrototype,
    initialPosition: IntVec2,
    initialRelativeMovementRange: HorizontalRange,
) : Elevator<HorizontalRange>(
    rezIndex = rezIndex,
    prototype = prototype,
    initialPosition = initialPosition,
    initialRelativeMovementRange = initialRelativeMovementRange,
), WapObjectExportable {
    companion object {
        fun load(
            rezIndex: RezIndex,
            retail: Retail,
            data: HorizontalElevatorData,
        ): HorizontalElevator =
            HorizontalElevator(
                rezIndex = rezIndex,
                prototype = retail.elevatorPrototype,
                initialPosition = data.position,
                initialRelativeMovementRange = data.relativeMovementRange,
            )
    }

    fun toData(): HorizontalElevatorData = HorizontalElevatorData(
        position = entityPosition.position.sample(),
        relativeMovementRange = movementRange.relativeMovementRange.sample(),
    )

    override fun exportElevatorRangeRect(): Geometry.Rectangle {
        val movementRange = globalMovementRange.sample()
        return Geometry.Rectangle(
            left = movementRange.minX,
            right = movementRange.maxX,
            top = 0,
            bottom = 0,
        )
    }
}

@Serializable
data class HorizontalElevatorData(
    val position: IntVec2,
    val relativeMovementRange: HorizontalRange = HorizontalRange.ZERO,
)