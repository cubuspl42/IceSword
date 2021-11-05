@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.RezIndex
import icesword.editor.WapObjectPrototype.FloorSpikePrototype
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.MutableDynamicList
import icesword.frp.dynamic_list.lastNow
import icesword.frp.map
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.Wwd
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class FloorSpikeRow(
    private val rezIndex: RezIndex,
    initialPosition: IntVec2,
    initialSpikeConfigs: List<FloorSpikeConfig>,
) : Entity(), WapObjectExportable {
    companion object {
        fun load(
            rezIndex: RezIndex,
            data: FloorSpikeRowData,
        ): FloorSpikeRow =
            FloorSpikeRow(
                rezIndex = rezIndex,
                initialPosition = data.position,
                initialSpikeConfigs = data.spikes.map {
                    FloorSpikeConfig(
                        initialStartDelayMillis = it.startDelayMillis,
                        initialTimeOffMillis = it.timeOffMillis,
                        initialTimeOnMillis = it.timeOnMillis,
                    )
                }
            )
    }

    class FloorSpikeConfig(
        initialStartDelayMillis: Int,
        initialTimeOffMillis: Int,
        initialTimeOnMillis: Int,
    ) {
        val startDelayMillis = MutCell(initialStartDelayMillis)

        val timeOffMillis = MutCell(initialTimeOffMillis)

        val timeOnMillis = MutCell(initialTimeOnMillis)
    }

    data class OutputSpike(
        val config: FloorSpikeConfig,
        val position: IntVec2,
        val bounds: IntRect,
    )

    data class OutputRow(
        val spikes: List<OutputSpike>,
    )

    val spikeImageMetadata = rezIndex.getImageMetadata(
        imageSetId = FloorSpikePrototype.imageSetId,
        i = -1,
    )!!

    private fun buildSpikes(
        configs: List<FloorSpikeConfig>,
        position: IntVec2,
    ): List<OutputSpike> =
        configs.firstOrNull()?.let { config ->
            val spike = OutputSpike(
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

    private val _spikeConfigs = MutableDynamicList(
        initialContent = initialSpikeConfigs,
    )

    val spikeConfigs: DynamicList<FloorSpikeConfig> = _spikeConfigs

    val outputRow = Cell.map2(
        entityPosition.position,
        _spikeConfigs.content,
    ) { position, configs ->
        OutputRow(
            spikes = buildSpikes(
                configs = configs,
                position = position,
            ),
        )
    }

    fun addSpike() {
        val lastConfig = _spikeConfigs.lastNow()

        _spikeConfigs.add(FloorSpikeConfig(
            initialStartDelayMillis = lastConfig.startDelayMillis.sample(),
            initialTimeOffMillis = lastConfig.timeOffMillis.sample(),
            initialTimeOnMillis = lastConfig.timeOnMillis.sample(),
        ))
    }

    fun removeSpike(
        floorSpikeConfig: FloorSpikeConfig,
    ) {
        _spikeConfigs.remove(floorSpikeConfig)
    }

    val boundingBox = outputRow.map {
        IntRect.enclosing(rects = it.spikes.map { it.bounds })
    }

    override fun isSelectableIn(area: IntRect): Boolean =
        boundingBox.sample().overlaps(area)

    override fun exportWapObjects(): List<Wwd.Object_> {
        val outputROw = outputRow.sample()
        return outputROw.spikes.map {
            val config = it.config
            val position = it.position

            FloorSpikePrototype.wwdObjectPrototype.copy(
                x = position.x,
                y = position.y,
                speed = config.startDelayMillis.sample(),
                speedX = config.timeOnMillis.sample(),
                speedY = config.timeOffMillis.sample(),
            )
        }
    }

    fun toData(): FloorSpikeRowData = FloorSpikeRowData(
        position = entityPosition.position.sample(),
        spikes = outputRow.sample().spikes.map {
            FloorSpikeData(
                startDelayMillis = it.config.startDelayMillis.sample(),
                timeOffMillis = it.config.timeOffMillis.sample(),
                timeOnMillis = it.config.timeOnMillis.sample(),
            )
        }
    )
}

@Serializable
data class FloorSpikeData(
    val startDelayMillis: Int,
    val timeOffMillis: Int,
    val timeOnMillis: Int,
)

@Serializable
data class FloorSpikeRowData(
    val position: IntVec2,
    val spikes: List<FloorSpikeData> = emptyList(),
)
