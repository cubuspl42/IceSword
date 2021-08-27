package icesword.editor

import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.tileAtPoint

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
    private val tileOffset: Cell<IntVec2>,
    val localMetaTilesDynamic: DynamicMap<IntVec2, MetaTile>,
) {
//    private val _tileOffset = MutCell(initialTileOffset)

//    val tileOffset: Cell<IntVec2> = _tileOffset


//    private val localMetaTilesDynamic = DynamicMap.of(localMetaTiles)

//    private val globalTileCoordsDiff: DynamicSet<IntVec2> =
//        DynamicSet.diff(tileOffset.map { tileOffset ->
//            localMetaTiles.keys.map { tileOffset + it }.toSet()
//        })

    private val globalTileCoordsFuseMap: DynamicSet<IntVec2> =
        localMetaTilesDynamic.keys.fuseMap { localTileCoord ->
            tileOffset.map { it + localTileCoord }
        }

    val globalTileCoords: DynamicSet<IntVec2>
        get() = globalTileCoordsFuseMap

    fun getMetaTileAt(globalTileCoord: IntVec2): Cell<MetaTile?> =
        tileOffset.switchMap { localMetaTilesDynamic.get(globalTileCoord - it) }
//            .also {
//                it.subscribe { metaTile ->
//                    println("getMetaTileAt($globalTileCoord) -> $metaTile")
//                }
//            }


//    fun isSelectableAt(worldPoint: IntVec2): Boolean {
//        val globalTileCoord = tileAtPoint(worldPoint)
//        return getMetaTileAt(globalTileCoord).sample() != null
//    }

    override fun toString(): String = "MetaTileCluster(tileOffset=${tileOffset.sample()})"
}

//private fun logLevel(i: Int): Set<Pair<IntVec2, MetaTile>> = setOf(
//    IntVec2(-1, i) to MetaTile.LOG_LEFT,
//    IntVec2(0, i) to MetaTile.LOG,
//    IntVec2(1, i) to MetaTile.LOG_RIGHT,
//)

class PlaneTiles {
//    private val treeCrown = mapOf(
//        IntVec2(0, 0) to MetaTile.LEAVES_UPPER_LEFT,
//        IntVec2(1, 0) to MetaTile.LEAVES_UPPER,
//        IntVec2(2, 0) to MetaTile.LEAVES_UPPER,
//        IntVec2(3, 0) to MetaTile.LEAVES_UPPER,
//        IntVec2(4, 0) to MetaTile.LEAVES_UPPER_RIGHT,
//
//        IntVec2(0, 1) to MetaTile.LEAVES_LOWER_LEFT,
//        IntVec2(1, 1) to MetaTile.LEAVES_LOWER,
//        IntVec2(2, 1) to MetaTile.LEAVES_LOWER,
//        IntVec2(3, 1) to MetaTile.LEAVES_LOWER,
//        IntVec2(4, 1) to MetaTile.LEAVES_LOWER_RIGHT,
//    )

//    private val _elastics = MutableDynamicSet.of(
//        setOf(
//            MetaTileCluster(
//                initialTileOffset = IntVec2(83, 82),
//                localMetaTiles = (0..16).flatMap(::logLevel).toMap()
//            ),
//            MetaTileCluster(
//                initialTileOffset = IntVec2(81, 92),
//                localMetaTiles = treeCrown,
//            ),
//            MetaTileCluster(
//                initialTileOffset = IntVec2(79, 87),
//                localMetaTiles = treeCrown,
//            ),
//            MetaTileCluster(
//                initialTileOffset = IntVec2(83, 84),
//                localMetaTiles = treeCrown,
//            ),
//        )
//    )

    private val _elastics = MutableDynamicSet.of(
        setOf(
            Elastic(
                prototype = LogPrototype,
                initialBounds = IntRect(
                    position = IntVec2(83, 82),
                    size = IntSize(1, 16),
                ),
            ),
            Elastic(
                prototype = TreeCrownPrototype,

                initialBounds = IntRect(
                    position = IntVec2(81, 92),
                    size = IntSize(5, 2),
                ),
            ),
            Elastic(
                prototype = TreeCrownPrototype,
                initialBounds = IntRect(
                    position = IntVec2(79, 87),
                    size = IntSize.ZERO,
                ),
            ),
            Elastic(
                prototype = TreeCrownPrototype,
                initialBounds = IntRect(
                    position = IntVec2(83, 84),
                    size = IntSize.ZERO,
                ),
            ),
        )
    )

    val elastics: DynamicSet<Elastic>
        get() = _elastics

    fun insertElastic(elastic: Elastic) {
        _elastics.add(elastic)
    }

    private val globalTileCoords: DynamicSet<IntVec2> =
        elastics.unionMapDynamic { it.metaTileCluster.globalTileCoords }
//            .also {
//                it.changes.subscribe { change ->
//                    println("globalTileCoords change: $change")
//                }
//            }

//            .also { dynSet ->
//                dynSet.content.reactTill(Till.never) {
//                    println("globalTileCoord (PlaneTiles) content: $it")
//                }
//            }

    val tiles: DynamicMap<IntVec2, Int> =
        globalTileCoords.associateWithDynamic(
            tag = "globalTileCoords.associateWithDynamic",
            this::buildTileAt,
        )

    private fun buildTileAt(
        globalTileCoord: IntVec2,
    ): Cell<Int> {
        val metaTiles = elastics
            .associateWith(tag = "metaTileClusters.associateWith") {
                it.metaTileCluster.getMetaTileAt(globalTileCoord)
            }
//            .also { dynMap ->
//                dynMap.changes.subscribe { change ->
//                    println("[$globalTileCoord] associateWith change: $change")
//                }
//            }
            .fuseValues()
//            .also { dynMap ->
//                dynMap.changes.subscribe { change ->
//                    println("[$globalTileCoord] fuseValues change: $change")
//                }
//            }
            .filterValuesNotNull()
//            .also { dynMap ->
//                dynMap.changes.subscribe { change ->
//                    println("[$globalTileCoord] filterValuesNotNull change: $change")
//                }
//            }

            .valuesSet

        return metaTiles.content.map { metaTilesContent ->
            val tileId = buildTile(metaTilesContent)
            tileId ?: metaTilesContent.firstNotNullOfOrNull { it.tileId } ?: -1
        }
    }

    private fun buildTile(metaTiles: Set<MetaTile>): Int? {
        fun containsAll(vararg ms: MetaTile): Boolean =
            ms.all { metaTiles.contains(it) }

        return when {
            containsAll(MetaTile.LOG, MetaTile.LEAVES_UPPER) -> 647
            containsAll(MetaTile.LOG, MetaTile.LEAVES_LOWER) -> 653
            containsAll(MetaTile.LOG_LEFT, MetaTile.LEAVES_LOWER) -> 652
            containsAll(MetaTile.LOG_RIGHT, MetaTile.LEAVES_LOWER) -> 654

            containsAll(MetaTile.LOG, MetaTile.LEAVES_UPPER_RIGHT) -> 663
            containsAll(MetaTile.LOG, MetaTile.LEAVES_LOWER_RIGHT) -> 665

            containsAll(MetaTile.LOG, MetaTile.LEAVES_UPPER_LEFT) -> 659
            containsAll(MetaTile.LOG, MetaTile.LEAVES_LOWER_LEFT) -> 661

            else -> null
        }
    }
}
