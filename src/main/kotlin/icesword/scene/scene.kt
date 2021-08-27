package icesword.scene

import icesword.createContainer
import icesword.createHtmlElement
import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
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
    private val nodes: DynamicSet<Node>,
) {
    fun draw(ctx: CanvasRenderingContext2D, viewportRect: IntRect) {
        val transform = this.transform.sample()

        val inverseTransform = -transform

        val windowRect = viewportRect.translate(inverseTransform)

        ctx.resetTransform()
        ctx.translate(transform.x.toDouble(), transform.y.toDouble())

        nodes.volatileContentView.forEach {
            it.draw(ctx, windowRect)
        }
    }

    val onDirty: Stream<Unit> =
        transform.values().units()
            .mergeWith(DynamicSet.merge(nodes.map { it.onDirty }))
}

class SceneContext {
//    fun createTexture(image: Image): Texture =
//        Texture(image)
}

class Scene(
    val layers: List<Layer>,
    val overlayElements: DynamicSet<HTMLElement>,
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

                setProperty("grid-column", "1")
                setProperty("grid-row", "1")
                zIndex = "0"
            }
        }

        return canvas
    }

    fun createSceneOverlay(scene: Scene): HTMLElement {


        val overlay = createContainer(
            children = scene.overlayElements,
            tillDetach,
        ).apply {
            style.apply {
                setProperty("grid-column", "1")
                setProperty("grid-row", "1")
                zIndex = "1"
            }
        }

        return overlay
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

    val overlay = createSceneOverlay(scene)

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
            ctx.resetTransform()
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

    val root = createHtmlElement("div").apply {
        style.width = "100%"
        style.height = "100%"
        style.display = "grid"

        appendChild(canvas)
        appendChild(overlay)
    }

    return root
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
