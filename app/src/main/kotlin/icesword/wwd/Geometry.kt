package icesword.wwd

import kotlin.math.absoluteValue
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

    data class Rectangle(val position: Vec2, val size: Vec2) {
        companion object {
            val zero: Rectangle
                get() = Rectangle(Vec2(0, 0), Vec2(0, 0))

            fun fromCenter(center: Vec2, size: Vec2): Rectangle {
                val sizeAbs = size.abs()
                return Rectangle(sizeAbs.neg() / 2, sizeAbs)
            }

            fun fromBounds(left: Int, top: Int, right: Int, bottom: Int): Rectangle {
                return Rectangle(Vec2(left, top), Vec2(right - left, bottom - top))
            }

            fun fromDiagonal(a: Vec2, b: Vec2): Rectangle =
                Rectangle(Vec2(min(a.x, b.x), min(a.y, b.y)), b - a)
        }

        val xMin: Int
            get() = position.x

        val yMin: Int
            get() = position.y

        private val width: Int
            get() = size.width

        private val height: Int
            get() = size.height

        val xMax: Int = this.xMin + this.width

        val yMax: Int = this.yMin + this.height

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
