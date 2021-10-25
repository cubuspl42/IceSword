package icesword

import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2

const val TILE_SIZE = 64

fun tileTopLeftCorner(tileCoord: IntVec2): IntVec2 =
    tileCoord * TILE_SIZE

fun tileRect(tileCoord: IntVec2): IntRect =
    IntRect(
        tileTopLeftCorner(tileCoord),
        IntSize(TILE_SIZE, TILE_SIZE),
    )

fun tileAtPoint(point: IntVec2): IntVec2 =
    point.divFloor(TILE_SIZE)

fun tilesOverlappingArea(worldArea: IntRect): List<IntVec2> {
    val tileCoordA = worldArea.topLeft.divFloor(TILE_SIZE)
    val tileCoordB = worldArea.bottomRight.divCeil(TILE_SIZE)

    return IntRect.fromDiagonal(pointA = tileCoordA, pointC = tileCoordB)
        .points().toList()
}
