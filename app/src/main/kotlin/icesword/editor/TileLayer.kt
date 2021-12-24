package icesword.editor

import icesword.geometry.IntVec2

class TileLayer(
    val tiles: Map<IntVec2, Int>,
) {
    override fun toString(): String = "TileLayer()"
}
