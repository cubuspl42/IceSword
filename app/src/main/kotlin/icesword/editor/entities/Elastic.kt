@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor.entities

import icesword.RezIndex
import icesword.TILE_SIZE
import icesword.editor.IntVec2Serializer
import icesword.editor.MetaTile
import icesword.editor.entities.elastic.ElasticRectangularFragment
import icesword.editor.entities.elastic.ElasticRectangularPattern
import icesword.editor.entities.elastic.prototype.ElasticPrototype
import icesword.editor.retails.Retail
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.reactTill
import icesword.frp.update
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.wwd.Wwd
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

interface ElasticGenerator {
    companion object {
        fun fromHorizontalPattern(
            left: ElasticRectangularFragment? = null,
            center: ElasticRectangularFragment? = null,
            right: ElasticRectangularFragment? = null,
            staticHeight: Int,
        ): ElasticGenerator = ElasticRectangularPattern(
            topLeft = left,
            topCenter = center,
            topRight = right,
            leftStaticWidth = left?.width ?: 0,
            centerHorizontalRepeatingWidth = center?.width ?: 0,
            rightStaticWidth = right?.width ?: 0,
            topStaticHeight = staticHeight,
        ).toElasticGenerator()

        fun fromVerticalPattern(
            top: ElasticRectangularFragment? = null,
            center: ElasticRectangularFragment? = null,
            bottom: ElasticRectangularFragment? = null,
            staticWidth: Int,
        ): ElasticGenerator = ElasticRectangularPattern(
            topLeft = top,
            centerLeft = center,
            bottomLeft = bottom,
            topStaticHeight = top?.height ?: 0,
            centerVerticalRepeatingHeight = center?.height ?: 0,
            bottomStaticHeight = bottom?.height ?: 0,
            leftStaticWidth = staticWidth,
        ).toElasticGenerator()
    }

    fun buildOutput(size: IntSize): ElasticGeneratorOutput
}

abstract class ElasticMetaTilesGenerator : ElasticGenerator {
    override fun buildOutput(size: IntSize): ElasticGeneratorOutput =
        ElasticGeneratorOutput(
            localMetaTiles = buildMetaTiles(size = size),
            localWapObjects = emptyList(),
        )

    abstract fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile>
}

data class ElasticGeneratorOutput(
    val localMetaTiles: Map<IntVec2, MetaTile>,
    val localWapObjects: List<WapObjectPropsData>,
)

class Elastic(
    rezIndex: RezIndex,
    retail: Retail,
    private val prototype: ElasticPrototype,
    private val generator: ElasticGenerator,
    initialBounds: IntRect,
) :
    Entity(),
    WapObjectExportable {

    companion object {
        fun load(
            rezIndex: RezIndex,
            retail: Retail,
            data: ElasticData,
        ): Elastic =
            Elastic(
                rezIndex = rezIndex,
                retail = retail,
                prototype = data.prototype,
                generator = data.prototype.buildGenerator(retail = retail),
                initialBounds = data.bounds,
            )
    }

    inner class BoundsEntityPosition : EntityTilePosition() {
        override val tileOffset: Cell<IntVec2> by lazy {
            _tileBounds.map { it.topLeft }
        }

        override fun setTileOffset(tileOffset: IntVec2) {
            _tileBounds.update { b: IntRect ->
                b.copy(position = tileOffset)
            }
        }
    }

    private val _tileBounds = MutCell(initialBounds)

    val product = ElasticProduct(
        rezIndex = rezIndex,
        retail = retail,
        generator = generator,
        tileBounds = _tileBounds,
    )

    override val entityPosition = BoundsEntityPosition()

    fun resizeTopLeft(deltaTileCoord: Cell<IntVec2>, till: Till) = resize(
        deltaTileCoord = deltaTileCoord,
        sampleTileCoord = IntRect::topLeft,
        transform = { rect, tc -> rect.copyWithTopLeft(tc) },
        till = till,
    )

    fun resizeTopRight(deltaTileCoord: Cell<IntVec2>, till: Till) = resize(
        deltaTileCoord = deltaTileCoord,
        sampleTileCoord = IntRect::topRight,
        transform = { rect, tc -> rect.copyWithTopRight(tc) },
        till = till,
    )

    fun resizeBottomRight(deltaTileCoord: Cell<IntVec2>, till: Till) = resize(
        deltaTileCoord = deltaTileCoord,
        sampleTileCoord = IntRect::bottomRight,
        transform = { rect, tc -> rect.copyWithBottomRight(tc) },
        till = till,
    )

    fun resizeBottomLeft(deltaTileCoord: Cell<IntVec2>, till: Till) = resize(
        deltaTileCoord = deltaTileCoord,
        sampleTileCoord = IntRect::bottomLeft,
        transform = { rect, tc -> rect.copyWithBottomLeft(tc) },
        till = till,
    )

    private fun resize(
        deltaTileCoord: Cell<IntVec2>,
        sampleTileCoord: (rect: IntRect) -> IntVec2,
        transform: (rect: IntRect, tileCoord: IntVec2) -> IntRect,
        till: Till,
    ) {
        val initialTileCoord = sampleTileCoord(_tileBounds.sample())
        val tileCoord = deltaTileCoord.map { initialTileCoord + it }

        tileCoord.reactTill(till) { tc ->
            val oldRect = _tileBounds.sample()
            val newRect = transform(oldRect, tc)

            if (newRect != oldRect) {
                _tileBounds.set(newRect)
            }
        }
    }

    val size = _tileBounds.map { it.size }

    override fun isSelectableIn(area: IntRect): Boolean =
        (product.tileBounds.sample() * TILE_SIZE).overlaps(area)

    override fun toString(): String = "Elastic(boundsTopLeft=${product.boundsTopLeft.sample()})"

    override fun exportWapObjects(): List<Wwd.Object_> {
        val position = this.position.sample()

        return product.localWapObjects.volatileContentView.map { wapObjectProps ->
            wapObjectProps.copy(
                x = position.x + wapObjectProps.x,
                y = position.y + wapObjectProps.y,
            ).toWwdObject()
        }
    }

    fun toData(): ElasticData =
        ElasticData(
            prototype = prototype,
            bounds = _tileBounds.sample(),
        )
}

@Serializable
data class ElasticData(
    val prototype: ElasticPrototype,
    val bounds: IntRect,
)
