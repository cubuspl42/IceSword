package icesword.geometry

data class IntRect(
    val position: IntVec2,
    val size: IntSize,
) {
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

    fun overlaps(b: IntRect): Boolean =
        this.xMin < b.xMax && b.xMin < this.xMax &&
                this.yMin < b.yMax && b.yMin < this.yMax

    fun translate(t: IntVec2): IntRect =
        IntRect(position + t, size)
}
