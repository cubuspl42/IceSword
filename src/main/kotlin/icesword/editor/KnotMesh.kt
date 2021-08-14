package icesword.editor

import icesword.frp.*
import icesword.geometry.IntVec2
import icesword.tileAtPoint
import icesword.tileTopLeftCorner

fun tilesAroundKnot(knotCoord: IntVec2): Set<IntVec2> {
    @Suppress("UnnecessaryVariable")
    val k = knotCoord

    return setOf(
        k, IntVec2(x = k.x + 1, k.y),
        IntVec2(x = k.x, k.y + 1), IntVec2(x = k.x + 1, k.y + 1),
    )
}

//fun tilesAroundKnot(knotCoord: IntVec2, distance: Int = 0): List<IntVec2> {
//    @Suppress("UnnecessaryVariable")
//    val d = distance
//
//    val range = (-d..(1 + d))
//
//    return range.flatMap { y ->
//        range.map { x ->
//            knotCoord + IntVec2(x, y)
//        }
//    }
//}


fun closestKnot(point: IntVec2): IntVec2 =
    tileAtPoint(point - IntVec2(32, 32))

fun knotCenter(knotCoord: IntVec2): IntVec2 =
    tileTopLeftCorner(
        IntVec2(
            x = knotCoord.x + 1,
            y = knotCoord.y + 1,
        )
    )

fun knotsAroundTile(tileCoord: IntVec2, distance: Int): List<IntVec2> {
    @Suppress("UnnecessaryVariable")
    val d = distance

    val range = (-(d + 1)..d)

    return range.flatMap { y ->
        range.map { x ->
            tileCoord + IntVec2(x, y)
        }
    }
}

class KnotMesh(
    initialTileOffset: IntVec2,
    till: Till,
) : Entity(
    initialTileOffset = initialTileOffset,
) {
//    val tileOffset = Cell.constant(initialTileOffset)

    private val _localKnots = MutableDynamicSet.of(
        setOf(
            IntVec2(0, 0),
            IntVec2(1, 0),
            IntVec2(0, 1),
            IntVec2(1, 1),
        )
    )

    val localKnots: DynamicSet<IntVec2> = _localKnots

    fun putKnot(globalKnotCoord: IntVec2) {
        val localKnotCoord = globalKnotCoord - tileOffset.sample()
        _localKnots.add(localKnotCoord)

        val tileCoords = tilesAroundKnot(localKnotCoord)

        val localKnots = this._localKnots.sample()

        tileCoords.forEach { tileCoord ->
            localTiles.put(tileCoord, buildTileAt(localKnots, tileCoord))
        }
    }

//    private val localTileCoords = localKnots.unionMap(::tilesAroundKnot)

//    private val localTiles = localTileCoords.associateWith { tileCoord ->
//        val nearbyKnots = knotsAroundTile(tileCoord, distance = 1)
//            .filter(localKnots::contains)
//            .toSet()
//
//        val relativeKnots = nearbyKnots.map { it - tileCoord }.toSet()
//
//        buildTile(relativeKnots)
//    }


//    private fun <A> withCounter(f: (inc: () -> Unit) -> A): A {
//
//        var counter = 0
//
//        fun inc() {
//            ++counter
//        }
//
//        return f(::inc)
//
//        val a = f(::inc)
//
//        println("Counter: $counter")
//
//        return a
//    }

    private val localTiles = run {
        val localKnots = _localKnots.sample()
        val localTileCoords = localKnots.flatMap(::tilesAroundKnot)

        MutableDynamicMap.of(
            localTileCoords.associateWith { tileCoord ->
                buildTileAt(localKnots, tileCoord)
            }
        )
    }

    private fun buildTileAt(
        localKnots: Set<IntVec2>,
        tileCoord: IntVec2,
    ): Int {
        val nearbyKnots = knotsAroundTile(tileCoord, distance = 1)
            .filter(localKnots::contains)

        val relativeKnots = nearbyKnots.map { it - tileCoord }

        return buildTile(relativeKnots)
    }

//    private val localTiles = DynamicMap.diff(
//        Cell.map2(
//            localKnots.trackContent(till),
//            localTileCoords.trackContent(till),
//        ) { localKnots, localTileCoords ->
//            withCounter { inc ->
//                localTileCoords.associateWith { tileCoord ->
//                    val nearbyKnots = knotsAroundTile(tileCoord, distance = 1)
//                        .filter(localKnots::contains)
//
//                    val relativeKnots = nearbyKnots.map { it - tileCoord }
//
//                    inc()
//                    buildTile(relativeKnots)
//                }
//            }
//        },
//    )

//    val tiles = localTiles.mapKeys(tag = "localTiles.mapKeys") { (localTileOffset, _) ->
//        initialTileOffset + localTileOffset
//    }

    val tilesDynamic = localTiles.mapKeysDynamic { (localTileOffset, _) ->
        tileOffset.map { it + localTileOffset }
    }

    override fun isSelectableAt(worldPoint: IntVec2): Boolean {
        val globalTileCoord = tileAtPoint(worldPoint)
        return tilesDynamic.getNow(globalTileCoord) != null
    }
}


