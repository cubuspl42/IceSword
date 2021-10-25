package icesword.geometry

data class IntLineSeg(
    val pointA: IntVec2,
    val pointB: IntVec2,
) {
    companion object {
        val ZERO: IntLineSeg = IntLineSeg(
            pointA = IntVec2.ZERO,
            pointB = IntVec2.ZERO,
        )
    }

    fun translate(t: IntVec2): IntLineSeg =
        IntLineSeg(
            pointA = pointA + t,
            pointB = pointB + t,
        )

    val length: Double
        get() = (pointB - pointA).length
}
