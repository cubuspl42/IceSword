package icesword.editor

import icesword.frp.DynamicMap
import icesword.geometry.IntVec2

enum class MetaTile(
    val tileId: Int?,
) {
    LOG(657),
    LOG_LEFT(null),
    LOG_RIGHT(null),

    LEAVES_UPPER(644),
    LEAVES_LOWER(651),
    LEAVES_UPPER_LEFT(645),
    LEAVES_LOWER_LEFT(650),
    LEAVES_UPPER_RIGHT(649),
    LEAVES_LOWER_RIGHT(656),

}

class MetaTileCluster(
    val tileOffset: IntVec2,
    val localMetaTiles: Map<IntVec2, MetaTile>,
) {
    val globalMetaTiles = localMetaTiles.mapKeys { (localTileCoord, _) ->
        tileOffset + localTileCoord
    }
}

private fun logLevel(i: Int): Set<Pair<IntVec2, MetaTile>> = setOf(
    IntVec2(-1, i) to MetaTile.LOG_LEFT,
    IntVec2(0, i) to MetaTile.LOG,
    IntVec2(1, i) to MetaTile.LOG_RIGHT,
)

class PlaneTiles {
    private val treeCrown = mapOf(
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

    private val metaTileClusters = setOf(
        MetaTileCluster(
            tileOffset = IntVec2(83, 82),
            localMetaTiles = (0..16).flatMap(::logLevel).toMap()
        ),
        MetaTileCluster(
            tileOffset = IntVec2(81, 92),
            localMetaTiles = treeCrown,
        ),
        MetaTileCluster(
            tileOffset = IntVec2(79, 87),
            localMetaTiles = treeCrown,
        ),
        MetaTileCluster(
            tileOffset = IntVec2(83, 84),
            localMetaTiles = treeCrown,
        ),
    )

    private val globalTileCoords =
        metaTileClusters.flatMap { it.globalMetaTiles.keys }.toSet()

    val tiles: DynamicMap<IntVec2, Int> =
        DynamicMap.of(
            globalTileCoords.associateWith(this::buildTileAt)
        )

    private fun buildTileAt(
        globalTileCoord: IntVec2,
    ): Int {
        val metaTiles = metaTileClusters
            .mapNotNull { it.globalMetaTiles[globalTileCoord] }
            .toSet()

        val tileId = buildTile(metaTiles)

        return tileId ?: metaTiles.firstNotNullOfOrNull { it.tileId } ?: -1
    }

    private fun buildTile(metaTiles: Set<MetaTile>): Int? =
        when {
            metaTiles.containsAll(setOf(MetaTile.LOG, MetaTile.LEAVES_UPPER)) -> 647
            metaTiles.containsAll(setOf(MetaTile.LOG, MetaTile.LEAVES_LOWER)) -> 653
            metaTiles.containsAll(setOf(MetaTile.LOG_LEFT, MetaTile.LEAVES_LOWER)) -> 652
            metaTiles.containsAll(setOf(MetaTile.LOG_RIGHT, MetaTile.LEAVES_LOWER)) -> 654

            metaTiles.containsAll(setOf(MetaTile.LOG, MetaTile.LEAVES_UPPER_RIGHT)) -> 663
            metaTiles.containsAll(setOf(MetaTile.LOG, MetaTile.LEAVES_LOWER_RIGHT)) -> 665

            metaTiles.containsAll(setOf(MetaTile.LOG, MetaTile.LEAVES_UPPER_LEFT)) -> 659
            metaTiles.containsAll(setOf(MetaTile.LOG, MetaTile.LEAVES_LOWER_LEFT)) -> 661
            else -> null
        }
}
