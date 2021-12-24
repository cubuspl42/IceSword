package icesword.scene

import icesword.TILE_SIZE
import icesword.editor.TilesView
import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.tileAtPoint
import icesword.tileTopLeftCorner
import org.w3c.dom.CENTER
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextAlign
import org.w3c.dom.CanvasTextBaseline
import org.w3c.dom.MIDDLE
import org.w3c.dom.TOP

class TileLayer(
    val tileset: Tileset,
    val tiles: DynamicView<TilesView>,
) : CanvasNode {
    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        val tilesView: TilesView = this.tiles.view

        val xyMinTileCoord = tileAtPoint(windowRect.xyMin)
        val xyMaxTileCoord = tileAtPoint(windowRect.xyMax)

        ctx.save()

        ctx.fillStyle = "white"
        ctx.lineWidth = 1.0

        ctx.font = "20px Arial"
        ctx.textAlign = CanvasTextAlign.CENTER
        ctx.textBaseline = CanvasTextBaseline.MIDDLE

        (xyMinTileCoord.y..xyMaxTileCoord.y).forEach { i ->
            (xyMinTileCoord.x..xyMaxTileCoord.x).forEach { j ->
                val tileCoord = IntVec2(j, i)
                val tileId = tilesView.getTile(tileCoord) ?: -1

                ctx.strokeStyle = "grey"
                ctx.lineWidth = 1.0

                tileset.tileTextures[tileId]?.let { texture ->
                    val tilePosition = tileTopLeftCorner(tileCoord)

                    val dv = tilePosition

                    ctx.globalAlpha = 1.0

                    drawTexture(ctx = ctx, texture = texture, dv = dv)

                    val tdv = dv + IntVec2.both(TILE_SIZE / 2)

                    ctx.strokeStyle = "black"

                    ctx.strokeText(
                        tileId.toString(),
                        x = tdv.x.toDouble(),
                        y = tdv.y.toDouble(),
                    )

                    ctx.fillText(
                        tileId.toString(),
                        x = tdv.x.toDouble(),
                        y = tdv.y.toDouble(),
                    )

                    ctx.globalAlpha = 0.2

                    ctx.strokeRect(
                        x = dv.x.toDouble(),
                        y = dv.y.toDouble(),
                        w = TILE_SIZE.toDouble(),
                        h = TILE_SIZE.toDouble(),
                    )
                }
            }
        }

        ctx.restore()
    }

    override val onDirty: Stream<Unit> = tiles.updates
}
