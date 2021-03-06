@file:UseSerializers(IntVec2Serializer::class)

package icesword.geometry

import icesword.editor.IntVec2Serializer
import icesword.frp.Cell
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.reduce
import icesword.wwd.Geometry
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.math.max
import kotlin.math.min

@Serializable
data class IntRect(
    val position: IntVec2,
    val size: IntSize,
) {
    companion object {
        val ZERO: IntRect = IntRect(
            position = IntVec2.ZERO,
            size = IntSize.ZERO,
        )

        fun unit(position: IntVec2): IntRect = IntRect(
            position = position,
            size = IntSize.UNIT,
        )

        fun fromRectangle(rect: Geometry.Rectangle): IntRect = IntRect(
            position = IntVec2(rect.left, rect.top),
            size = IntSize(rect.width, rect.height),
        )

        fun fromDiagonal(pointA: IntVec2, pointC: IntVec2): IntRect {
            val tl = IntVec2(min(pointA.x, pointC.x), min(pointA.y, pointC.y))
            val br = IntVec2(max(pointA.x, pointC.x), max(pointA.y, pointC.y))
            val size = IntSize(br.x - tl.x, br.y - tl.y)
            return IntRect(position = tl, size = size)
        }

        fun fromCenter(
            center: IntVec2,
            sideLength: Int,
        ): IntRect = IntRect(
            position = center - IntVec2(sideLength / 2, sideLength / 2),
            size = IntSize(width = sideLength, height = sideLength),
        )

        fun fromLtrb(
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
        ): IntRect = IntRect(
            position = IntVec2(left, top),
            size = IntSize(right - left, bottom - top)
        )

        fun enclosing(rects: List<IntRect>): IntRect =
            rects.reduce { acc, rect -> enclosing(acc, rect) }

        fun enclosing(rects: DynamicList<IntRect>): Cell<IntRect> =
            rects.reduce { acc, rect -> enclosing(acc, rect) }

        fun enclosing(
            acc: IntRect,
            rect: IntRect,
        ): IntRect = fromDiagonal(
            pointA = IntVec2(
                x = min(acc.xMin, rect.xMin),
                y = min(acc.yMin, rect.yMin),
            ),
            pointC = IntVec2(
                x = max(acc.xMax, rect.xMax),
                y = max(acc.yMax, rect.yMax),
            ),
        )

//        fun containing(points: Iterable<IntVec2>): IntRect {
//            val minX = points.minOfOrNull { it.x }!!
//            val minY = points.minOfOrNull { it.y }!!
//            val maxX = points.maxOfOrNull { it.x }!!
//            val maxY = points.maxOfOrNull { it.y }!!
//
//            return fromLtrb(
//                left = minX,
//                top = minY,
//                // Add 1, because this method treats points as tiny rects
//                right = maxX + 1,
//                bottom = maxY + 1,
//            )
//        }
    }

    val center: IntVec2
        get() = position + (size.toVec2() / 2)

    val xMin: Int
        get() = position.x

    val yMin: Int
        get() = position.y

    val width: Int
        get() = size.width

    val height: Int
        get() = size.height

    val xMax: Int
        get() = this.xMin + this.width

    val yMax: Int
        get() = this.yMin + this.height

    val left: Int
        get() = xMin

    val top: Int
        get() = yMin

    val right: Int
        get() = xMax

    val bottom: Int
        get() = yMax

    val xyMin: IntVec2
        get() = IntVec2(this.xMin, this.yMin)

    val xyMax: IntVec2
        get() = IntVec2(this.xMax, this.yMax)

    val topLeft: IntVec2
        get() = xyMin

    val topRight: IntVec2
        get() = IntVec2(this.xMax, this.yMin)

    val bottomRight: IntVec2
        get() = xyMax

    val bottomLeft: IntVec2
        get() = IntVec2(this.xMin, this.yMax)

    val area: Int
        get() = width * height

    fun overlaps(b: IntRect): Boolean =
        this.xMin < b.xMax && b.xMin < this.xMax &&
                this.yMin < b.yMax && b.yMin < this.yMax

    fun translate(t: IntVec2): IntRect =
        IntRect(position + t, size)

    fun transform(t: Transform): IntRect = fromDiagonal(
        pointA = t.transform(topLeft),
        pointC = t.transform(bottomRight),
    )

    fun points(): Sequence<IntVec2> = sequence {
        for (y in yMin until yMax) {
            for (x in xMin until xMax) {
                yield(IntVec2(x, y))
            }
        }
    }

    operator fun div(s: Int): IntRect = IntRect(
        position = this.position / s,
        size = this.size / s,
    )

    operator fun times(s: Int): IntRect = IntRect(
        position = this.position * s,
        size = this.size * s,
    )

    fun copyWithTopLeft(p: IntVec2) = fromDiagonal(bottomRight, p)

    fun copyWithTopRight(p: IntVec2) = fromDiagonal(bottomLeft, p)

    fun copyWithBottomRight(p: IntVec2) = fromDiagonal(topLeft, p)

    fun copyWithBottomLeft(p: IntVec2) = fromDiagonal(topRight, p)

    fun contains(point: IntVec2): Boolean =
        point.x in (xMin until xMax) && point.y in (yMin until yMax)

    fun toRectangle() = Geometry.Rectangle(
        left = xMin,
        top = yMin,
        right = xMax,
        bottom = yMax,
    )

    fun expand(n: Int): IntRect = fromDiagonal(
        topLeft - IntVec2.both(n),
        bottomRight + IntVec2.both(n),
    )
}