private fun buildTile(relativeKnots: List<IntVec2>): Int {
//    return 620

    fun intVec2(y: Int, x: Int) = IntVec2(x, y)

    return if (
        relativeKnots.contains(intVec2(0, 0)) &&
        !relativeKnots.contains(intVec2(-1, -1)) &&
        !relativeKnots.contains(intVec2(-1, 0)) &&
        !relativeKnots.contains(intVec2(0, -1))
    ) 620 else if (
        relativeKnots.contains(intVec2(0, 0)) &&
        relativeKnots.contains(intVec2(0, -1)) &&
        !relativeKnots.contains(intVec2(-1, 0)) &&
        !relativeKnots.contains(intVec2(-1, -1)) &&
        !relativeKnots.contains(intVec2(0, -2))
    ) 621 else if (
        relativeKnots.contains(intVec2(0, 0)) &&
        relativeKnots.contains(intVec2(0, -1)) &&
        !relativeKnots.contains(intVec2(-1, 0)) &&
        !relativeKnots.contains(intVec2(-1, -1))
    ) 622 else if (
        relativeKnots.contains(intVec2(0, 0)) &&
        relativeKnots.contains(intVec2(-1, 0)) &&
        !relativeKnots.contains(intVec2(0, -1)) &&
        !relativeKnots.contains(intVec2(-1, -1))
    ) 624 else if (
        relativeKnots.contains(intVec2(-1, -1)) &&
        relativeKnots.contains(intVec2(-1, 0)) &&
        relativeKnots.contains(intVec2(0, -1)) &&
        relativeKnots.contains(intVec2(0, 0)) &&
        !relativeKnots.contains(intVec2(-1, -2)) &&
        !relativeKnots.contains(intVec2(0, -2))
    ) 613 else if (
        relativeKnots.contains(intVec2(-1, 0)) &&
        !relativeKnots.contains(intVec2(-1, -1)) &&
        !relativeKnots.contains(intVec2(0, -1)) &&
        !relativeKnots.contains(intVec2(0, 0))
    ) 632 else if (
        relativeKnots.contains(intVec2(-1, -1)) &&
        relativeKnots.contains(intVec2(-1, 0)) &&
        !relativeKnots.contains(intVec2(-1, -2)) &&
        !relativeKnots.contains(intVec2(0, -2)) &&
        !relativeKnots.contains(intVec2(0, -1)) &&
        !relativeKnots.contains(intVec2(0, 0))
    ) 633 else if (
        relativeKnots.contains(intVec2(-1, -1)) &&
        relativeKnots.contains(intVec2(-1, 0)) &&
        relativeKnots.contains(intVec2(-1, -2)) &&
        !relativeKnots.contains(intVec2(0, -1)) &&
        !relativeKnots.contains(intVec2(0, 0))
    ) 634 else if (
        relativeKnots.contains(intVec2(-1, -1)) &&
        !relativeKnots.contains(intVec2(0, -1)) &&
        !relativeKnots.contains(intVec2(0, 0)) &&
        !relativeKnots.contains(intVec2(-1, 0))
    ) 635 else if (
        relativeKnots.contains(intVec2(-1, -1)) &&
        relativeKnots.contains(intVec2(0, -1)) &&
        !relativeKnots.contains(intVec2(-1, 0)) &&
        !relativeKnots.contains(intVec2(0, 0))
    ) 615 else if (
        relativeKnots.contains(intVec2(0, -1)) &&
        !relativeKnots.contains(intVec2(-1, -1)) &&
        !relativeKnots.contains(intVec2(-1, 0)) &&
        !relativeKnots.contains(intVec2(0, 0))
    ) 623 else if (
        relativeKnots.contains(intVec2(0, -1)) &&
        relativeKnots.contains(intVec2(-1, 0)) &&
        relativeKnots.contains(intVec2(-1, -1)) &&
        !relativeKnots.contains(intVec2(0, 0))
    ) 617 else if (
        relativeKnots.contains(intVec2(0, 0)) &&
        relativeKnots.contains(intVec2(-1, 0)) &&
        relativeKnots.contains(intVec2(-1, -1)) &&
        !relativeKnots.contains(intVec2(0, -1))
    ) 618
    else 630
}
