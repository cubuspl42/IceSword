package icesword.scene

import icesword.html.*
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


class KnotMeshUi private constructor(
    private val editor: Editor,
    private val viewTransform: Cell<IntVec2>,
    private val knotMesh: KnotMesh,
    private val isSelected: Cell<Boolean>,
    private val isCovered: Cell<Boolean>,
) : Node {
    constructor(
        editor: Editor,
        viewTransform: Cell<IntVec2>,
        knotMesh: KnotMesh
    ) : this(
        editor = editor,
        viewTransform = viewTransform,
        knotMesh = knotMesh,
        isSelected = editor.isEntitySelected(knotMesh),
        isCovered = editor.isAreaSelectionCovered(knotMesh),
    )


    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        val viewTransform = this.viewTransform.sample()
        val tileOffset = knotMesh.tileOffset.sample()
        val localKnots = knotMesh.localKnots.volatileContentView
        val localTiles = knotMesh.localTileCoords.volatileContentView

        val isSelected = isSelected.sample()
        val isCovered = isCovered.sample()

        ctx.save()

        ctx.fillStyle = "grey"
//        ctx.strokeStyle = "black"
        ctx.lineWidth = 4.0
        ctx.globalAlpha = if (isSelected) 0.8 else 0.2

        // DRAW LOCAL TILES

        localTiles.forEach { localTileCoord ->
            val globalTileCoord = tileOffset + localTileCoord
            val rect = tileRect(globalTileCoord)
            val viewRect = rect.translate(viewTransform)

            // Here, window rect = viewport rect...
            if (windowRect.overlaps(viewRect)) {

                when {
                    isCovered -> {
                        ctx.strokeStyle = "orange"
                    }
                    isSelected -> {
                        ctx.strokeStyle = "red"
                    }
                    else -> {
                        val a = 128
                        ctx.strokeStyle = "rgba($a, $a, $a, 0.4)"
                    }
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

        ctx.restore()
    }

    override val onDirty: Stream<Unit> =
        viewTransform.values().units()
            .mergeWith(isSelected.values().units())
            .mergeWith(isCovered.values().units())
            .mergeWith(knotMesh.tileOffset.values().units())
            .mergeWith(knotMesh.localKnots.changes.units())
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
