package icesword.editor

import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2

class MetaTileCluster(
    private val tileOffset: Cell<IntVec2>,
    val localMetaTiles: DynamicMap<IntVec2, MetaTile>,
) {
//    private val globalTileCoordsDiff: DynamicSet<IntVec2> =
//        DynamicSet.diff(tileOffset.map { tileOffset ->
//            localMetaTiles.keys.map { tileOffset + it }.toSet()
//        })

//    private val globalTileCoordsFuseMap: DynamicSet<IntVec2> =
//        localMetaTilesDynamic.getKeys().fuseMap { localTileCoord ->
//            tileOffset.map { it + localTileCoord }
//        }

    val globalTileCoords: DynamicSet<IntVec2> =
        localMetaTiles.getKeys().adjust(
            hash = IntVec2.HASH,
            adjustment = tileOffset,
        ) { localKnotCoord: IntVec2, tileOffset: IntVec2 ->
            tileOffset + localKnotCoord
        }.also {
            it.changes.subscribe { change ->
//                println("MetaTileLayer.globalTileCoords change: $change")
            }
        }

    fun getMetaTileAt(globalTileCoord: IntVec2): Cell<MetaTile?> =
        tileOffset.switchMap { localMetaTiles.get(globalTileCoord - it) }
//            .also {
//                it.subscribe { metaTile ->
//                    println("getMetaTileAt($globalTileCoord) -> $metaTile")
//                }
//            }

    override fun toString(): String = "MetaTileCluster(tileOffset=${tileOffset.sample()})"
}

class MetaTileLayer(
    knotMeshLayer: KnotMeshLayer,
    initialElastics: Set<Elastic>,
) {
    private val _elastics = MutableDynamicSet.of(
        initialElastics,
    )

    val elastics: DynamicSet<Elastic>
        get() = _elastics

    fun insertElastic(elastic: Elastic) {
        _elastics.add(elastic)
    }


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
//                    println("MetaTileLayer.globalTileCoords change: $change")
                }
            }

//            .also { dynSet ->
//                dynSet.content.reactTill(Till.never) {
//                    println("globalTileCoord (PlaneTiles) content: $it")
//                }
//            }

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
//            .also { dynMap ->
//                dynMap.changes.subscribe { change ->
//                    println("[$globalTileCoord] associateWith change: $change")
//                }
//            }
            .fuseValues(tag = "buildTileAt($globalTileCoord) / .fuseValues")
//            .also { dynMap ->
//                dynMap.changes.subscribe { change ->
//                    println("[$globalTileCoord] fuseValues change: $change")
//                }
//            }
//            .filterValuesNotNull(tag = "buildTileAt($globalTileCoord) / .filterValuesNotNull")
//            .also { dynMap ->
//                dynMap.changes.subscribe { change ->
//                    println("[$globalTileCoord] filterValuesNotNull change: $change")
//                }
//            }

            .valuesSet

        return metaTiles.content.map { metaTilesContent ->
            val tileId = buildTile(metaTilesContent)
            tileId ?: metaTilesContent.firstNotNullOfOrNull { it?.tileId } ?: -1
        }
    }

    private fun buildTile(metaTiles: Set<MetaTile?>): Int? {
        fun containsAll(vararg ms: MetaTile): Boolean =
            ms.all { metaTiles.contains(it) }

        return when {
            containsAll(MetaTile.Log, MetaTile.LeavesUpper) -> 647
            containsAll(MetaTile.Log, MetaTile.LeavesLower) -> 653
            containsAll(MetaTile.LogLeft, MetaTile.LeavesLower) -> 652
            containsAll(MetaTile.LogRight, MetaTile.LeavesLower) -> 654

            containsAll(MetaTile.Log, MetaTile.LeavesUpperRight) -> 663
            containsAll(MetaTile.Log, MetaTile.LeavesLowerRight) -> 665

            containsAll(MetaTile.Log, MetaTile.LeavesUpperLeft) -> 659
            containsAll(MetaTile.Log, MetaTile.LeavesLowerLeft) -> 661

            containsAll(MetaTile.Log, MetaTile.GrassUpper) -> 666

            // Tile 660 is like 644, but with the "Climb" attribute
            containsAll(MetaTile.LadderTop, MetaTile.LeavesUpper) -> 660
            containsAll(MetaTile.Ladder, MetaTile.LeavesLower) -> 667

            else -> null
        }
    }
}
