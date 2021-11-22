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

    val direction: IntVec2
        get() = pointB - pointA

    fun shorten(a: Int): IntLineSeg {
        val s = (a / length).coerceAtMost(0.5)
        val pointAPrim = pointA + direction.scale(s)
        val pointBPrim = pointB + direction.scale(-s)

        return IntLineSeg(
            pointA = pointAPrim,
            pointB = pointBPrim,
        )
    }

    fun translate(t: IntVec2): IntLineSeg =
        IntLineSeg(
            pointA = pointA + t,
            pointB = pointB + t,
        )

    fun transform(t: Transform): IntLineSeg =
        IntLineSeg(
            pointA = t.transform(pointA),
            pointB = t.transform(pointB),
        )

    val length: Double
        get() = (pointB - pointA).length
}
