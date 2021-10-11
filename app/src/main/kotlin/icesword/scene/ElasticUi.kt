package icesword.scene

import html.DynamicStyleDeclaration
import html.createHtmlElement
import html.createStyledHtmlElement
import html.createSvgCircle
import html.createSvgGroup
import html.createSvgRect
import html.onMouseDrag
import icesword.*
import icesword.editor.Editor
import icesword.editor.Elastic
import icesword.editor.MetaTileCluster
import icesword.editor.Tool
import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.ui.EntityMoveDragController
import kotlinx.css.Cursor
import kotlinx.css.button
import kotlinx.css.style
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement


class ElasticUi(
    private val viewTransform: Cell<IntVec2>,
    private val elastic: Elastic,
) : Node {
    private val metaTileCluster: MetaTileCluster
        get() = elastic.metaTileCluster

    private val localTileCoords = metaTileCluster.localMetaTilesDynamic.getKeys()

    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {

        val viewTransform = this.viewTransform.sample()
        val tileOffset = elastic.tileOffset.sample()
        val size = elastic.size.sample()
        val isSelected = elastic.isSelected.sample()

        val localTileCoords = localTileCoords.volatileContentView

        ctx.strokeStyle = if (isSelected) "red" else "rgba(103, 103, 131, 0.3)"

        localTileCoords.forEach { localTileCoord ->
            val globalTileCoord = tileOffset + localTileCoord
            val rect = tileRect(globalTileCoord).translate(viewTransform)

            ctx.lineWidth = 1.0

            ctx.strokeRect(
                x = rect.xMin.toDouble(),
                y = rect.yMin.toDouble(),
                w = rect.width.toDouble(),
                h = rect.height.toDouble(),
            )
        }

        val sizeRect = IntRect(
            tileTopLeftCorner(tileOffset),
            size * TILE_SIZE,
        ).translate(viewTransform)

        ctx.lineWidth = 4.0

        ctx.strokeRect(
            x = sizeRect.xMin.toDouble(),
            y = sizeRect.yMin.toDouble(),
            w = sizeRect.width.toDouble(),
            h = sizeRect.height.toDouble(),
        )
    }

    override val onDirty: Stream<Unit> =
        viewTransform.values().units()
            .mergeWith(metaTileCluster.localMetaTilesDynamic.changesUnits())
            .mergeWith(elastic.tileOffset.values().units())
            .mergeWith(elastic.size.values().units())
            .mergeWith(elastic.isSelected.values().units())
            .mergeWith(localTileCoords.changes().units())
}


fun createElasticOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: Cell<IntVec2>,
    elastic: Elastic,
    tillDetach: Till,
): SVGElement {
    val boxMoveController = EntityMoveDragController.create(
        editor = editor,
        entity = elastic,
    )

    val boxCursor = boxMoveController.map {
        it?.let { Cursor.move }
    }

    val boxSize = elastic.size.map { it * TILE_SIZE }

    val rootTranslate = Cell.map2(
        viewTransform,
        elastic.position,
    ) { vt, ep -> vt + ep }

    val box = createSvgRect(
        svg = svg,
        size = boxSize,
        translate = Cell.constant(IntVec2.ZERO),
        style = DynamicStyleDeclaration(
            cursor = boxCursor,
        ),
        tillDetach = tillDetach,
    ).apply {
        setAttributeNS(null, "fill-opacity", "0")

        style.apply {
            boxSizing = "border-box"
            borderStyle = "dashed"
            borderRadius = "4px"
            borderColor = "red"
        }
    }

    EntityMoveDragController.linkDragHandler(
        element = box,
        outer = viewport,
        controller = boxMoveController,
        till = tillDetach,
    )

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
            style = DynamicStyleDeclaration(
                cursor = Cell.constant(cursor),
            ),
            tillDetach = tillDetach,
        ).apply {
            setAttributeNS(null, "fill", "grey")
            setAttributeNS(null, "stroke", "red")
            setAttributeNS(null, "stroke-width", "3")

            style.apply {
                boxSizing = "border-box"
                backgroundColor = "grey"
            }
        }

        handle.onMouseDrag(
            button = 0,
            outer = viewport,
            till = tillDetach,
        ).reactTill(tillDetach) { mouseDrag ->
            val initialPosition = mouseDrag.position.sample()

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
