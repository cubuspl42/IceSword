package icesword.ui.world_view.scene

import icesword.Debug
import icesword.TILE_SIZE
import icesword.editor.EditorTilesView
import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.tileAtPoint
import icesword.tileTopLeftCorner
import icesword.ui.CanvasNode
import org.w3c.dom.CENTER
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextAlign
import org.w3c.dom.CanvasTextBaseline
import org.w3c.dom.MIDDLE

class TileLayer(
    val tileset: Tileset,
    val tiles: DynamicView<EditorTilesView>,
) : CanvasNode {
    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        val editorTilesView = this.tiles.view

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

                val previewTileId = editorTilesView.previewTilesView.getTile(tileCoord)
                val primaryTileIdOrNull = editorTilesView.primaryTilesView.getTile(tileCoord)

                ctx.strokeStyle = "grey"
                ctx.lineWidth = 1.0

                fun drawTile(tileId: Int, alpha: Double) {
                    tileset.tileTextures[tileId]?.let { texture ->
                        val tilePosition = tileTopLeftCorner(tileCoord)

                        val dv = tilePosition

                        ctx.globalAlpha = alpha

                        drawTexture(ctx = ctx, texture = texture, dv = dv)

                        val tdv = dv + IntVec2.both(TILE_SIZE / 2)

                        if (Debug.showTileIds) {
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
                        }

                        ctx.globalAlpha = 0.2

                        ctx.strokeRect(
                            x = dv.x.toDouble(),
                            y = dv.y.toDouble(),
                            w = TILE_SIZE.toDouble(),
                            h = TILE_SIZE.toDouble(),
                        )
                    }
                }

                if (previewTileId != null) {
                    if (primaryTileIdOrNull != null) {
                        drawTile(tileId = primaryTileIdOrNull, alpha = 0.5)
                    }

                    drawTile(tileId = previewTileId, alpha = 0.5)
                } else if (primaryTileIdOrNull != null) {
                    drawTile(tileId = primaryTileIdOrNull, alpha = 1.0)
                }
            }
        }

        ctx.restore()
    }

    override val onDirty: Stream<Unit> = tiles.updates
}
