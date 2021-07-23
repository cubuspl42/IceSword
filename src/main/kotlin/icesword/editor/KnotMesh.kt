package icesword.editor

import icesword.frp.DynamicMap
import icesword.geometry.IntVec2

fun tilesAroundKnot(knotCoord: IntVec2): Set<IntVec2> {
    @Suppress("UnnecessaryVariable")
    val k = knotCoord

    return setOf(
        k, IntVec2(x = k.x + 1, k.y),
        IntVec2(x = k.x, k.y + 1), IntVec2(x = k.x + 1, k.y + 1),
    )
}

fun knotsAroundTile(tileCoord: IntVec2, distance: Int): Set<IntVec2> {
    @Suppress("UnnecessaryVariable")
    val d = distance

    val range = (-(d + 1)..d)

    return range.flatMap { y ->
        range.map { x ->
            tileCoord + IntVec2(x, y)
        }
    }.toSet()
}

class KnotMesh(tileOffset: IntVec2) {
    private val localKnots = setOf(
        IntVec2(0, 0),
        IntVec2(1, 0),
        IntVec2(0, 1),
        IntVec2(1, 1),
    )

    private val localTileCoords = localKnots.flatMap(::tilesAroundKnot).toSet()

    private val localTiles = localTileCoords.associateWith { tileCoord ->
        val nearbyKnots = knotsAroundTile(tileCoord, distance = 1)
            .filter(localKnots::contains)
            .toSet()

        val relativeKnots = nearbyKnots.map { it - tileCoord }.toSet()

        buildTile(relativeKnots)
    }

    val tiles: DynamicMap<IntVec2, Int> = DynamicMap.of(
        localTiles.mapKeys { (localTileOffset, _) ->
            tileOffset + localTileOffset
        },
    )
}

private fun buildTile(relativeKnots: Set<IntVec2>): Int {
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
    ) 623
    else 630
}
