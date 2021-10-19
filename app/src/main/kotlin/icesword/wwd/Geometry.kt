package icesword.wwd

import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

object Geometry {
    data class Vec2(val x: Int, val y: Int) {
        operator fun times(s: Int): Vec2 =
            Vec2(x * s, y * s)

        operator fun div(s: Int): Vec2 =
            Vec2(x / s, y / s)

        operator fun plus(that: Vec2): Vec2 =
            Vec2(x + that.x, y + that.y)

        operator fun minus(that: Vec2): Vec2 =
            Vec2(x - that.x, y - that.y)

        fun abs(): Vec2 = Vec2(x.absoluteValue, y.absoluteValue)

        fun neg(): Vec2 = Vec2(-x, -y)

        val width: Int
            get() = x.absoluteValue

        val height: Int
            get() = y.absoluteValue
    }

    data class Rectangle(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int,

        ) {
        companion object {
            val zero: Rectangle
                get() = Rectangle(0, 0, 0, 0)

            fun fromBounds(left: Int, top: Int, right: Int, bottom: Int): Rectangle {
                return Rectangle(left, top, right, bottom)
            }

            fun fromDiagonal(a: Vec2, b: Vec2): Rectangle =
                Rectangle(min(a.x, b.x), min(a.y, b.y), max(a.x, b.x), max(a.y, b.y))
        }

        val xMin: Int
            get() = left

        val yMin: Int
            get() = top

        private val width: Int
            get() = right - left

        private val height: Int
            get() = bottom - top

        val xMax: Int = right

        val yMax: Int = bottom

        private val xyMin: Vec2 = Vec2(this.xMin, this.yMin)

        private val xyMax: Vec2 = Vec2(this.xMax, this.yMax)

        fun overlaps(b: Rectangle): Boolean =
            this.xMin < b.xMax && b.xMin < this.xMax &&
                    this.yMin < b.yMax && b.yMin < this.yMax

        fun map(f: (Vec2) -> Vec2): Rectangle =
            Rectangle.fromDiagonal(
                f(this.xyMin),
                f(this.xyMax),
            )
    }
}
