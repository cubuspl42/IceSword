package icesword.geometry;

import kotlin.math.sqrt

data class IntVec2(
    val x: Int,
    val y: Int,
) {
    val length: Double
        get() = sqrt(x.toDouble() * x + y.toDouble() * y)

    operator fun plus(that: IntVec2): IntVec2 = IntVec2(x + that.x, y + that.y)

    operator fun minus(that: IntVec2): IntVec2 = IntVec2(x - that.x, y - that.y)

    companion object {
        val ZERO = IntVec2(0, 0)
    }
}
