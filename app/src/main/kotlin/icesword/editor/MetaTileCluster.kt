package icesword.editor

import icesword.editor.Retail.Retail3
import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2

class MetaTileCluster(
    private val tileOffset: Cell<IntVec2>,
    val localMetaTiles: DynamicMap<IntVec2, MetaTile>,
) {
    val globalTileCoords: DynamicSet<IntVec2> =
        localMetaTiles.getKeys().adjust(
            hash = IntVec2.HASH,
            adjustment = tileOffset,
        ) { localKnotCoord: IntVec2, tileOffset: IntVec2 ->
            tileOffset + localKnotCoord
        }.also {
            it.changes.subscribe { change ->
            }
        }

    fun getMetaTileAt(globalTileCoord: IntVec2): Cell<MetaTile?> =
        tileOffset.switchMap { localMetaTiles.get(globalTileCoord - it) }

    override fun toString(): String = "MetaTileCluster(tileOffset=${tileOffset.sample()})"
}

class MetaTileLayer(
    knotMeshLayer: KnotMeshLayer,
    elastics: DynamicSet<Elastic>,
) {
    private val metaTileClusters = elastics.map(
        tag = "MetaTileLayer/elastics.map"
    ) { it.metaTileCluster }
        .unionWith(DynamicSet.of(setOf(knotMeshLayer.metaTileCluster)))

    private val globalTileCoords: DynamicSet<IntVec2> =
        metaTileClusters.unionMapDynamic(
            tag = "metaTileClusters.unionMapDynamic",
        ) { it.globalTileCoords }
            .also {
                it.changes.subscribe { change ->
                }
            }

    val tiles: DynamicMap<IntVec2, Int> =
        globalTileCoords.associateWithDynamic(
            tag = "globalTileCoords.associateWithDynamic",
            this::buildTileAt,
        ).also {
            it.changes.subscribe { } // FIXME
        }

    private fun buildTileAt(
        globalTileCoord: IntVec2,
    ): Cell<Int> {
        val metaTiles = metaTileClusters
            .associateWith(tag = "buildTileAt($globalTileCoord) / .associateWith") {
                it.getMetaTileAt(globalTileCoord)
            }
            .fuseValues(tag = "buildTileAt($globalTileCoord) / .fuseValues")
            .valuesSet

        return metaTiles.content.map { metaTilesContent ->
            val tileId = buildTile(metaTilesContent)
            tileId ?: metaTilesContent.firstNotNullOfOrNull { it?.tileId } ?: -1
        }
    }

}

private fun buildTile(metaTiles: Set<MetaTile?>): Int? {
    fun containsAll(vararg ms: MetaTile): Boolean =
        ms.all { metaTiles.contains(it) }

    return when {
        // Tree crown
        containsAll(MetaTile.Log, MetaTile.LeavesUpper) -> 647
        containsAll(MetaTile.Log, MetaTile.LeavesLower) -> 653
        containsAll(MetaTile.LogLeft, MetaTile.LeavesLower) -> 652
        containsAll(MetaTile.LogRight, MetaTile.LeavesLower) -> 654

        // Side tree crown tip
        containsAll(MetaTile.Log, MetaTile.LeavesUpperRight) -> 663
        containsAll(MetaTile.Log, MetaTile.LeavesLowerRight) -> 665

        // Tree branches
        containsAll(MetaTile.Log, MetaTile.LeavesUpperLeft) -> 659
        containsAll(MetaTile.Log, MetaTile.LeavesLowerLeft) -> 661

        // Tree root
        containsAll(MetaTile.Log, MetaTile.GrassUpper) -> 666

        // Ladder connection to tree crown
        // Tile 660 is like 644, but with the "Climb" attribute
        containsAll(Retail3.ladderTop, MetaTile.LeavesUpper) -> 660
        containsAll(Retail3.ladder, MetaTile.LeavesLower) -> 667

        // Spikes
        containsAll(MetaTile.SpikeTop, MetaTile.RockRightSide) -> 712
        containsAll(MetaTile.SpikeBottom, MetaTile.RockLowerLeftCorner) -> 713

        containsAll(MetaTile.SpikeBottom, MetaTile.RockTop) -> 704

        containsAll(MetaTile.SpikeTop, MetaTile.RockLeftSideOuter) -> 701
        containsAll(MetaTile.SpikeTop, MetaTile.RockLeftSideInner) -> 702
        containsAll(MetaTile.SpikeBottom, MetaTile.RockLowerRightCornerOuter) -> 704
        containsAll(MetaTile.SpikeBottom, MetaTile.RockLowerRightCornerInner) -> 709 // 696

        else -> null
    }
}