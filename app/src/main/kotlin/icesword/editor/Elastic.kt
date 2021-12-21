@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.RezIndex
import icesword.TILE_SIZE
import icesword.editor.elastic.prototype.ElasticPrototype
import icesword.editor.retails.Retail
import icesword.frp.Cell
import icesword.frp.DynamicMap
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.diffMap
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.map
import icesword.frp.map
import icesword.frp.reactTill
import icesword.frp.update
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.tileTopLeftCorner
import icesword.wwd.Wwd
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

data class ElasticWapObjectBuildContext(
    val tileBounds: IntRect,
) {
    val position: IntVec2
        get() = tileTopLeftCorner(tileBounds.topLeft)

    val size: IntSize
        get() = tileBounds.size
}

interface ElasticMetaTilesGenerator {
    fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile>

    fun buildWapObjects(size: IntSize): List<WapObjectPropsData> = emptyList()
}

private data class ElasticGeneratorOutput(
    val localMetaTiles: Map<IntVec2, MetaTile>,
    val localWapObjects: List<WapObjectPropsData>,
)

class Elastic(
    rezIndex: RezIndex,
    retail: Retail,
    private val prototype: ElasticPrototype,
    private val generator: ElasticMetaTilesGenerator,
    initialBounds: IntRect,
) :
    Entity(),
    EntityPosition,
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

    inner class BoundsEntityPosition : EntityPosition {
        override val position: Cell<IntVec2> by lazy {
            tileBounds.map { it.topLeft * TILE_SIZE }
        }

        override fun setPosition(newPosition: IntVec2) {
            _tileBounds.update { b: IntRect ->
                b.copy(position = newPosition.divRound(TILE_SIZE))
            }
        }
    }

    private val _tileBounds = MutCell(initialBounds)

    val tileBounds: Cell<IntRect>
        get() = _tileBounds

    val pixelBounds = tileBounds.map { it * TILE_SIZE }

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
        val initialTileCoord = sampleTileCoord(tileBounds.sample())
        val tileCoord = deltaTileCoord.map { initialTileCoord + it }

        tileCoord.reactTill(till) { tc ->
            val oldRect = tileBounds.sample()
            val newRect = transform(oldRect, tc)

            if (newRect != oldRect) {
                _tileBounds.set(newRect)
            }
        }
    }

    val size = _tileBounds.map { it.size }

    private val boundsTopLeft = tileBounds.map { it.topLeft }

    private val generatorOutput = size.map {
        ElasticGeneratorOutput(
            localMetaTiles = generator.buildMetaTiles(size = it),
            localWapObjects = generator.buildWapObjects(size = it),
        )
    }

    private val localWapObjects: DynamicList<WapObjectPropsData> =
        generatorOutput.diffMap { it.localWapObjects }

    val metaTileCluster = MetaTileCluster(
        tileOffset = boundsTopLeft,
        localMetaTiles = DynamicMap.diff(
            generatorOutput.map { it.localMetaTiles },
            tag = "metaTileCluster.localMetaTilesDynamic",
        )
    )

    val wapObjectSprites = this.localWapObjects.map { localWapObject ->
        DynamicWapSprite.fromImageSet(
            rezIndex = rezIndex,
            imageSetId = expandImageSetId(
                retail = retail,
                shortImageSetId = localWapObject.imageSet,
            ),
            position = position.map { it + localWapObject.position },
            i = localWapObject.i,
        )
    }

    override fun isSelectableIn(area: IntRect): Boolean =
        (tileBounds.sample() * TILE_SIZE).overlaps(area)

    override fun toString(): String = "Elastic(boundsTopLeft=${boundsTopLeft.sample()})"

    override fun exportWapObjects(): List<Wwd.Object_> {
        val position = this.position.sample()

        return localWapObjects.volatileContentView.map { wapObjectProps ->
            wapObjectProps.copy(
                x = position.x + wapObjectProps.x,
                y = position.y + wapObjectProps.y,
            ).toWwdObject()
        }
    }

    fun toData(): ElasticData =
        ElasticData(
            prototype = prototype,
            bounds = tileBounds.sample(),
        )
}

@Serializable
data class ElasticData(
    val prototype: ElasticPrototype,
    val bounds: IntRect,
)
