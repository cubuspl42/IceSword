package icesword.scene

import icesword.editor.StartPoint
import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.mergeWith
import icesword.frp.units
import icesword.frp.values
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import org.w3c.dom.CanvasRenderingContext2D
import kotlin.math.PI
import kotlin.math.sqrt


class StartPointUi(
    private val viewTransform: Cell<IntVec2>,
    private val startPoint: StartPoint,
) : Node {
    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        val viewTransform = this.viewTransform.sample()

        val position = startPoint.position.sample()
        val viewPosition = position + viewTransform

        ctx.fillStyle = "darkgray"
        ctx.strokeStyle = "black"
        ctx.lineWidth = 4.0

        drawCircle(
            ctx = ctx,
            center = viewPosition,
            radius = 32.0,
        )

        ctx.fillStyle = "gray"


        drawEquilateralTriangle(
            ctx = ctx,
            center = viewPosition,
            sideLength = 42,
        )
    }

    override val onDirty: Stream<Unit> =
        viewTransform.values().units()
            .mergeWith(startPoint.position.values().units())
}

private fun drawCircle(
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

private fun drawEquilateralTriangle(
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

