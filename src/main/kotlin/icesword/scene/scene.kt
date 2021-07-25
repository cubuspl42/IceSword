package icesword.scene

import icesword.TILE_SIZE
import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.tileAtPoint
import icesword.tileTopLeftCorner
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.*
import kotlin.math.roundToInt


data class Texture(
    val imageBitmap: ImageBitmap,
    val sourceRect: IntRect,
)

class Tileset(
    val tileTextures: Map<Int, Texture>,
)

interface Node {
    fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect)

    val onDirty: Stream<Unit>
}

class Layer(
    private val transform: Cell<IntVec2>,
    private val nodes: List<Node>,
) {
    fun draw(ctx: CanvasRenderingContext2D, viewportRect: IntRect) {
        val transform = this.transform.sample()

        val inverseTransform = -transform

        val windowRect = viewportRect.translate(inverseTransform)

        ctx.resetTransform()
        ctx.translate(transform.x.toDouble(), transform.y.toDouble())

        nodes.forEach {
            it.draw(ctx, windowRect)
        }
    }

    val onDirty: Stream<Unit> =
        transform.values().units()
            .mergeWith(Stream.merge(nodes.map { it.onDirty }))
}

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

class SceneContext {
//    fun createTexture(image: Image): Texture =
//        Texture(image)
}

class Scene(
    val layers: List<Layer>,
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

            style.apply {
                width = "100%"
                height = "100%"
            }
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

        val onDirty = Stream.merge(scene.layers.map { it.onDirty })

        onDirty.reactTill(tillDetach) {
            isDirty.set(true)
        }

        return isDirty
    }

    val scene = buildScene()

    val canvas = createSceneCanvas()

    val isDirty = buildDirtyFlag(scene)

    val ctx = canvas.getContext("2d").unsafeCast<CanvasRenderingContext2D>()

    val layers = scene.layers

    fun resizeCanvasIfNeeded() {
        val rectSize = canvas.getBoundingClientRect().size

        if (canvas.size != rectSize) {
            canvas.size = rectSize
            isDirty.set(true)
        }
    }

    fun drawScene() {
        val viewportRect = canvas.size.toRect()

        ctx.resetTransform()

        ctx.clearRect(
            x = viewportRect.xMin.toDouble(),
            y = viewportRect.yMin.toDouble(),
            w = viewportRect.width.toDouble(),
            h = viewportRect.height.toDouble(),
        )

        layers.forEach {
            it.draw(ctx, viewportRect = viewportRect)
        }
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
