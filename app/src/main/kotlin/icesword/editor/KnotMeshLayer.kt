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
                initialSize = 115,
            ),
            KnotMesh(
                initialTileOffset = tileAtPoint(startPoint) + IntVec2(8, 0),
                knotPrototype = OvergroundRockPrototype,
            ),
        ),
    )

    init {
//        val knotMeshesContent = _knotMeshes.volatileContentView
//
//        val foo = knotMeshesContent.toCollection(LinkedHashSet<KnotMesh>(mapCapacity(size))
//
//        println("knotMeshesContent is Collection: ${knotMeshesContent is Collection}")
//
//        val knotMeshContentSet = knotMeshesContent.toSet()
//
//        println("knotMeshContentSet.size: ${knotMeshContentSet.size}")
//        println("knotMeshContentSet: $knotMeshContentSet")
//
//        for (knotMesh in knotMeshesContent) {
//            println("knotMesh: $knotMesh")
//        }
    }

    val knotMeshes: DynamicSet<KnotMesh>
        get() = _knotMeshes

    private val globalKnots: DynamicMap<IntVec2, KnotPrototype> = DynamicMap.unionMerge(
        _knotMeshes.map { it.globalKnots },
        merge = { knots: Set<KnotPrototype> -> knots.first() },
        tag = "KnotMeshLayer.globalKnots",
    )
//        .also {
//            it.changes.subscribe { change ->
//                println("globalKnots change: $change / ${it.volatileContentView}")
//            }
//
//            it.content.subscribe { change ->
//                println("globalKnots.content change: $change")
//            }
//        }

//    private val globalKnotCoords = globalKnots.getKeys(tag = "globalKnotCoords")
//        .also {
//            it.changes.subscribe { change ->
//                println("globalKnotCoords change: $change")
//            }
//        }

//    private val globalTileCoords: DynamicSet<IntVec2> = globalKnotCoords.unionMapDynamic { globalKnotCoord ->
//        // FIXME: Impure (time-dependent) transform!
//        DynamicSet.of(tilesAroundKnot(globalKnotCoord))
//    }
//        .also {
//            it.changes.subscribe { change ->
//                println("globalTileCoords change: $change")
//            }
//        }

//    val globalTiles = globalTileCoords.associateWithDynamic(tag = "globalTiles") { tileCoord ->
//        buildTileAt(tileCoord)
//    }.also {
//        // FIXME
//        it.changes.subscribe { }
//    }
//        .also {
//            it.changes.subscribe { change ->
//                println("globalTiles change: $change")
//            }
//        }

    val globalTiles = globalKnots.project(
        projectKey = { knotCoord -> tilesAroundKnotLst(knotCoord) },
        buildValue = { tileCoord, globalKnots ->
            buildTile(
                tileCoord = tileCoord,
                globalKnots = globalKnots,
            )
        },
    ).also {
        it.changes.subscribe { }
    }

//    private fun buildTileAt(
//        tileCoord: IntVec2,
//    ): Cell<Int> {
//        val nearbyKnotCoords = DynamicSet.of(
//            knotsAroundTile(tileCoord, distance = 1).toSet(),
//        )
//
//        val nearbyKnots =
//            nearbyKnotCoords.associateWithDynamic("buildTileAt($tileCoord)/associateWithDynamic") { knotCoord ->
////            println("nearbyKnot, knotCoord: $knotCoord")
//                globalKnots.get(knotCoord)
//            }.filterValuesNotNull(tag = "buildTileAt($tileCoord)/filterValuesNotNull")
//
//        val tile = nearbyKnots.content.map { nearbyKnotsContent ->
////            println("buildTileAt @ $tileCoord; nearbyKnotsContent = $nearbyKnotsContent")
//
//            val relativeKnots = nearbyKnotsContent.mapKeys { (tc, _) -> tc - tileCoord }
//
//            buildTile(
//                relativeKnots = relativeKnots,
//            )
//        }
//
//        return tile
//
//    }
}
