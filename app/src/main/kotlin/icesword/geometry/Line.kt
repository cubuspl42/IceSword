package icesword.geometry

import kotlin.math.absoluteValue
import kotlin.math.roundToInt

// Line with equation Ax + By + C = 0
data class Line(
    val a: Double,
    val b: Double,
    val c: Double,
) {
    companion object {
        val vertical = Line(a = 1.0, b = 0.0, c = 0.0)

        val horizontal = Line(a = 0.0, b = 1.0, c = 0.0)

        val neSw = Line(a = -1.0, b = 1.0, c = 0.0)

        val nwSe = Line(a = 1.0, b = 1.0, c = 0.0)
    }

    fun intersection(other: Line): IntVec2? {
        val epsilon = 0.01
        val l1 = this
        val l2 = other

        val d = l1.a * l2.b - l2.a * l1.b

        return if (d.absoluteValue > epsilon) {
            IntVec2(
                x = ((l1.b * l2.c - l2.b * l1.c) / d).roundToInt(),
                y = ((l1.c * l2.a - l2.c * l1.a) / d).roundToInt(),
            )
        } else null
    }

    // Line with the same slope as this one, but containing the given point
    fun includingPoint(point: IntVec2): Line {
        val p = point
        val c2 = -(a * p.x + b * p.y)
        return Line(a = a, b = b, c = c2)
    }
}
