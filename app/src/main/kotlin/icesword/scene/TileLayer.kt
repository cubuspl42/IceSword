package icesword.scene

import icesword.TILE_SIZE
import icesword.editor.TilesView
import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.tileAtPoint
import icesword.tileTopLeftCorner
import org.w3c.dom.CanvasRenderingContext2D

class TileLayer(
    val tileset: Tileset,
    val tiles: DynamicView<TilesView>,
) : Node {
    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        val tilesView: TilesView = this.tiles.view

        val xyMinTileCoord = tileAtPoint(windowRect.xyMin)
        val xyMaxTileCoord = tileAtPoint(windowRect.xyMax)

        ctx.save()

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
