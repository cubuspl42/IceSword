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
import icesword.wwd.Geometry
import icesword.wwd.Geometry.Rectangle
import icesword.wwd.Wwd
import kotlinx.serialization.UseSerializers

interface AxisRange<Range> {
    val min: Int

    val max: Int

    fun translate(t: IntVec2): Range

    fun copyWithMin(min: Int): Range

    fun copyWithMax(max: Int): Range
}

abstract class Elevator<Range : AxisRange<Range>>(
    rezIndex: RezIndex,
    initialPosition: IntVec2,
    initialRelativeMovementRange: Range,
) : Entity(), WapObjectExportable {

    final override val entityPosition: EntityPosition =
        EntityPixelPosition(
            initialPosition = initialPosition,
        )

    val wapSprite = WapSprite(
        rezIndex = rezIndex,
        imageSetId = ElevatorPrototype.imageSetId,
        position = entityPosition.position,
    )

    val relativeMovementRange = MutCell(
        initialValue = initialRelativeMovementRange,
    )

    fun resizeMovementRangeMin(
        minDelta: Cell<Int>,
        till: Till,
    ) {
        resizeMovementRange(
            extremumDelta = minDelta,
            extremum = { it.min },
            copy = { range, min -> range.copyWithMin(min = min) },
            till = till,
        )
    }

    fun resizeMovementRangeMax(
        maxDelta: Cell<Int>,
        till: Till,
    ) {
        resizeMovementRange(
            extremumDelta = maxDelta,
            extremum = { it.max },
            copy = { range, max -> range.copyWithMax(max = max) },
            till = till,
        )
    }

    // Extremum is a fancy name for min-or-max, so this method can be used for
    // moving either end of the movement range.
    private fun resizeMovementRange(
        extremumDelta: Cell<Int>,
        extremum: (Range) -> Int,
        copy: (it: Range, extremum: Int) -> Range,
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
        mr.translate(ep)
    }

    final override fun isSelectableIn(area: IntRect): Boolean {
        val hitBox = wapSprite.boundingBox.sample()
        return hitBox.overlaps(area)
    }

    abstract fun exportElevatorRangeRect(): Rectangle

    final override fun exportWapObject(): Wwd.Object_ {
        val position = position.sample()

        return ElevatorPrototype.wwdObjectPrototype.copy(
            x = position.x,
            y = position.y,
            rangeRect = exportElevatorRangeRect(),
        )
    }
}
