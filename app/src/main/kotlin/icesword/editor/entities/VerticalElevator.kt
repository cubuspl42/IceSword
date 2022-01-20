@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor.entities

import icesword.RezIndex
import icesword.editor.IntVec2Serializer
import icesword.editor.retails.Retail
import icesword.geometry.IntLineSeg
import icesword.geometry.IntVec2
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
    prototype: ElevatorPrototype,
    initialPosition: IntVec2,
    initialZOrder: Int,
    initialRelativeMovementRange: VerticalRange,
) : Elevator<VerticalRange>(
    rezIndex = rezIndex,
    prototype = prototype,
    initialPosition = initialPosition,
    initialZOrder = initialZOrder,
    initialRelativeMovementRange = initialRelativeMovementRange,
), WapObjectExportable {

    companion object {
        fun load(
            rezIndex: RezIndex,
            retail: Retail,
            data: VerticalElevatorData,
        ): VerticalElevator =
            VerticalElevator(
                rezIndex = rezIndex,
                prototype = retail.elevatorPrototype,
                initialPosition = data.position,
                initialZOrder = data.zOrder,
                initialRelativeMovementRange = data.relativeMovementRange,
            )
    }

    fun toData(): VerticalElevatorData = VerticalElevatorData(
        position = entityPosition.position.sample(),
        zOrder = asZOrderedEntity.zOrder.sample(),
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
    val zOrder: Int = 0,
    val relativeMovementRange: VerticalRange = VerticalRange.ZERO,
)
