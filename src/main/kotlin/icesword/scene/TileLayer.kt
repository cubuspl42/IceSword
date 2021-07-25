package icesword.scene

import icesword.TILE_SIZE
import icesword.frp.DynamicMap
import icesword.frp.Stream
import icesword.frp.changes
import icesword.frp.sample
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.tileAtPoint
import icesword.tileTopLeftCorner
import org.w3c.dom.CanvasRenderingContext2D

class TileLayer(
    val tileset: Tileset,
    val tiles: DynamicMap<IntVec2, Int>,
) : Node {
    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        val tiles = this.tiles.sample()

        val xyMinTileCoord = tileAtPoint(windowRect.xyMin)
        val xyMaxTileCoord = tileAtPoint(windowRect.xyMax)

        (xyMinTileCoord.y..xyMaxTileCoord.y).forEach { i ->
            (xyMinTileCoord.x..xyMaxTileCoord.x).forEach { j ->
                val tileCoord = IntVec2(j, i)
                val tileId = tiles[tileCoord] ?: -1

                tileset.tileTextures[tileId]?.let { texture ->
                    val tilePosition = tileTopLeftCorner(tileCoord)

                    ctx.drawImage(
                        image = texture.imageBitmap,
                        sx = texture.sourceRect.xMin.toDouble(),
                        sy = texture.sourceRect.yMin.toDouble(),
                        sw = texture.sourceRect.width.toDouble(),
                        sh = texture.sourceRect.height.toDouble(),
                        dx = tilePosition.x.toDouble(),
                        dy = tilePosition.y.toDouble(),
                        dw = TILE_SIZE.toDouble(),
                        dh = TILE_SIZE.toDouble(),
                    )
                }
            }
        }
    }

    override val onDirty: Stream<Unit> = tiles.changes()
}
