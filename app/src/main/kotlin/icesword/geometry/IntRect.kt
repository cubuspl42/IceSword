@file:UseSerializers(IntVec2Serializer::class)

package icesword.geometry

import icesword.editor.IntVec2Serializer
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

    fun overlaps(b: IntRect): Boolean =
        this.xMin < b.xMax && b.xMin < this.xMax &&
                this.yMin < b.yMax && b.yMin < this.yMax

    fun translate(t: IntVec2): IntRect =
        IntRect(position + t, size)

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

    fun copyWithTopLeft(p: IntVec2) = IntRect.fromDiagonal(bottomRight, p)

    fun copyWithTopRight(p: IntVec2) = IntRect.fromDiagonal(bottomLeft, p)

    fun copyWithBottomRight(p: IntVec2) = IntRect.fromDiagonal(topLeft, p)

    fun copyWithBottomLeft(p: IntVec2) = IntRect.fromDiagonal(topRight, p)

    fun contains(localPoint: IntVec2): Boolean =
        localPoint.x in (xMin until xMax) && localPoint.y in (yMin until yMax)


}
