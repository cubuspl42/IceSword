package icesword.scene

import html.*
import icesword.TILE_SIZE
import icesword.editor.*
import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.tileRect
import icesword.ui.EntityMoveDragController
import kotlinx.css.Cursor
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement
import kotlin.math.PI


class KnotMeshUi(
    private val viewTransform: Cell<IntVec2>,
    private val knotMesh: KnotMesh,
) : Node {
    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        val viewTransform = this.viewTransform.sample()
        val tileOffset = knotMesh.tileOffset.sample()
        val localKnots = knotMesh.localKnots.volatileContentView
        val localTiles = knotMesh.localTileCoords.volatileContentView
        val isSelected = knotMesh.isSelected.sample()

        ctx.fillStyle = "grey"
//        ctx.strokeStyle = "black"
        ctx.lineWidth = 4.0

        // DRAW LOCAL TILES

        localTiles.forEach { localTileCoord ->
            val globalTileCoord = tileOffset + localTileCoord
            val rect = tileRect(globalTileCoord)
            val viewRect = rect.translate(viewTransform)

            // Here, window rect = viewport rect...
            if (windowRect.overlaps(viewRect)) {

                if (isSelected) {
                    ctx.strokeStyle = "red"
                } else {
                    val a = 128
                    ctx.strokeStyle = "rgba($a, $a, $a, 0.4)"
                }

                ctx.strokeRect(
                    x = viewRect.xMin.toDouble(),
                    y = viewRect.yMin.toDouble(),
                    w = viewRect.width.toDouble(),
                    h = viewRect.height.toDouble(),
                )
            }
        }

        // DRAW LOCAL KNOTS

        localKnots.forEach { localKnotCoord ->
            val globalKnotCoord = tileOffset + localKnotCoord
            val center = knotCenter(globalKnotCoord) + viewTransform

            val knotRect = tileRect(globalKnotCoord)
                .translate(IntVec2(32, 32))

            val viewRect = knotRect.translate(viewTransform)

            // Here, window rect = viewport rect...
            if (windowRect.overlaps(viewRect)) {

                val a = 64
                ctx.strokeStyle = "rgba($a, $a, $a, 0.5)"

                ctx.strokeRect(
                    x = viewRect.xMin.toDouble(),
                    y = viewRect.yMin.toDouble(),
                    w = viewRect.width.toDouble(),
                    h = viewRect.height.toDouble(),
                )

                ctx.strokeStyle = "black"

                drawCircle(
                    ctx,
                    center = center,
                    radius = 12.0,
                )
            }
        }
    }

    override val onDirty: Stream<Unit> =
        viewTransform.values().units()
            .mergeWith(knotMesh.isSelected.values().units())
            .mergeWith(knotMesh.tileOffset.values().units())
            .mergeWith(knotMesh.localKnots.changes().units())
}

private fun drawCircle(
    ctx: CanvasRenderingContext2D,
    center: IntVec2,
    radius: Double,
) {
    ctx.beginPath()
    ctx.arc(
        x = center.x.toDouble(),
        y = center.y.toDouble(),
        radius = radius,
        startAngle = 0.0,
        endAngle = 2 * PI,
        anticlockwise = false,
    )
    ctx.fill()
    ctx.stroke()
}

fun createKnotMeshOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    knotMesh: KnotMesh,
    viewport: HTMLElement,
    viewTransform: Cell<IntVec2>,
    tillDetach: Till,
): SVGElement {
    fun createTileOverlay(
        localTileCoord: IntVec2,
        till: Till,
    ): SVGElement {
        val moveController = EntityMoveDragController.create(
            editor = editor,
            entity = knotMesh,
        )

//        val pixelCoord = localTileCoord.times(TILE_SIZE)

        val tileOverlay = createSvgRect(
            svg = svg,
            size = Cell.constant(IntSize(TILE_SIZE, TILE_SIZE)),
            translate = Cell.constant(localTileCoord.times(TILE_SIZE)),
            style = DynamicStyleDeclaration(
                cursor = moveController.map { it?.let { Cursor.move } },
            ),
            tillDetach = till,
        ).apply {
            setAttributeNS(null, "fill-opacity", "0")

            style.apply {
                borderStyle = "dashed"
                borderRadius = "4px"
                borderColor = "yellow"
            }
        }

        EntityMoveDragController.linkDragHandler(
            element = tileOverlay,
            outer = viewport,
            controller = moveController,
            till = till,
        )

        return tileOverlay
    }

    val root = createSvgGroup(
        svg = svg,
        translate = knotMesh.tileOffset.map { it.times(TILE_SIZE) },
        tillDetach = tillDetach,
    ).apply {
        setAttribute("class", "knotMeshOverlay")
    }

    val tileOverlays = knotMesh.localTileCoords
        .mapTillRemoved(tillDetach) { localTileCoord, tillRemoved ->
            createTileOverlay(
                localTileCoord = localTileCoord,
                till = tillRemoved,
            )
        }

    linkSvgChildren(
        element = root,
        children = tileOverlays,
        till = tillDetach,
    )

    return root
}