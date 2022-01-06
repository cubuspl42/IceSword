package icesword.ui.scene

import icesword.EditorTextureBank
import icesword.RezTextureBank
import icesword.html.createHTMLElementRaw
import icesword.html.createSvgGroup
import icesword.html.createSvgRoot
import icesword.html.linkSvgChildren
import icesword.frp.Cell
import icesword.frp.DynamicSet
import icesword.frp.MutCell
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.changesUnits
import icesword.frp.dynamic_list.map
import icesword.frp.dynamic_list.mapTillRemoved
import icesword.frp.reactTill
import icesword.frp.units
import icesword.frp.values
import icesword.geometry.DynamicTransform
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.html.createSvgGroupDl
import icesword.loadImageBitmap
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.css.BackgroundRepeat
import kotlinx.css.px
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.DOMRect
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.ImageBitmap
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement
import kotlin.math.roundToInt


data class Texture(
    val path: String,
    val imageBitmap: ImageBitmap,
    val sourceRect: IntRect,
) {
    companion object {
        suspend fun load(imagePath: String): Texture {
            val imageBitmap = loadImageBitmap(imagePath = imagePath)!!

            val texture = Texture(
                path = imagePath,
                imageBitmap = imageBitmap,
                sourceRect = IntRect(
                    position = IntVec2.ZERO,
                    size = IntSize(
                        width = imageBitmap.width,
                        height = imageBitmap.height,
                    ),
                )
            )

            return texture
        }
    }

    fun createImage(
        width: Int? = null,
        height: Int? = null,
    ): HTMLElement {
        val element = document.createElement("div") as HTMLDivElement

        val effectiveWidth = (width ?: sourceRect.width).px
        val effectiveHeight = (height ?: sourceRect.height).px

        return element.apply {
            style.apply {
                this.width = effectiveWidth.value
                this.height = effectiveHeight.value
                backgroundImage = "url('$path')"
                backgroundRepeat = BackgroundRepeat.noRepeat.toString()
                backgroundPosition = "${(-sourceRect.xMin)}px ${(-sourceRect.yMin)}px"
                backgroundSize = "$effectiveWidth $effectiveHeight"
            }
        }
    }
}

class Tileset(
    val tileTextures: Map<Int, Texture>,
)

interface CanvasNode {
    companion object {
        fun ofSingle(node: Cell<CanvasNode?>): CanvasNode = GroupCanvasNode(
            children = DynamicList.ofSingle(node),
        )
    }

    fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect)

    val onDirty: Stream<Unit>
}

class NoopCanvasNode : CanvasNode {
    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
    }

    override val onDirty: Stream<Unit> = Stream.never()
}

abstract class HybridNode {
    companion object {
        fun ofSingle(node: Cell<HybridNode?>): HybridNode = GroupNode(
            children = DynamicList.ofSingle(node)
        )
    }

    data class CanvasNodeBuildContext(
        val editorTextureBank: EditorTextureBank,
        val textureBank: RezTextureBank,
    )

    data class OverlayBuildContext(
        val svg: SVGSVGElement,
        val viewport: HTMLElement,
        val viewTransform: DynamicTransform,
        val tillDetach: Till,
    )

    open fun buildCanvasNode(
        context: CanvasNodeBuildContext,
    ): CanvasNode = NoopCanvasNode()

    open fun buildOverlayElement(
        context: OverlayBuildContext,
    ): SVGElement = createSvgGroup(
        svg = context.svg,
        children = DynamicSet.empty(),
        tillDetach = context.tillDetach
    )

    override fun equals(other: Any?): Boolean {
        println("HybridNode.equals")
//        throw UnsupportedOperationException()
        return false
    }

    override fun hashCode(): Int {
        println("HybridNode.hashCode")
        //        throw UnsupportedOperationException()
        return 0
    }
}

typealias BuildOverlayElements = (SVGSVGElement) -> DynamicSet<SVGElement>

