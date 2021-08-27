package icesword.geometry

import kotlin.math.max
import kotlin.math.min

data class IntRect(
    val position: IntVec2,
    val size: IntSize,
) {
    companion object {
        fun fromDiagonal(a: IntVec2, b: IntVec2): IntRect {
            val tl = IntVec2(min(a.x, b.x), min(a.y, b.y))
            val br = IntVec2(max(a.x, b.x), max(a.y, b.y))
            val size = IntSize(br.x - tl.x, br.y - tl.y)
            return IntRect(position = tl, size = size)
        }
    }

    val xMin: Int = position.x

    val yMin: Int = position.y

    val width: Int = size.width

    val height: Int = size.height

    val xMax: Int =
        this.xMin + this.width

    val yMax: Int =
        this.yMin + this.height

    val xyMin: IntVec2 =
        IntVec2(this.xMin, this.yMin)

    val xyMax: IntVec2 =
        IntVec2(this.xMax, this.yMax)

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


    fun copyWithTopLeft(p: IntVec2) = IntRect.fromDiagonal(bottomRight, p)

    fun copyWithTopRight(p: IntVec2) = IntRect.fromDiagonal(bottomLeft, p)

    fun copyWithBottomRight(p: IntVec2) = IntRect.fromDiagonal(topLeft, p)

    fun copyWithBottomLeft(p: IntVec2) = IntRect.fromDiagonal(topRight, p)
}
