package icesword.scene

import icesword.geometry.IntVec2
import org.w3c.dom.CanvasRenderingContext2D
import kotlin.math.PI
import kotlin.math.sqrt

fun drawCircle(
    ctx: CanvasRenderingContext2D,
    center: IntVec2,
    radius: Double,
) {
    ctx.beginPath()
    ctx.arc(
        x = center.x.toDouble(),
        y = center.y.toDouble(),
        radius = radius,
        startAngle = 0.0,
        endAngle = 2 * PI,
        anticlockwise = false,
    )
    ctx.fill()
    ctx.stroke()
}

fun drawEquilateralTriangle(
    ctx: CanvasRenderingContext2D,
    center: IntVec2,
    sideLength: Int,
) {
    val a = sideLength
    val h = (a * sqrt(3.0) / 2).toInt()

    ctx.beginPath()

    val p0 = center + IntVec2(-h / 3, 0)

    val p1 = p0 + IntVec2(0, -a / 2)
    val p2 = p0 + IntVec2(0, a / 2)
    val p3 = p0 + IntVec2(h, 0)

    ctx.moveTo(p1.x.toDouble(), p1.y.toDouble())
    ctx.lineTo(p2.x.toDouble(), p2.y.toDouble())
    ctx.lineTo(p3.x.toDouble(), p3.y.toDouble())
    ctx.closePath()

    ctx.fill()
    ctx.stroke()
}