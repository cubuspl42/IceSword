package icesword.geometry;

import frpjs.Hash
import icesword.TILE_SIZE
import icesword.collections.HybridMapFactory
import icesword.collections.MapFactory
import icesword.utils.divCeil
import icesword.utils.divFloor
import kotlin.math.roundToInt
import kotlin.math.sqrt

class IntVec2Hash : Hash<IntVec2> {
    override fun hash(value: IntVec2): Int = value.hashCode()

    override fun isEqual(l: IntVec2, r: IntVec2): Boolean =
        l.x == r.x && l.y == r.y
}

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

    fun scale(s: Double): IntVec2 = IntVec2((x * s).roundToInt(), (y * s).roundToInt())

    fun divRound(s: Int): IntVec2 = IntVec2(
        (x.toDouble() / s).roundToInt(),
        (y.toDouble() / s).roundToInt(),
    )

    fun divFloor(s: Int): IntVec2 = IntVec2(
        x.divFloor(s),
        y.divFloor(s),
    )

    fun divCeil(s: Int): IntVec2 = IntVec2(
        x.divCeil(s),
        y.divCeil(s),
    )

    operator fun unaryMinus(): IntVec2 = IntVec2(-x, -y)

    fun map(f: (Int) -> Int): IntVec2 = IntVec2(f(x), f(y))

    fun negY(): IntVec2 = copy(x = x, y = -y)

    companion object {
        val ZERO = IntVec2(0, 0)

        val HASH = IntVec2Hash()

        fun <V> mapFactory(): MapFactory<IntVec2, V> =
            HybridMapFactory(HASH)
    }
}
