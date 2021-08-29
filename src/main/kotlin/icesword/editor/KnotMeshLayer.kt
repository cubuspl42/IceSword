package icesword.editor

import icesword.frp.*
import icesword.geometry.IntVec2
import icesword.tileAtPoint

class KnotMeshLayer(
    startPoint: IntVec2,
) {
    private val _knotMeshes = MutableDynamicSet(
        setOf(
            KnotMesh(
                initialTileOffset = tileAtPoint(startPoint),
                knotPrototype = UndergroundRockPrototype,
            ),
            KnotMesh(
                initialTileOffset = tileAtPoint(startPoint) + IntVec2(8, 0),
                knotPrototype = OvergroundRockPrototype,
            ),
        ),
    )

    val knotMeshes: DynamicSet<KnotMesh>
        get() = _knotMeshes

    private val globalKnots: DynamicMap<IntVec2, KnotPrototype> = DynamicMap.unionMerge(
        _knotMeshes.map { it.globalKnots },
//        merge = { knots: Set<KnotPrototype> -> knots.first() }
    )
//        .also {
//            it.changes.subscribe { }
//            println("Global knots: ${it.volatileContentView}")
//        }

    private val globalKnotCoords = globalKnots.keys

    private val globalTileCoords: DynamicSet<IntVec2> = globalKnotCoords.unionMapDynamic { globalKnotCoord ->
        // FIXME: Impure (time-dependent) transform!
        DynamicSet.of(tilesAroundKnot(globalKnotCoord))
    }

    val globalTiles = globalTileCoords.associateWithDynamic(tag = "globalTiles") { tileCoord ->
        buildTileAt(tileCoord)
    }

    private fun buildTileAt(
        tileCoord: IntVec2,
    ): Cell<Int> {
        val nearbyKnotCoords = DynamicSet.of(
            knotsAroundTile(tileCoord, distance = 1).toSet(),
        )

        val relativeKnots = nearbyKnotCoords.associateWithDynamic("relativeKnots") { knotCoord ->
            globalKnots.get(knotCoord)
        }.filterValuesNotNull()

        val tile = relativeKnots.content.map { relativeKnotsContent ->
//            println("buildTileAt @ $tileCoord; relativeKnotsContent = $relativeKnotsContent")

            buildTile(
                relativeKnots = relativeKnotsContent.keys.map { it - tileCoord }.toList(),
            )
        }

        return tile

    }
}
