package icesword.ui.world_view.scene

import icesword.EditorTextureBank
import icesword.RezTextureBank
import icesword.ui.world_view.MouseDrag
import icesword.TILE_SIZE
import icesword.editor.Editor
import icesword.editor.entities.Elastic
import icesword.editor.entities.ElasticProduct
import icesword.editor.entities.Entity
import icesword.editor.modes.EntitySelectMode.SelectionState
import icesword.editor.Tool
import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.Till
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.map
import icesword.frp.dynamic_list.staticListOf
import icesword.frp.map
import icesword.frp.orElse
import icesword.frp.reactDynamicNotNullTill
import icesword.geometry.DynamicTransform
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.html.DynamicStyleDeclaration
import icesword.html.MouseButton
import icesword.html.createSvgCircle
import icesword.html.createSvgGroup
import icesword.html.createSvgRect
import icesword.html.onMouseDrag
import icesword.ui.CanvasNode
import icesword.ui.world_view.EntityNode
import icesword.ui.world_view.EntityNodeB
import icesword.ui.world_view.WapNode
import icesword.ui.world_view.scene.base.HybridNode
import kotlinx.css.Color
import kotlinx.css.Cursor
import kotlinx.css.PointerEvents
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement

fun createElasticNode(elastic: Elastic): EntityNodeB = object : EntityNodeB {
    override fun build(context: EntityNodeB.BuildContext): EntityNode = context.run {
        EntityNode(
            wapNodes = createElasticProductWapNodes(
                editorTextureBank = editorTextureBank,
                textureBank = textureBank,
                elasticProduct = elastic.product,
            ),
            overlayNode = ElasticOverlayNode(
                editor = editor,
                elastic = elastic,
            ),
        )
    }
}

class ElasticProductNode(
    private val elasticProduct: ElasticProduct,
    private val alpha: Double,
) : HybridNode() {
    override fun buildCanvasNode(
        context: CanvasNodeBuildContext,
    ): CanvasNode = GroupCanvasNode(
        children = elasticProduct.wapObjectSprites.map { wapSprite ->
            WapSpriteNode(
                editorTextureBank = context.editorTextureBank,
                textureBank = context.textureBank,
                wapSprite = wapSprite,
                alpha = alpha,
            )
        }
    )
}

fun createElasticProductWapNodes(
    editorTextureBank: EditorTextureBank,
    textureBank: RezTextureBank,
    elasticProduct: ElasticProduct,
    alpha: Double = 1.0,
): DynamicList<WapNode> = elasticProduct.wapObjectSprites.map { wapSprite ->
    WapNode.fromWapSprite(
        editorTextureBank = editorTextureBank,
        textureBank = textureBank,
        wapSprite = wapSprite,
        alpha = alpha,
    )
}

class ElasticOverlayNode(
    private val editor: Editor,
    private val elastic: Elastic,
) : HybridNode() {
    override fun buildOverlayElement(
        context: OverlayBuildContext,
    ): SVGElement = context.run {
        createElasticOverlayElement(
            editor = editor,
            svg = svg,
            viewport = viewport,
            viewTransform = viewTransform,
            elastic = elastic,
            tillDetach = tillDetach,
        )
    }
}

