package icesword.geometry

import icesword.frp.Cell
import icesword.frp.map
import org.w3c.dom.DOMMatrix
import org.w3c.dom.DOMMatrixReadOnly
import org.w3c.dom.svg.SVGSVGElement
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
        val identity: Transform = Transform(
            a = 1.0,
            c = 0.0,
            e = 0.0,
            b = 0.0,
            d = 1.0,
            f = 0.0,
        )

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

    // The returned object is actually an instance of SVGMatrix
    fun toSVGMatrix(svg: SVGSVGElement): DOMMatrixReadOnly {
        val t = this
        return svg.createSVGMatrix().apply {
            a = t.a
            b = t.b
            c = t.c
            d = t.d
            e = t.e
            f = t.f
        }
    }

    fun toDOMMatrix(): DOMMatrixReadOnly =
        DOMMatrixReadOnly(arrayOf(a, b, c, d, e, f))
}

class DynamicTransform(
    // TODO: Support scaling
    val transform: Cell<Transform>,
) {
    companion object {
        val identity: DynamicTransform = DynamicTransform(
           transform = Cell.constant(Transform.identity),
        )

        fun translate(t: Cell<IntVec2>): DynamicTransform = DynamicTransform(
            t.map(Transform.Companion::translate)
        )
    }

    val inversed: DynamicTransform by lazy {
        DynamicTransform(
            transform = transform.map { it.inversed },
        )
    }

    fun transform(point: Cell<IntVec2>): Cell<IntVec2> =
        Cell.map2(transform, point) { t, p -> t.transform(p) }

    fun transform(rect: Cell<IntRect>): Cell<IntRect> =
        Cell.map2(transform, rect) { t, r -> t.transform(r) }

    operator fun times(that: DynamicTransform): DynamicTransform =
        DynamicTransform(
            Cell.map2(transform, that.transform) { t1, t2 -> t1 * t2 },
        )

    operator fun times(t2: Transform): DynamicTransform =
        DynamicTransform(
            transform.map { t -> t * t2 },
        )
}
