package icesword.scene

import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.*

const val TILE_SIZE = 64

data class Texture(
    val imageBitmap: ImageBitmap,
    val sourceRect: IntRect,
)

class Tileset(
    val tileTextures: Map<Int, Texture>,
)

interface Node {
    fun draw(ctx: CanvasRenderingContext2D)
}

class TileLayer(
    val tileset: Tileset,
    val tiles: Map<IntVec2, Int>,
) : Node {
    override fun draw(ctx: CanvasRenderingContext2D) {
        tiles.forEach { (tileOffset, tileId) ->
            val texture = tileset.tileTextures[tileId]!!
            val pixelOffset = tileOffset * TILE_SIZE

            ctx.drawImage(
                image = texture.imageBitmap,
                sx = texture.sourceRect.xMin.toDouble(),
                sy = texture.sourceRect.yMin.toDouble(),
                sw = texture.sourceRect.width.toDouble(),
                sh = texture.sourceRect.height.toDouble(),
                dx = pixelOffset.x.toDouble(),
                dy = pixelOffset.y.toDouble(),
                dw = TILE_SIZE.toDouble(),
                dh = TILE_SIZE.toDouble(),
            )
        }
    }
}

class SceneContext {
//    fun createTexture(image: Image): Texture =
//        Texture(image)
}

fun scene(
    builder: (SceneContext) -> Node
): HTMLElement {
    val canvas = document.createElement("canvas") as HTMLCanvasElement

    canvas.apply {
        width = 1024
        height = 1024
    }

    canvas.style.apply {
        width = "100%"
        height = "100%"
    }

    canvas.addEventListener("contextmenu", { it.preventDefault() })

    val ctx = canvas.getContext("2d").unsafeCast<CanvasRenderingContext2D>()

    val context = SceneContext()
    val root = builder(context)

    fun handleAnimationFrame() {
        val rect = canvas.getBoundingClientRect()

        canvas.width = rect.width.toInt()
        canvas.height = rect.height.toInt()

        root.draw(ctx)
    }

    fun requestAnimationFrames() {
        window.requestAnimationFrame {
            handleAnimationFrame()
//            requestAnimationFrames()
        }
    }

    requestAnimationFrames()

    return canvas
}

private fun createHtmlElement(tagName: String): HTMLElement =
    document.createElement(tagName) as HTMLElement
