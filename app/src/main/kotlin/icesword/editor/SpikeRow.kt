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

class SpikeRow(
    rezIndex: RezIndex,
    initialPosition: IntVec2,
    initialRelativeMovementRange: VerticalRange,
) : Entity(), WapObjectExportable {

    companion object {
        fun load(
            rezIndex: RezIndex,
            data: ElevatorData,
        ): Elevator =
            Elevator(
                rezIndex = rezIndex,
                initialPosition = data.position,
                initialRelativeMovementRange = data.relativeMovementRange,
            )
    }

    override val entityPosition: EntityPosition =
        EntityPixelPosition(
            initialPosition = initialPosition,
        )

    val wapObjectStem = WapObjectStem(
        rezIndex = rezIndex,
        wapObjectPrototype = ElevatorPrototype,
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
        extremum: (VerticalRange) -> Int,
        copy: (it: VerticalRange, extremum: Int) -> VerticalRange,
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
        val hitBox = wapObjectStem.boundingBox.sample()
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

    fun toData(): ElevatorData = ElevatorData(
        position = entityPosition.position.sample(),
        relativeMovementRange = relativeMovementRange.sample(),
    )
}

@Serializable
data class ElevatorData(
    val position: IntVec2,
    val relativeMovementRange: VerticalRange = VerticalRange.ZERO,
)
