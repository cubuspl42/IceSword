package icesword.wwd

object Geometry {
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
        }

        val xMin: Int
            get() = left

        val yMin: Int
            get() = top

        val width: Int
            get() = right - left

        val height: Int
            get() = bottom - top

        val xMax: Int = right

        val yMax: Int = bottom

        fun overlaps(b: Rectangle): Boolean =
            this.xMin < b.xMax && b.xMin < this.xMax &&
                    this.yMin < b.yMax && b.yMin < this.yMax
    }
}
