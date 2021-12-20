package icesword.editor

import icesword.frp.Cell
import icesword.frp.DynamicMap
import icesword.frp.DynamicSet
import icesword.frp.distinctMap
import icesword.frp.project
import icesword.geometry.IntVec2

interface KnotMetaTileBuilder {
    fun buildMetaTile(
        tileCoord: IntVec2,
        globalKnots: Map<IntVec2, KnotPrototype>,
    ): MetaTile?
}

class KnotMeshLayer(
    knotMeshes: DynamicSet<KnotMesh>,
    knotMetaTileBuilder: KnotMetaTileBuilder,
) {
    private val globalKnots: DynamicMap<IntVec2, KnotPrototype> = DynamicMap.unionMerge(
        through = IntVec2.mapFactory(),
        maps = knotMeshes.distinctMap(
            tag = "KnotMeshLayer/globalKnots/_knotMeshes.map",
        ) { it.globalKnots },
        merge = { knots: Set<KnotPrototype> -> knots.first() },
        tag = "KnotMeshLayer.globalKnots",
    )

    private val globalTiles: DynamicMap<IntVec2, MetaTile> = globalKnots.project(
        projectKey = { knotCoord: IntVec2 -> tilesAroundKnotForTileBuilding(knotCoord) },
        buildValue = { tileCoord: IntVec2, globalKnots: Map<IntVec2, KnotPrototype> ->
            knotMetaTileBuilder.buildMetaTile(
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
