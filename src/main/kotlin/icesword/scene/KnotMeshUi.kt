package icesword.scene

import icesword.editor.KnotMesh
import icesword.editor.knotCenter
import icesword.editor.tilesAroundKnot
import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.tileRect
import org.w3c.dom.CanvasRenderingContext2D
import kotlin.math.PI


class KnotMeshUi(
    private val viewTransform: Cell<IntVec2>,
    private val knotMesh: KnotMesh,
) : Node {
    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        val viewTransform = this.viewTransform.sample()
        val tileOffset = knotMesh.tileOffset.sample()
        val localKnots = knotMesh.localKnots.sample()
        val localTiles = localKnots.flatMap(::tilesAroundKnot).toSet()
        val isSelected = knotMesh.isSelected.sample()

        ctx.fillStyle = "grey"
//        ctx.strokeStyle = "black"
        ctx.lineWidth = 4.0

        localTiles.forEach { localTileCoord ->
            val globalTileCoord = tileOffset + localTileCoord
            val rect = tileRect(globalTileCoord).translate(viewTransform)

            if (isSelected) {
                ctx.strokeStyle = "red"
            } else {
                val a = 128
                ctx.strokeStyle = "rgba($a, $a, $a, 0.4)"
            }

            ctx.strokeRect(
                x = rect.xMin.toDouble(),
                y = rect.yMin.toDouble(),
                w = rect.width.toDouble(),
                h = rect.height.toDouble(),
            )
        }

        localKnots.forEach { localKnotCoord ->
            val globalKnotCoord = tileOffset + localKnotCoord
            val center = knotCenter(globalKnotCoord) + viewTransform

            val rect = tileRect(globalKnotCoord)
                .translate(IntVec2(32, 32))
                .translate(viewTransform)

            val a = 64
            ctx.strokeStyle = "rgba($a, $a, $a, 0.5)"

            ctx.strokeRect(
                x = rect.xMin.toDouble(),
                y = rect.yMin.toDouble(),
                w = rect.width.toDouble(),
                h = rect.height.toDouble(),
            )

            ctx.strokeStyle = "black"

            drawCircle(
                ctx,
                center = center,
                radius = 12.0,
            )
        }
    }

    override val onDirty: Stream<Unit> =
        viewTransform.values().units()
            .mergeWith(knotMesh.isSelected.values().units())
            .mergeWith(knotMesh.tileOffset.values().units())
            .mergeWith(knotMesh.localKnots.changes().units())
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
