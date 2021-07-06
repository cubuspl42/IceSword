package icesword

import icesword.geometry.IntVec2

class World {
    val tiles: Map<IntVec2, Int> = mapOf(
        IntVec2(0, 0) to 28,
        IntVec2(1, 0) to 35,
        IntVec2(0, 1) to 53,
        IntVec2(1, 1) to 38,
    )
}
