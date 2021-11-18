package icesword.scene

import icesword.html.DynamicStyleDeclaration
import icesword.html.MouseButton
import icesword.html.createSvgCircle
import icesword.html.createSvgGroup
import icesword.html.createSvgRect
import icesword.html.onMouseDrag
import icesword.TILE_SIZE
import icesword.editor.Editor
import icesword.editor.Elastic
import icesword.editor.Entity
import icesword.editor.MetaTileCluster
import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.changesUnits
import icesword.frp.getKeys
import icesword.frp.map
import icesword.frp.mergeWith
import icesword.frp.reactTill
import icesword.frp.units
import icesword.frp.values
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.tileRect
import kotlinx.css.Cursor
import kotlinx.css.PointerEvents
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement


class ElasticUi private constructor(
    private val editor: Editor,
    private val viewTransform: Cell<IntVec2>,
    private val elastic: Elastic,
    private val isSelected: Cell<Boolean>,
) : Node {
    constructor(
        editor: Editor,
        viewTransform: Cell<IntVec2>,
        elastic: Elastic,
    ) : this(
        editor = editor,
        viewTransform = viewTransform,
        elastic = elastic,
        isSelected = editor.isEntitySelected(elastic)
    )

    private val metaTileCluster: MetaTileCluster
        get() = elastic.metaTileCluster

    private val localTileCoords = metaTileCluster.localMetaTiles.getKeys()

    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {

        val viewTransform = this.viewTransform.sample()
        val bounds = elastic.bounds.sample()
        val isSelected = isSelected.sample()

        ctx.strokeStyle = if (isSelected) "red" else "rgba(103, 103, 131, 0.3)"

        bounds.points().forEach { globalTileCoord ->
            val viewTileRect = tileRect(globalTileCoord).translate(viewTransform)

            ctx.lineWidth = 1.0

            ctx.strokeRect(
                x = viewTileRect.xMin.toDouble(),
                y = viewTileRect.yMin.toDouble(),
                w = viewTileRect.width.toDouble(),
                h = viewTileRect.height.toDouble(),
            )
        }

        val viewBounds = bounds.translate(viewTransform)

        ctx.lineWidth = 4.0

        ctx.strokeRect(
            x = viewBounds.xMin.toDouble(),
            y = viewBounds.yMin.toDouble(),
            w = viewBounds.width.toDouble(),
            h = viewBounds.height.toDouble(),
        )
    }

    override val onDirty: Stream<Unit> =
        viewTransform.values().units()
            .mergeWith(metaTileCluster.localMetaTiles.changesUnits())
            .mergeWith(elastic.bounds.values().units())
            .mergeWith(isSelected.values().units())
            .mergeWith(localTileCoords.changes.units())
}


fun createElasticOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: Cell<IntVec2>,
    elastic: Elastic,
    tillDetach: Till,
): SVGElement {
    val boxSize = elastic.size.map { it * TILE_SIZE }

    val rootTranslate = Cell.map2(
        viewTransform,
        elastic.position,
    ) { vt, ep -> vt + ep }

    val isSelected = editor.isEntitySelected(elastic)

    val pointerEvents = isSelected.map {
        if (it) null else PointerEvents.none
    }

    val box = createEntityFrameElement(
        editor = editor,
        svg = svg,
        outer = viewport,
        entity = elastic,
        boundingBox = boxSize.map { it.toRect() },
        tillDetach = tillDetach,
    )

    val handleStroke = isSelected.map {
        if (it) "red" else "none"
    }

    fun createHandle(
        cursor: Cursor,
        resize: (tileCoord: Cell<IntVec2>, till: Till) -> Unit,
        sx: Int,
        sy: Int,
    ): SVGElement {
        val handle = createSvgCircle(
            svg = svg,
            translate = boxSize.map { IntVec2(it.width * sx, it.height * sy) },
            radius = 8.0f,
            stroke = handleStroke,
            style = DynamicStyleDeclaration(
                pointerEvents = pointerEvents,
                cursor = Cell.constant(cursor),
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
        ).reactTill(tillDetach) { mouseDrag ->
            val initialPosition = mouseDrag.position.sample()

            // TODO: Support dragging handle and camera at the same time
            val deltaTileCoord = mouseDrag.position.map {
                val deltaPosition = it - initialPosition
                deltaPosition.divRound(TILE_SIZE)
            }

            resize(
                deltaTileCoord,
                mouseDrag.tillEnd,
            )
        }

        return handle
    }

    fun buildHandles(): List<SVGElement> {
        val handles = listOf(
            createHandle(
                cursor = Cursor.nwseResize,
                resize = elastic::resizeTopLeft,
                sx = 0, sy = 0,
            ),
            createHandle(
                cursor = Cursor.neswResize,
                resize = elastic::resizeTopRight,
                sx = 1, sy = 0,
            ),
            createHandle(
                cursor = Cursor.nwseResize,
                resize = elastic::resizeBottomRight,
                sx = 1, sy = 1,
            ),
            createHandle(
                cursor = Cursor.neswResize,
                resize = elastic::resizeBottomLeft,
                sx = 0, sy = 1,
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
    boundingBox: Cell<IntRect>,
    tillDetach: Till,
): SVGElement {
    return createDraggableOverlayElement(
        editor = editor,
        entity = entity,
        outer = outer,
        till = tillDetach,
    ) { cursor ->
        val isAreaSelectionCovered = editor.isAreaSelectionCovered(entity)

        val isSelected = editor.isEntitySelected(entity)

        val pointerEvents = isSelected.map {
            if (it) null else PointerEvents.none
        }

        val stroke = Cell.map2(
            isSelected,
            isAreaSelectionCovered,
        ) { isSel, isCovered ->
            when {
                isCovered -> "orange"
                isSel -> "red"
                else -> "none"
            }
        }

        val box = createSvgRect(
            svg = svg,
            size = boundingBox.map { it.size },
            translate = boundingBox.map { it.position },
            stroke = stroke,
            style = DynamicStyleDeclaration(
                pointerEvents = pointerEvents,
                cursor = cursor,
            ),
            tillDetach = tillDetach,
        ).apply {
            setAttributeNS(null, "fill-opacity", "0")
        }

        box
    }
}
