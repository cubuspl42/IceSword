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
//    private val globalTileCoordsDiff: DynamicSet<IntVec2> =
//        DynamicSet.diff(tileOffset.map { tileOffset ->
//            localMetaTiles.keys.map { tileOffset + it }.toSet()
//        })

//    private val globalTileCoordsFuseMap: DynamicSet<IntVec2> =
//        localMetaTilesDynamic.getKeys().fuseMap { localTileCoord ->
//            tileOffset.map { it + localTileCoord }
//        }

    val globalTileCoords: DynamicSet<IntVec2> =
        localMetaTilesDynamic.getKeys().adjust(
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
        tileOffset.switchMap { localMetaTilesDynamic.get(globalTileCoord - it) }
//            .also {
//                it.subscribe { metaTile ->
//                    println("getMetaTileAt($globalTileCoord) -> $metaTile")
//                }
//            }

    override fun toString(): String = "MetaTileCluster(tileOffset=${tileOffset.sample()})"
}

class MetaTileLayer {
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
                    size = IntSize(5, 2),
                ),
            ),
            Elastic(
                prototype = TreeCrownPrototype,
                initialBounds = IntRect(
                    position = IntVec2(83, 84),
                    size = IntSize(5, 2),
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
        val metaTiles = elastics
            .associateWith(tag = "buildTileAt($globalTileCoord) / .associateWith") {
                it.metaTileCluster.getMetaTileAt(globalTileCoord)
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
