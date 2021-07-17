package icesword.scene

import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


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

class Group(
    val children: List<Node>,
) {

    fun draw(ctx: CanvasRenderingContext2D) {

    }
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

class Scene(
    val root: Node,
    val cameraFocusPoint: Cell<IntVec2>,
)

fun scene(
    tillDetach: Till,
    builder: (SceneContext) -> Scene,
): HTMLElement {
    fun createSceneCanvas(): HTMLCanvasElement {
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

        return canvas
    }

    fun buildScene(): Scene {
        val context = SceneContext()

        return builder(context)
    }

    fun buildDirtyFlag(scene: Scene): MutCell<Boolean> {
        val isDirty = MutCell(true)

        val onDirty = scene.cameraFocusPoint.values().map { }

        onDirty.reactTill(tillDetach) {
            isDirty.set(true)
        }

        return isDirty
    }

    val scene = buildScene()

    val canvas = createSceneCanvas()

    val isDirty = buildDirtyFlag(scene)

    val ctx = canvas.getContext("2d").unsafeCast<CanvasRenderingContext2D>()

    val root = scene.root

    fun resizeCanvasIfNeeded() {
        val rectSize = canvas.getBoundingClientRect().size

        if (canvas.size != rectSize) {
            canvas.size = rectSize
            isDirty.set(true)
        }
    }

    fun drawScene() {
        ctx.resetTransform()

        ctx.clearRect(
            x = 0.0,
            y = 0.0,
            w = canvas.width.toDouble(),
            h = canvas.height.toDouble(),
        )

        val tv = -scene.cameraFocusPoint.sample()

        ctx.translate(tv.x.toDouble(), tv.y.toDouble())

        root.draw(ctx)
    }

    fun handleAnimationFrame() {
        resizeCanvasIfNeeded()

        if (isDirty.sample()) {
            drawScene()
            isDirty.set(false)
        }
    }

    fun requestAnimationFrames() {
        window.requestAnimationFrame {
            handleAnimationFrame()
            requestAnimationFrames()
        }
    }

    requestAnimationFrames()

    return canvas
}

private val DOMRect.size: IntSize
    get() = IntSize(
        this.width.roundToInt(),
        this.height.roundToInt(),
    )

private var HTMLCanvasElement.size: IntSize
    get() = IntSize(
        this.width,
        this.height,
    )
    set(value) {
        this.width = value.width
        this.height = value.height
    }
