package icesword.editor

import icesword.frp.Cell
import icesword.frp.DynamicMap
import icesword.frp.map
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.tileAtPoint

sealed class ElasticPrototype {
//    abstract val metaTiles: Map<IntVec2, MetaTile>

    abstract fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile>
}

object LogPrototype : ElasticPrototype() {
//    override val metaTiles: Map<IntVec2, MetaTile> =
//        (0..16).flatMap(::logLevel).toMap()

    override fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile> =
        (0 until size.height).flatMap(::logLevel).toMap()

}

object TreeCrownPrototype : ElasticPrototype() {
    override fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile> =
        mapOf(
            IntVec2(0, 0) to MetaTile.LEAVES_UPPER_LEFT,
            IntVec2(1, 0) to MetaTile.LEAVES_UPPER,
            IntVec2(2, 0) to MetaTile.LEAVES_UPPER,
            IntVec2(3, 0) to MetaTile.LEAVES_UPPER,
            IntVec2(4, 0) to MetaTile.LEAVES_UPPER_RIGHT,

            IntVec2(0, 1) to MetaTile.LEAVES_LOWER_LEFT,
            IntVec2(1, 1) to MetaTile.LEAVES_LOWER,
            IntVec2(2, 1) to MetaTile.LEAVES_LOWER,
            IntVec2(3, 1) to MetaTile.LEAVES_LOWER,
            IntVec2(4, 1) to MetaTile.LEAVES_LOWER_RIGHT,
        )

}


private fun logLevel(i: Int): Set<Pair<IntVec2, MetaTile>> = setOf(
    IntVec2(-1, i) to MetaTile.LOG_LEFT,
    IntVec2(0, i) to MetaTile.LOG,
    IntVec2(1, i) to MetaTile.LOG_RIGHT,
)


class Elastic(
    prototype: ElasticPrototype,
    initialTileOffset: IntVec2,
    initialSize: IntSize,
) : Entity(
    initialTileOffset = initialTileOffset,
) {
//    private val _tileOffset = MutCell(initialTileOffset)

//    val tileOffset: Cell<IntVec2> = _tileOffset

    val size = Cell.constant(initialSize)

    val metaTileCluster = MetaTileCluster(
        tileOffset = tileOffset,
        localMetaTilesDynamic = DynamicMap.diff(size.map { prototype.buildMetaTiles(it) })
    )

    override fun isSelectableAt(worldPoint: IntVec2): Boolean {
        val globalTileCoord = tileAtPoint(worldPoint)
        return metaTileCluster.getMetaTileAt(globalTileCoord).sample() != null
    }

    override fun toString(): String = "MetaTileCluster(tileOffset=${tileOffset.sample()})"
}
