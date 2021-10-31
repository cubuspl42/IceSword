@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.RezIndex
import icesword.editor.WapObjectPrototype.FloorSpikePrototype
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.map
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.utils.updated
import icesword.wwd.Wwd
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

    data class FloorSpikeConfig(
        val startDelayMillis: Int,
        val timeOffMillis: Int,
        val timeOnMillis: Int,
    )

    data class FloorSpike(
        val config: FloorSpikeConfig,
        val position: IntVec2,
        val bounds: IntRect,
    )

    val spikeImageMetadata = rezIndex.getImageMetadata(
        imageSetId = FloorSpikePrototype.imageSetId,
        i = -1,
    )!!

    private fun buildSpikes(
        configs: List<FloorSpikeConfig>,
        position: IntVec2,
    ): List<FloorSpike> =
        configs.firstOrNull()?.let { config ->
            val spike = FloorSpike(
                config = config,
                position = position,
                bounds = calculateWapSpriteBounds(
                    imageMetadata = spikeImageMetadata,
                    position = position,
                )
            )

            val gapWidth = 4

            val delta = IntVec2(
                x = spikeImageMetadata.size.width + gapWidth,
                y = 0,
            )

            listOf(spike) + buildSpikes(
                configs = configs.drop(1),
                position = position + delta,
            )
        } ?: emptyList()

    override val entityPosition: EntityPosition =
        EntityPixelPosition(
            initialPosition = initialPosition,
        )

    private val _spikeConfigs = MutCell(
        initialValue = listOf(
            FloorSpikeConfig(
                startDelayMillis = 0,
                timeOffMillis = 1500,
                timeOnMillis = 1500
            ),
            FloorSpikeConfig(
                startDelayMillis = 750,
                timeOffMillis = 1500,
                timeOnMillis = 1500
            ),
            FloorSpikeConfig(
                startDelayMillis = 1500,
                timeOffMillis = 1500,
                timeOnMillis = 1500
            ),
            FloorSpikeConfig(
                startDelayMillis = 2250,
                timeOffMillis = 1500,
                timeOnMillis = 1500
            ),
        )
    )

    fun getSpikeConfig(
        spikeIndex: Int,
    ): FloorSpikeConfig =
        _spikeConfigs.sample()[spikeIndex]

    fun updateSpikeConfig(
        spikeIndex: Int,
        config: FloorSpikeConfig,
    ) {
        val oldConfigs = _spikeConfigs.sample()
        _spikeConfigs.set(oldConfigs.updated(spikeIndex, config))
    }

    fun addSpike() {
        val oldConfigs = _spikeConfigs.sample()
        _spikeConfigs.set(oldConfigs + listOf(oldConfigs.last()))
    }

    fun removeSpike(
        spikeIndex: Int,
    ) {
        val oldConfigs = _spikeConfigs.sample()
        _spikeConfigs.set(oldConfigs.filterIndexed { index, _ -> index == spikeIndex })
    }

    val spikes = Cell.map2(
        _spikeConfigs,
        entityPosition.position,
    ) { spikeConfigs, position ->
        buildSpikes(
            configs = spikeConfigs,
            position = position,
        )
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
            val config = it.config

            FloorSpikePrototype.wwdObjectPrototype.copy(
                x = it.position.x,
                y = it.position.y,
                speed = config.startDelayMillis,
                speedX = config.timeOnMillis,
                speedY = config.timeOffMillis,
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
