@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.RezIndex
import icesword.editor.WapObjectPrototype.FloorSpikePrototype
import icesword.frp.map
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.Wwd
import kotlinx.css.del
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class FloorSpikeRow(
    private val rezIndex: RezIndex,
    initialPosition: IntVec2,
) : Entity(), WapObjectExportable {

    companion object {
        fun load(
            rezIndex: RezIndex,
            data: FloorSpikeRowData,
        ): FloorSpikeRow =
            FloorSpikeRow(
                rezIndex = rezIndex,
                initialPosition = data.position,
            )
    }

    data class FloorSpike(
        val position: IntVec2,
        val bounds: IntRect,
    )

    val spikeImageMetadata = rezIndex.getImageMetadata(
        imageSetId = FloorSpikePrototype.imageSetId,
        i = -1,
    )!!

    private fun buildSpikes(
        spikeCount: Int,
        position: IntVec2,
    ): List<FloorSpike> {
        val head = FloorSpike(
            position = position,
            bounds = calculateWapSpriteBounds(
                imageMetadata = spikeImageMetadata,
                position = position,
            )
        )

        return if (spikeCount > 1) {
            val gapWidth = 4

            val delta = IntVec2(
                x = spikeImageMetadata.size.width + gapWidth,
                y = 0,
            )

            listOf(head) + buildSpikes(
                spikeCount = spikeCount - 1,
                position = position + delta,
            )
        } else {
            listOf(head)
        }
    }

    override val entityPosition: EntityPosition =
        EntityPixelPosition(
            initialPosition = initialPosition,
        )

    val spikes = entityPosition.position.map {
        buildSpikes(spikeCount = 5, position = it)
    }

    val boundingBox = spikes.map { spikes ->
        IntRect.enclosing(
            rects = spikes.map { it.bounds },
        )
    }

    override fun isSelectableIn(area: IntRect): Boolean =
        boundingBox.sample().overlaps(area)

    override fun exportWapObjects(): List<Wwd.Object_> =
        spikes.sample().map {
            FloorSpikePrototype.wwdObjectPrototype.copy(
                x = it.position.x,
                y = it.position.y,
            )
        }

    fun toData(): FloorSpikeRowData = FloorSpikeRowData(
        position = entityPosition.position.sample(),
    )
}

@Serializable
data class FloorSpikeRowData(
    val position: IntVec2,
)