private fun createElasticOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: DynamicTransform,
    elastic: Elastic,
    tillDetach: Till,
): SVGElement {
    val elasticProduct = elastic.product

    val relativeViewBounds = viewTransform.transform(elasticProduct.pixelBounds)
        .map { it.copy(position = IntVec2.ZERO) }

    val rootTranslate = viewTransform.transform(elasticProduct.pixelBoundsTopLeft)

    val isSelected = editor.isEntitySelected(elastic)

    val box = createEntityFrameElement(
        editor = editor,
        svg = svg,
        outer = viewport,
        entity = elastic,
        viewBoundingBox = relativeViewBounds,
        tillDetach = tillDetach,
    )

    fun createHandle(
        dragCursor: Cursor,
        resize: (tileCoord: Cell<IntVec2>, till: Till) -> Unit,
        corner: (IntRect) -> IntVec2,
    ): SVGElement {
        val dragHandler = Cell.map2(
            editor.editorMode,
            isSelected,
        ) { editorModeNow, isSelectedNow ->
            if (editorModeNow == Tool.MOVE && isSelectedNow) { mouseDrag: MouseDrag ->
                val worldPosition = viewTransform.inversed.transform(mouseDrag.clientPosition)

                val initialPosition = worldPosition.sample()

                // TODO: Support dragging handle and camera at the same time
                val deltaTileCoord = worldPosition.map {
                    val deltaPosition = it - initialPosition
                    deltaPosition.divRound(TILE_SIZE)
                }

                resize(
                    deltaTileCoord,
                    mouseDrag.released,
                )
            } else null
        }

        val pointerEvents = dragHandler.map {
            if (it != null) PointerEvents.auto
            else PointerEvents.none
        }

        val cursor = dragHandler.map {
            if (it != null) dragCursor
            else null
        }

        val handleStroke = Cell.map2(
            isSelected,
            dragHandler,
        ) {
                isSelectedNow,
                dragHandlerNow,
            ->
            val color = if (isSelectedNow) Color.red else Color.lightGray
            val alpha = if (dragHandlerNow != null) 1.0 else 0.3

            color.withAlpha(alpha)
        }

        val handle = createSvgCircle(
            svg = svg,
            translate = relativeViewBounds.map(corner),
            radius = 8.0f,
            stroke = handleStroke,
            style = DynamicStyleDeclaration(
                pointerEvents = pointerEvents,
                cursor = cursor,
            ),
            tillDetach = tillDetach,
        ).apply {
            setAttributeNS(null, "fill", "grey")
            setAttributeNS(null, "fill-opacity", "0.5")
            setAttributeNS(null, "stroke-width", "3")

            style.apply {
                boxSizing = "border-box"
                backgroundColor = "grey"
            }
        }

        handle.onMouseDrag(
            button = MouseButton.Primary,
            outer = viewport,
            till = tillDetach,
        ).reactDynamicNotNullTill(dragHandler, tillDetach)

        return handle
    }

    fun buildHandles(): List<SVGElement> {
        val handles = listOf(
            createHandle(
                dragCursor = Cursor.nwseResize,
                resize = elastic::resizeTopLeft,
                corner = { it.topLeft },
            ),
            createHandle(
                dragCursor = Cursor.neswResize,
                resize = elastic::resizeTopRight,
                corner = { it.topRight },
            ),
            createHandle(
                dragCursor = Cursor.nwseResize,
                resize = elastic::resizeBottomRight,
                corner = { it.bottomRight },
            ),
            createHandle(
                dragCursor = Cursor.neswResize,
                resize = elastic::resizeBottomLeft,
                corner = { it.bottomLeft },
            )
        )

        return handles
    }

    val group = createSvgGroup(
        svg = svg,
        translate = rootTranslate,
        tillDetach = tillDetach,
    )

    group.appendChild(box)
    buildHandles().forEach(group::appendChild)

    return group
}

fun createEntityFrameElement(
    editor: Editor,
    svg: SVGSVGElement,
    outer: HTMLElement,
    entity: Entity,
    viewBoundingBox: Cell<IntRect>,
    tillDetach: Till,
): SVGElement =
    createDraggableOverlayElement(
        editor = editor,
        entity = entity,
        outer = outer,
        till = tillDetach,
    ) { context ->
        val stroke: Cell<Color> = buildEntityStrokeColor(
            editor = editor,
            entity = entity,
        ).orElse(constant(Color.transparent))

        val box = createSvgRect(
            svg = svg,
            size = viewBoundingBox.map { it.size },
            translate = viewBoundingBox.map { it.position },
            stroke = stroke,
            style = DynamicStyleDeclaration(
                pointerEvents = context.map { it.pointerEvents },
                cursor = context.map { it.cursor },
            ),
            tillDetach = tillDetach,
        ).apply {
            setAttributeNS(null, "fill-opacity", "0")
        }

        box
    }

fun buildEntityStrokeColor(
    editor: Editor,
    entity: Entity,
): Cell<Color?> {
    val isSelected = editor.isEntitySelected(entity)

    val isFocused = editor.isEntityFocused(entity)

    val projectedSelectionState = editor.projectEntitySelectionState(entity)

    return Cell.map3(
        isSelected,
        isFocused,
        projectedSelectionState,
    ) {
            isSelectedNow,
            isFocusedNow,
            projectedSelectionStateNow,
        ->
        when {
            isFocusedNow -> Color.blueViolet.withAlpha(0.8)
            isSelectedNow && projectedSelectionStateNow == SelectionState.NonSelected ->
                Color.red.withAlpha(0.3)
            isSelectedNow || projectedSelectionStateNow == SelectionState.Selected -> Color.red
            else -> null
        }
    }
}
