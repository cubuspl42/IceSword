package icesword.geometry

import icesword.frp.Cell
import icesword.frp.map
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class Transform(
    val a: Double,
    val b: Double,
    val c: Double,
    val d: Double,
    val e: Double,
    val f: Double,
) {
    companion object {
        fun translate(t: IntVec2): Transform =
            Transform(
                a = 1.0,
                c = 0.0,
                e = t.x.toDouble(),
                b = 0.0,
                d = 1.0,
                f = t.y.toDouble(),
            )

        fun rotate(angleRad: Double): Transform {
            val cosAngle = cos(angleRad)
            val sinAngle = sin(angleRad)

            return Transform(
                a = cosAngle,
                c = -sinAngle,
                e = 0.0,
                b = sinAngle,
                d = cosAngle,
                f = 0.0,
            )
        }
    }

    val inversed: Transform by lazy { this.calculateInversed() }

    private fun calculateInversed(): Transform {
        val de = a * d - b * c

        return Transform(
            a = d / de,
            b = b / -de,
            c = c / -de,
            d = a / de,
            e = (d * e - c * f) / -de,
            f = (b * e - a * f) / de
        )
    }

    operator fun times(that: Transform): Transform {
        val m1 = this
        val m2 = that

        return Transform(
            a = m1.a * m2.a + m1.c * m2.b,
            c = m1.a * m2.c + m1.c * m2.d,
            e = m1.a * m2.e + m1.c * m2.f + m1.e,
            b = m1.b * m2.a + m1.d * m2.b,
            d = m1.b * m2.c + m1.d * m2.d,
            f = m1.b * m2.e + m1.d * m2.f + m1.f,
        )
    }

    fun transform(v: IntVec2): IntVec2 =
        IntVec2(
            x = (a * v.x + c * v.y + e).roundToInt(),
            y = (b * v.x + d * v.y + f).roundToInt(),
        )

    fun transform(rect: IntRect): IntRect =
        rect.transform(this)

    fun transform(lineSeg: IntLineSeg): IntLineSeg =
        lineSeg.transform(this)
}

class DynamicTransform(
    // TODO: Support scaling
    val transform: Cell<Transform>,
) {
    val reversed: DynamicTransform by lazy {
        DynamicTransform(
            transform = transform.map { it.inversed },
        )
    }

    fun transform(point: Cell<IntVec2>): Cell<IntVec2> =
        Cell.map2(transform, point) { t, p -> t.transform(p) }

    fun transform(rect: Cell<IntRect>): Cell<IntRect> =
        Cell.map2(transform, rect) { t, r -> t.transform(r) }
}
