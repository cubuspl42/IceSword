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
    point.map {
        if (it >= 0) it / TILE_SIZE
        else (it + 1) / TILE_SIZE
    }
