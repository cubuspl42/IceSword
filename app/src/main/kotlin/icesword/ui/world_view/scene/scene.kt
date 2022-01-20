package icesword.ui.world_view.scene

import icesword.EditorTextureBank
import icesword.RezTextureBank
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.mapTillRemoved
import icesword.frp.dynamic_list.staticListOf
import icesword.geometry.DynamicTransform
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.html.HTMLWidget
import icesword.html.HTMLWidgetB
import icesword.html.createSvgGroupDl
import icesword.html.createSvgRoot
import icesword.loadImageBitmap
import icesword.ui.CanvasNode
import icesword.ui.createCanvasView
import icesword.ui.createStackWb
import icesword.ui.world_view.scene.base.HybridNode
import kotlinx.browser.document
import kotlinx.css.BackgroundRepeat
import kotlinx.css.px
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.ImageBitmap
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement


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

class NoopCanvasNode : CanvasNode {
    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
    }

    override val onDirty: Stream<Unit> = Stream.never()
}

class Layer(
    editorTextureBank: EditorTextureBank,
    textureBank: RezTextureBank,
    private val viewTransform: DynamicTransform,
    private val backFoil: SVGElement,
    private val hybridNodes: DynamicList<HybridNode> = DynamicList.empty(),
    // Hybrid nodes that have un-transformed (viewport-space) CanvasNode. Overlay node is ignored.
    private val hybridViewportCanvasNodes: DynamicList<HybridNode> = DynamicList.empty(),
    // Hybrid nodes that have transformed (plane-space) overlay node. Canvas node is ignored.
    private val hybridContentOverlayNodes: DynamicList<HybridNode> = DynamicList.empty(),
    tillDetach: Till,
) {
    private val canvasNode = run {
        fun buildCanvasNodes(hybridNodes: DynamicList<HybridNode>): DynamicList<CanvasNode> =
            hybridNodes.mapTillRemoved(tillDetach) { it, _ ->
                it.buildCanvasNode(
                    context = HybridNode.CanvasNodeBuildContext(
                        editorTextureBank = editorTextureBank,
                        textureBank = textureBank,
                    ),
                )
            }

        CanvasNode.group(
            children = DynamicList.concat(
                staticListOf(
                    CanvasNode.transform(
                        children = buildCanvasNodes(hybridNodes),
                        viewTransform = viewTransform,
                    ),
                ),
                buildCanvasNodes(hybridViewportCanvasNodes),
            ),
        )
    }

    fun asCanvasNode() = canvasNode

    fun buildOverlayRoot(
        svg: SVGSVGElement,
        viewport: HTMLElement,
        tillDetach: Till,
    ): SVGElement {
        fun buildOverlayElements(hybridNodes: DynamicList<HybridNode>): DynamicList<SVGElement> =
            hybridNodes.mapTillRemoved(tillDetach) { it, tillRemoved ->
                it.buildOverlayElement(
                    context = HybridNode.OverlayBuildContext(
                        svg = svg,
                        viewport = viewport,
                        viewTransform = viewTransform,
                        tillDetach = tillRemoved,
                    )
                )
            }

        // An overlay that applies view transform on the group level, so its
        // children are mostly fixed relatively to the group.
        fun buildFixedOverlay(): SVGElement {
            val overlayElements =
                buildOverlayElements(hybridContentOverlayNodes)

            return createSvgGroupDl(
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
            val overlayElements = buildOverlayElements(hybridNodes)

            return createSvgGroupDl(
                svg = svg,
                children = overlayElements,
                tillDetach = tillDetach,
            ).apply {
                classList.add("adjustingOverlay")
            }
        }

        return createSvgGroupDl(
            svg = svg,
            children = staticListOf(
                backFoil,
                buildFixedOverlay(),
                buildAdjustingOverlay(),
            ),
            tillDetach = tillDetach,
        )
    }
}

class Scene(
    val layers: List<Layer>,
)

fun createScene(
    viewport: HTMLElement,
    tillDetach: Till,
    builder: () -> Scene,
): HTMLWidgetB<*> {
    fun createSceneOverlay(scene: Scene): SVGSVGElement {
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

        layersOverlays.forEach(overlay::appendChild)

        return overlay
    }

    fun buildScene(): Scene = builder()

    val scene = buildScene()

    val layers = scene.layers

    val canvas = createCanvasView(
        root = GroupCanvasNode(
            children = DynamicList.of(
                layers.map { it.asCanvasNode() },
            ),
        )
    )

    val sceneOverlay = createSceneOverlay(scene)

    val root = createStackWb(
        children = staticListOf(
            canvas,
            HTMLWidget.of(sceneOverlay),
        )
    )

    return root
}
