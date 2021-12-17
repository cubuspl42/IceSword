@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.JsonRezIndex
import icesword.RezIndex
import icesword.TILE_SIZE
import icesword.editor.retails.Retail
import icesword.editor.retails.Retail3
import icesword.editor.retails.RetailLadderPrototype
import icesword.editor.retails.doublePilePattern
import icesword.editor.retails.retail1.retail1ColumnPattern
import icesword.editor.retails.retail2PlatformPattern
import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.tileTopLeftCorner
import kotlinx.serialization.SerialName
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

interface ElasticGenerator {
    fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile>

    fun buildWapObject(): WapObjectPropsData? = null
}

@Serializable
sealed class ElasticPrototype {
    abstract val defaultSize: IntSize

    abstract fun buildGenerator(retail: Retail): ElasticGenerator
}

@Serializable
@SerialName("Log")
object LogPrototype : ElasticPrototype() {
    private object Generator : ElasticGenerator {
        override fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile> =
            (0 until size.height).flatMap(::logLevel).toMap()
    }

    private fun logLevel(i: Int): Set<Pair<IntVec2, MetaTile>> = setOf(
        IntVec2(-1, i) to MetaTile.LogLeft,
        IntVec2(0, i) to MetaTile.Log,
        IntVec2(1, i) to MetaTile.LogRight,
    )

    override val defaultSize: IntSize = IntSize(1, 4)

    override fun buildGenerator(retail: Retail): ElasticGenerator {
        if (retail !is Retail3) throw UnsupportedOperationException()
        return Generator
    }
}

@Serializable
@SerialName("TreeCrown")
object TreeCrownPrototype : ElasticPrototype() {
    private object Generator : ElasticGenerator {
        override fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile> {
            val columns =
                listOf(
                    setOf(
                        IntVec2(0, 0) to MetaTile.LeavesUpperLeft,
                        IntVec2(0, 1) to MetaTile.LeavesLowerLeft,
                    ),
                ) +
                        (1..(size.width - 2)).map { j ->

                            setOf(
                                IntVec2(j, 0) to MetaTile.LeavesUpper,
                                IntVec2(j, 1) to MetaTile.LeavesLower,
                            )
                        } +
                        listOf(
                            setOf(
                                IntVec2(size.width - 1, 0) to MetaTile.LeavesUpperRight,
                                IntVec2(size.width - 1, 1) to MetaTile.LeavesLowerRight,
                            ),
                        )

            return columns.take(size.width).flatten().toMap()
        }
    }

    override val defaultSize: IntSize = IntSize(5, 2)

    override fun buildGenerator(retail: Retail): ElasticGenerator {
        if (retail !is Retail3) throw UnsupportedOperationException()
        return Generator
    }
}

@Serializable
@SerialName("Ladder")
object LadderPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(1, 4)

    override fun buildGenerator(retail: Retail): ElasticGenerator {
        if (retail !is RetailLadderPrototype) throw UnsupportedOperationException()
        return retail.ladderGenerator
    }
}

@Serializable
@SerialName("Spikes")
object SpikesPrototype : ElasticPrototype() {
    private object Generator : ElasticGenerator {
        override fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile> {
            val n = size.width
            return (0 until n).flatMap {
                listOf(
                    IntVec2(it, 0) to MetaTile.SpikeTop,
                    IntVec2(it, 1) to MetaTile.SpikeBottom,
                )
            }.toMap()
        }
    }

    override val defaultSize: IntSize = IntSize(4, 2)

    override fun buildGenerator(retail: Retail): ElasticGenerator {
        if (retail !is Retail3) throw UnsupportedOperationException()
        return Generator
    }
}

@Serializable
@SerialName("Level2Platform")
object PlatformPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(5, 2)

    private val generator = retail2PlatformPattern.toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}

@Serializable
@SerialName("Level2DoublePile")
object DoublePilePrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(1, 3)

    private val generator = doublePilePattern.toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}

@Serializable
@SerialName("Level1Column")
object ColumnPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(1, 4)

    private val generator = retail1ColumnPattern.toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}

class Elastic(
    rezIndex: RezIndex,
    retail: Retail,
    private val prototype: ElasticPrototype,
    private val generator: ElasticGenerator,
    initialBounds: IntRect,
) :
    Entity(),
    EntityPosition {

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

    val metaTileCluster = MetaTileCluster(
        tileOffset = boundsTopLeft,
        localMetaTiles = DynamicMap.diff(
            size.map { generator.buildMetaTiles(it) },
            tag = "metaTileCluster.localMetaTilesDynamic",
        )
    )

    val wapObjectProps = generator.buildWapObject()

    val wapObjectSprite = wapObjectProps?.let { props ->
        DynamicWapSprite.fromImageSet(
            rezIndex = rezIndex,
            imageSetId = expandImageSetId(
                retail = retail,
                shortImageSetId = props.imageSet,
            ),
            position = position.map {
                it + IntVec2(props.x, props.y)
            },
            i = props.i,
        )
    }

    override fun isSelectableIn(area: IntRect): Boolean =
        (tileBounds.sample() * TILE_SIZE).overlaps(area)

    override fun toString(): String = "MetaTileCluster(boundsTopLeft=${boundsTopLeft.sample()})"

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