class Layer(
    editorTextureBank: EditorTextureBank,
    textureBank: RezTextureBank,
    private val viewTransform: DynamicTransform,
    nodes: DynamicSet<CanvasNode>,
    private val buildOverlayElements: BuildOverlayElements? = null,
    private val hybridNodes: DynamicList<HybridNode> = DynamicList.empty(),
    tillDetach: Till,
) {
    private val canvasNodes = DynamicList.concat(
        nodes.internalOrder,
        hybridNodes.mapTillRemoved(tillDetach) { hybridNode, _ ->
            hybridNode.buildCanvasNode(
                context = HybridNode.CanvasNodeBuildContext(
                    editorTextureBank = editorTextureBank,
                    textureBank = textureBank,
                )
            )
        },
    )

    fun draw(ctx: CanvasRenderingContext2D, viewportRect: IntRect) {
        val transform = this.viewTransform.transform.sample()

        val inverseTransform = transform.inversed

        val windowRect = inverseTransform.transform(viewportRect)

        ctx.setTransform(
            transform.a,
            transform.b,
            transform.c,
            transform.d,
            transform.e,
            transform.f,
        )

        canvasNodes.volatileContentView.forEach {
            it.draw(ctx, windowRect)
        }
    }

    val onDirty: Stream<Unit> =
        Stream.merge(
            listOf(
                viewTransform.transform.values().units(),
                canvasNodes.changesUnits(),
                DynamicList.merge(canvasNodes.map { it.onDirty }),
            )
        )

//    fun buildOverlayRoot(
//        svg: SVGSVGElement,
//        viewport: HTMLElement,
//        tillDetach: Till,
//    ): SVGElement = createSvgGroup(
//        svg = svg,
//        translate = transform,
//        tillDetach = tillDetach,
//    ).also { root ->
//        root.setAttribute("class", "layerOverlay")
//
//        val buildOverlayElements = this.buildOverlayElements
//
//        val overlayElements1 = if (buildOverlayElements != null) {
//            buildOverlayElements(svg)
//        } else DynamicSet.of(emptySet())
//
//        val overlayElements2 = hybridNodes.mapNotNull {
//            it.buildOverlayElement(
//                context = HybridNode.OverlayBuildContext(
//                    svg = svg,
//                    viewport = viewport,
//                    viewTransform = viewTransform,
//                    tillDetach = tillDetach,
//                )
//            )
//        }
//
//        linkSvgChildren(
//            element = root,
//            children = DynamicSet.union2(overlayElements1, overlayElements2),
//            till = tillDetach,
//        )
//    }

    fun buildOverlayRoot(
        svg: SVGSVGElement,
        viewport: HTMLElement,
        tillDetach: Till,
    ): SVGElement {
        // An overlay that applies view transform on the group level, so its
        // children are mostly fixed relatively to the group.
        fun buildFixedOverlay(): SVGElement {
            val buildOverlayElements = this.buildOverlayElements

            val overlayElements = if (buildOverlayElements != null) {
                buildOverlayElements(svg)
            } else DynamicSet.of(emptySet())

            return createSvgGroup(
                svg = svg,
                transform = viewTransform,
                children = overlayElements,
                tillDetach = tillDetach,
            )
        }


        // An overlay that does not apply view transform on the group level,
        // leaving it to its children. Effectively, it means that children are
        // in the screen space.
        fun buildAdjustingOverlay(): SVGElement {
            val overlayElements2 = hybridNodes.mapTillRemoved(tillDetach) { it, tillRemoved ->
                it.buildOverlayElement(
                    context = HybridNode.OverlayBuildContext(
                        svg = svg,
                        viewport = viewport,
                        viewTransform = viewTransform,
                        tillDetach = tillRemoved,
                    )
                )
            }

            return createSvgGroupDl(
                svg = svg,
                children = overlayElements2,
                tillDetach = tillDetach,
            ).apply {
                classList.add("adjustingOverlay")
            }
        }

        return createSvgGroup(
            svg = svg,
            children = DynamicSet.of(setOf(
                buildFixedOverlay(),
                buildAdjustingOverlay(),
            )),
            tillDetach = tillDetach,
        )
    }
}

class SceneContext {
//    fun createTexture(image: Image): Texture =
//        Texture(image)
}

class Scene(
    val layers: List<Layer>,
    val buildOverlayElements: BuildOverlayElements,
)

fun scene(
    viewport: HTMLElement,
    tillDetach: Till,
    builder: (SceneContext) -> Scene,
): HTMLElement {
    fun createSceneCanvas(): HTMLCanvasElement {
        val canvas = document.createElement("canvas") as HTMLCanvasElement

        canvas.apply {
            width = 1024
            height = 1024

            className = "scene-canvas"
        }

        return canvas
    }

    fun createSceneOverlay(scene: Scene): SVGElement {
        val overlay = createSvgRoot(
            tillDetach = tillDetach,
        ).apply {
            setAttribute("class", "svgOverlay")
        }

        val layersOverlays = scene.layers.map {
            it.buildOverlayRoot(
                svg = overlay,
                viewport = viewport,
                tillDetach = tillDetach,
            )
        }

        val topOverlayElements = scene.buildOverlayElements(overlay)

        val topOverlay = createSvgGroup(
            svg = overlay,
            translate = Cell.constant(IntVec2.ZERO),
            tillDetach = tillDetach,
        ).apply {
            setAttribute("class", "topOverlay")
            linkSvgChildren(this, topOverlayElements, till = tillDetach)
        }

        layersOverlays.forEach(overlay::appendChild)
        overlay.appendChild(topOverlay)

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

    val sceneOverlay = createSceneOverlay(scene)

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
            if (!tillDetach.wasReached()) {
                handleAnimationFrame()
                requestAnimationFrames()
            }
        }
    }

    requestAnimationFrames()

    val root = createHTMLElementRaw("div").apply {
        className = "scene"

        style.apply {
            display = "grid"

            setProperty("grid-template-columns", "minmax(0, 1fr)")
            setProperty("grid-template-rows", "minmax(0, 1fr)")
            setProperty("place-items", "stretch")
        }

        appendChild(
            canvas.apply {
                style.apply {
                    setProperty("grid-column", "1")
                    setProperty("grid-row", "1")
                    zIndex = "0"
                }
            },
        )

        appendChild(
            sceneOverlay.apply {
                style.apply {
                    setProperty("grid-column", "1")
                    setProperty("grid-row", "1")

                    zIndex = "1"
                }
            }
        )
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