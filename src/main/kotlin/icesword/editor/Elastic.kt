package icesword.editor

import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.tileAtPoint
import kotlinx.css.col
import kotlinx.css.del

sealed class ElasticPrototype {
//    abstract val metaTiles: Map<IntVec2, MetaTile>

    abstract val defaultSize: IntSize

    abstract fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile>
}

object LogPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(1, 4)

    override fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile> =
        (0 until size.height).flatMap(::logLevel).toMap()

}

object TreeCrownPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(5, 2)

    override fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile> {

        val columns =
            listOf(
                setOf(
                    IntVec2(0, 0) to MetaTile.LEAVES_UPPER_LEFT,
                    IntVec2(0, 1) to MetaTile.LEAVES_LOWER_LEFT,
                ),
            ) +
                    (1..(size.width - 2)).map { j ->

                        setOf(
                            IntVec2(j, 0) to MetaTile.LEAVES_UPPER,
                            IntVec2(j, 1) to MetaTile.LEAVES_LOWER,
                        )
                    } +
                    listOf(
                        setOf(
                            IntVec2(size.width - 1, 0) to MetaTile.LEAVES_UPPER_RIGHT,
                            IntVec2(size.width - 1, 1) to MetaTile.LEAVES_LOWER_RIGHT,
                        ),
                    )

        return columns.take(size.width).flatten().toMap()
    }
}

private fun logLevel(i: Int): Set<Pair<IntVec2, MetaTile>> = setOf(
    IntVec2(-1, i) to MetaTile.LOG_LEFT,
    IntVec2(0, i) to MetaTile.LOG,
    IntVec2(1, i) to MetaTile.LOG_RIGHT,
)


class Elastic(
    prototype: ElasticPrototype,
    initialBounds: IntRect,
) :
    Entity(),
    EntityTileOffset {

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

    val metaTileCluster = MetaTileCluster(
        tileOffset = tileOffset,
        localMetaTilesDynamic = DynamicMap.diff(
            size.map { prototype.buildMetaTiles(it) },
        )

//            .also {
//                it.changes.subscribe {
//                    println("Elastic localMetaTilesDynamic change: $it")
//                }
//            }
    )

    override fun isSelectableAt(worldPoint: IntVec2): Boolean {
        val globalTileCoord = tileAtPoint(worldPoint)
        return metaTileCluster.getMetaTileAt(globalTileCoord).sample() != null
    }

    override val tileOffset: Cell<IntVec2>
        get() = _bounds.map { it.position }

    override fun setTileOffset(newOffset: IntVec2) {
        _bounds.update { b: IntRect -> b.copy(position = newOffset) }
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
}

