package icesword.geometry;

import kotlin.math.roundToInt
import kotlin.math.sqrt

data class IntVec2(
    val x: Int,
    val y: Int,
) {
    val lengthSquared: Double
        get() = x.toDouble() * x + y.toDouble() * y

    val length: Double
        get() = sqrt(lengthSquared)

    operator fun plus(that: IntVec2): IntVec2 = IntVec2(x + that.x, y + that.y)

    operator fun minus(that: IntVec2): IntVec2 = IntVec2(x - that.x, y - that.y)

    operator fun times(s: Int): IntVec2 = IntVec2(x * s, y * s)

    operator fun div(s: Int): IntVec2 = IntVec2(x / s, y / s)

    fun divRound(s: Int): IntVec2 = IntVec2(
        (x.toDouble() / s).roundToInt(),
        (y.toDouble() / s).roundToInt(),
    )

    operator fun unaryMinus(): IntVec2 = IntVec2(-x, -y)

    fun map(f: (Int) -> Int): IntVec2 = IntVec2(f(x), f(y))

    companion object {
        val ZERO = IntVec2(0, 0)
    }
}
