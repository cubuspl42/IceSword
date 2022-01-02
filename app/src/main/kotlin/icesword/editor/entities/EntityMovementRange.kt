@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor.entities

import icesword.editor.IntVec2Serializer
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.reactTill
import icesword.geometry.IntLineSeg
import icesword.geometry.IntVec2
import kotlinx.serialization.UseSerializers

interface AxisRange<Range> {
    val min: Int

    val max: Int

    fun translate(t: IntVec2): Range

    fun copyWithMin(min: Int): Range

    fun copyWithMax(max: Int): Range

    fun toLineSeg(origin: IntVec2): IntLineSeg
}

interface EntityMovementRange<Range : AxisRange<Range>> {
    val movementOrigin: Cell<IntVec2>

    val relativeMovementRange: Cell<Range>

    val movementLine: Cell<IntLineSeg>

    fun resizeMovementRangeMin(
        minDelta: Cell<Int>,
        till: Till,
    )

    fun resizeMovementRangeMax(
        maxDelta: Cell<Int>,
        till: Till,
    )
}

class EntityMovementRangeMixin<Range : AxisRange<Range>>(
    override val movementOrigin: Cell<IntVec2>,
    initialRelativeMovementRange: Range,
) : EntityMovementRange<Range> {
    private val _relativeMovementRange = MutCell(
        initialValue = initialRelativeMovementRange,
    )

    override val relativeMovementRange: Cell<Range>
        get() = _relativeMovementRange

    override val movementLine: Cell<IntLineSeg> = Cell.map2(
        movementOrigin,
        relativeMovementRange,
    ) { mo, mr ->
        mr.toLineSeg(mo)
    }

    override fun resizeMovementRangeMin(
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

    override fun resizeMovementRangeMax(
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
                _relativeMovementRange.set(newRange)
            }
        }
    }
}
