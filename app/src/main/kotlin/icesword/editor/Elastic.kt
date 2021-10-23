@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.TILE_SIZE
import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.tileAtPoint
import icesword.tileRect
import icesword.tilesInArea
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
sealed class ElasticPrototype {
    abstract val defaultSize: IntSize

    abstract fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile>
}

@Serializable
@SerialName("Log")
object LogPrototype : ElasticPrototype() {
    private fun logLevel(i: Int): Set<Pair<IntVec2, MetaTile>> = setOf(
        IntVec2(-1, i) to MetaTile.LogLeft,
        IntVec2(0, i) to MetaTile.Log,
        IntVec2(1, i) to MetaTile.LogRight,
    )

    override val defaultSize: IntSize = IntSize(1, 4)

    override fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile> =
        (0 until size.height).flatMap(::logLevel).toMap()
}

@Serializable
@SerialName("TreeCrown")
object TreeCrownPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(5, 2)

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


@Serializable
@SerialName("Ladder")
object LadderPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(1, 4)

    override fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile> {
        val n = size.height
        return (0 until n).associate {
            val metaTile = when (it) {
                0 -> MetaTile.LadderTop
                n - 1 -> MetaTile.LadderBottom
                else -> MetaTile.Ladder
            }
            IntVec2(0, it) to metaTile
        }
    }
}

@Serializable
@SerialName("Spikes")
object SpikesPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(4, 2)

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

class Elastic(
    private val prototype: ElasticPrototype,
    initialBounds: IntRect,
) :
    Entity(),
    EntityTileOffset {

    companion object {
        fun load(data: ElasticData): Elastic =
            Elastic(
                prototype = data.prototype,
                initialBounds = data.bounds,
            )
    }

    private val _bounds = MutCell(initialBounds)

    val bounds: Cell<IntRect>
        get() = _bounds

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
        val initialTileCoord = sampleTileCoord(bounds.sample())
        val tileCoord = deltaTileCoord.map { initialTileCoord + it }

        tileCoord.reactTill(till) { tc ->
            val oldRect = bounds.sample()
            val newRect = transform(oldRect, tc)

            if (newRect != oldRect) {
                _bounds.set(newRect)
            }
        }
    }

    val size = _bounds.map { it.size }

    // TODO: Nuke?
    override val tileOffset: Cell<IntVec2> = bounds.map { it.topLeft }

    val metaTileCluster = MetaTileCluster(
        tileOffset = tileOffset,
        localMetaTiles = DynamicMap.diff(
            size.map { prototype.buildMetaTiles(it) },
            tag = "metaTileCluster.localMetaTilesDynamic",
        )

//            .also {
//                it.changes.subscribe {
//                    println("Elastic localMetaTilesDynamic change: $it")
//                }
//            }
    )

    override fun isSelectableIn(area: IntRect): Boolean {
        val tilesCoords = tilesInArea(area)
        return  tilesCoords.any { metaTileCluster.getMetaTileAt(it).sample() != null }
    }

    override val position: Cell<IntVec2> by lazy {
        bounds.map { it.topLeft * TILE_SIZE }
    }

    override fun setPosition(newPosition: IntVec2) {
        _bounds.update { b: IntRect -> b.copy(position = newPosition.divRound(TILE_SIZE)) }
    }

    override fun toString(): String = "MetaTileCluster(tileOffset=${tileOffset.sample()})"

    fun expandRight() {
        _bounds.update { b: IntRect ->
            val oldSize = b.size

            b.copy(
                size = oldSize.copy(width = oldSize.width + 1),
            )
        }
    }

    fun toData(): ElasticData =
        ElasticData(
            prototype = prototype,
            bounds = bounds.sample(),
        )
}

@Serializable
data class ElasticData(
    val prototype: ElasticPrototype,
    val bounds: IntRect,
)
