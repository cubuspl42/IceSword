package icesword.editor

import icesword.frp.Cell
import icesword.frp.DynamicMap
import icesword.frp.DynamicSet
import icesword.frp.MutableDynamicSet
import icesword.frp.map
import icesword.frp.project
import icesword.geometry.IntVec2

class KnotMeshLayer(
    knotMeshes: DynamicSet<KnotMesh>,
) {
    private val globalKnots: DynamicMap<IntVec2, KnotPrototype> = DynamicMap.unionMerge(
        through = IntVec2.mapFactory(),
        maps = knotMeshes.map(
            tag = "KnotMeshLayer/globalKnots/_knotMeshes.map",
        ) { it.globalKnots },
        merge = { knots: Set<KnotPrototype> -> knots.first() },
        tag = "KnotMeshLayer.globalKnots",
    )

    private val globalTiles: DynamicMap<IntVec2, MetaTile> = globalKnots.project(
        projectKey = { knotCoord: IntVec2 -> tilesAroundKnotForTileBuilding(knotCoord) },
        buildValue = { tileCoord: IntVec2, globalKnots: Map<IntVec2, KnotPrototype> ->
            buildTile(
                tileCoord = tileCoord,
                globalKnots = globalKnots,
            ) ?: MetaTile.None
        },
    ).also {
        it.changes.subscribe {
        }
    }

    val metaTileCluster = MetaTileCluster(
        tileOffset = Cell.constant(IntVec2.ZERO),
        localMetaTiles = globalTiles,
    )
}
