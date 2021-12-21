package icesword.editor.elastic

import icesword.editor.ElasticGenerator
import icesword.editor.MetaTile
import icesword.editor.WapObjectPropsData
import icesword.geometry.IntSize
import icesword.geometry.IntVec2


data class LinearMetaTilePattern(
    val metaTiles: List<MetaTile>,
    val width: Int,
) {
    companion object {
        fun empty() = LinearMetaTilePattern(
            metaTiles = emptyList(),
            width = 0,
        )
    }

    init {
        if (metaTiles.isNotEmpty() && metaTiles.size % width != 0) throw IllegalArgumentException()
    }

    val length = metaTiles.size / width

    fun getLayer(layerIndex: Int): List<MetaTile> {
        val fromIndex = layerIndex * width
        val toIndex = fromIndex + width

        return metaTiles.subList(
            fromIndex = fromIndex,
            toIndex = toIndex,
        )
    }

    // -1 is the last one, -2 is one befoe last, etc.
    fun getLayerBackward(backwardLayerIndex: Int): List<MetaTile> {
        if (backwardLayerIndex >= 0) throw IllegalArgumentException()
        return getLayer(length + backwardLayerIndex)
    }

    fun getLayerModulo(layerIndex: Int): List<MetaTile> =
        getLayer(layerIndex % length)
}

enum class ElasticLinearPatternOrientation {
    Horizontal,
    Vertical,
}

data class ElasticLinearPattern(
    val startingPattern: LinearMetaTilePattern,
    val repeatingPattern: LinearMetaTilePattern,
    val endingPattern: LinearMetaTilePattern,
    val wapObject: WapObjectPropsData? = null,
    val orientation: ElasticLinearPatternOrientation,
) {
    fun toElasticGenerator(): ElasticGenerator = object : ElasticGenerator {
        private fun buildLayer(
            // Layer index in the length axis
            layerIndex: Int,
            requestedLength: Int,
        ): List<Pair<IntVec2, MetaTile>> = when {
            layerIndex < startingPattern.length -> startingPattern.getLayer(layerIndex)
            layerIndex >= requestedLength - endingPattern.length -> endingPattern.getLayerBackward(layerIndex - requestedLength)
            else -> repeatingPattern.getLayerModulo(layerIndex - startingPattern.length)
        }.mapIndexed { i, metaTile ->
            when (orientation) {
                ElasticLinearPatternOrientation.Horizontal -> IntVec2(layerIndex, i) to metaTile
                ElasticLinearPatternOrientation.Vertical -> IntVec2(i, layerIndex) to metaTile
            }
        }

        private fun buildLayers(length: Int): Map<IntVec2, MetaTile> =
            (0 until length).flatMap { buildLayer(it, requestedLength = length) }.toMap()

        override fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile> {
            val length = when (orientation) {
                ElasticLinearPatternOrientation.Horizontal -> size.width
                ElasticLinearPatternOrientation.Vertical -> size.height
            }

            return buildLayers(length = length)
        }

        override fun buildWapObjects(size: IntSize): List<WapObjectPropsData> =
            listOfNotNull(wapObject)
    }
}
