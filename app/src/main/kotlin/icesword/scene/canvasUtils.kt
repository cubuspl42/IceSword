package icesword.scene

import icesword.geometry.IntVec2
import icesword.geometry.Transform
import org.w3c.dom.CanvasRenderingContext2D
import kotlin.math.PI
import kotlin.math.sqrt

fun CanvasRenderingContext2D.setTransformT(transform: Transform) {
    this.setTransform(
        transform.a,
        transform.b,
        transform.c,
        transform.d,
        transform.e,
        transform.f,
    )
}

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

fun drawTexture(
    ctx: CanvasRenderingContext2D,
    texture: Texture,
    dv: IntVec2
) {
    val w = texture.sourceRect.width.toDouble()
    val h = texture.sourceRect.height.toDouble()

    ctx.drawImage(
        image = texture.imageBitmap,
        sx = texture.sourceRect.xMin.toDouble(),
        sy = texture.sourceRect.yMin.toDouble(),
        sw = w,
        sh = h,
        dx = dv.x.toDouble(),
        dy = dv.y.toDouble(),
        dw = w,
        dh = h,
    )
}
