package icesword.ui.world_view.scene

import icesword.TILE_SIZE
import icesword.editor.Editor
import icesword.editor.entities.KnotMesh
import icesword.editor.entities.knotCenter
import icesword.editor.entities.knotRect
import icesword.editor.modes.KnotSelectMode
import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.DynamicSet
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.mapTillRemoved
import icesword.frp.mergeWith
import icesword.frp.orElse
import icesword.frp.switchMap
import icesword.frp.units
import icesword.frp.values
import icesword.geometry.DynamicTransform
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.html.DynamicStyleDeclaration
import icesword.html.createSvgGroup
import icesword.html.createSvgRect
import icesword.html.linkSvgChildren
import icesword.tileRect
import icesword.ui.CanvasNode
import icesword.ui.EntityMoveDragController
import kotlinx.css.Color
import kotlinx.css.Cursor
import kotlinx.css.PointerEvents
import kotlinx.css.rgba
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement


class KnotMeshUi private constructor(
    private val viewTransform: DynamicTransform,
    private val knotMesh: KnotMesh,
    private val isSelected: Cell<Boolean>,
    private val knotsInSelectionArea: DynamicSet<IntVec2>,
    private val selectedKnots: DynamicSet<IntVec2>,
    private val strokeColor: Cell<Color>,
) : CanvasNode {
    companion object {
        val selectionColor = Color.red

        val fallbackColor = rgba(128, 128, 128, 0.4)
    }

    constructor(
        editor: Editor,
        viewTransform: DynamicTransform,
        knotMesh: KnotMesh,
    ) : this(
        viewTransform = viewTransform,
        knotMesh = knotMesh,
        isSelected = editor.isEntitySelected(knotMesh),
        knotsInSelectionArea = DynamicSet.diff(
            editor.knotSelectMode.map { knotSelectMode ->
                knotSelectMode?.knotsInSelectionArea ?: DynamicSet.empty()
            }
        ),
        selectedKnots = DynamicSet.diff(
            editor.knotSelectMode.switchMap { knotSelectModeOrNull ->
                knotSelectModeOrNull?.let {
                    it.selectedKnots.takeIf { _ -> it.knotMesh == knotMesh }
                } ?: constant(emptySet<IntVec2>())
            }
        ),
        strokeColor = buildSpecialKnotMeshStrokeColor(
            editor = editor,
            knotMesh = knotMesh,
        ).orElse(
            buildEntityStrokeColor(
                editor = editor,
                entity = knotMesh,
            ),
        ).orElse(constant(fallbackColor)),
    )

    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        val viewTransform = this.viewTransform.transform.sample()
        val tileOffset = knotMesh.tileOffset.sample()
        val localKnots = knotMesh.localKnots.volatileContentView
        val localTiles = knotMesh.localTileCoords.volatileContentView
        val knotsInSelectionArea = this.knotsInSelectionArea.volatileContentView
        val selectedKnots = this.selectedKnots.volatileContentView

        val isSelected = isSelected.sample()
        val strokeColorNow = strokeColor.sample()

        ctx.save()

        ctx.fillStyle = "grey"
//        ctx.strokeStyle = "black"
        ctx.lineWidth = 4.0
        ctx.globalAlpha = if (isSelected) 0.8 else 0.2

        // DRAW LOCAL TILES

        localTiles.forEach { localTileCoord ->
            val globalTileCoord = tileOffset + localTileCoord
            val rect = tileRect(globalTileCoord)
            val viewRect = viewTransform.transform(rect)

            // Here, window rect = viewport rect...
            if (windowRect.overlaps(viewRect)) {
                ctx.strokeStyle = strokeColorNow.value

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
            val center = viewTransform.transform(knotCenter(globalKnotCoord))
            val isInSelectionArea = knotsInSelectionArea.contains(globalKnotCoord)
            val isKnotSelected = selectedKnots.contains(globalKnotCoord)

            val knotRect = knotRect(globalKnotCoord = globalKnotCoord)

            val viewRect = viewTransform.transform(knotRect)

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

                ctx.strokeStyle = when {
                    isInSelectionArea -> "orange"
                    isKnotSelected -> "red"
                    else -> "black"
                }

                drawCircle(
                    ctx,
                    center = center,
                    radius = 12.0,
                )
            }
        }

        ctx.restore()
    }

    override
    val onDirty: Stream<Unit> =
        viewTransform.transform.values().units()
            .mergeWith(isSelected.values().units())
            .mergeWith(knotMesh.tileOffset.values().units())
            .mergeWith(knotMesh.localKnots.changes.units())
            .mergeWith(knotsInSelectionArea.changes.units())
            .mergeWith(selectedKnots.changes.units())
            .mergeWith(strokeColor.changes.units())
}

private fun buildSpecialKnotMeshStrokeColor(
    editor: Editor,
    knotMesh: KnotMesh,
): Cell<Color?> = Cell.map3(
    editor.isEntitySelected(knotMesh),
    editor.editorMode,
    editor.knotPaintOverReadyMode,
) {
        isSelectedNow,
        editorModeNow,
        knotPaintOverReadyModeNow,
    ->
    when {
        knotPaintOverReadyModeNow?.targetKnotMesh == knotMesh -> Color.lightBlue
        isSelectedNow && editorModeNow is KnotSelectMode -> KnotMeshUi.selectionColor.withAlpha(0.3)
        else -> null
    }
}

fun createKnotMeshOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    knotMesh: KnotMesh,
    viewport: HTMLElement,
    viewTransform: DynamicTransform,
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

        val tileOverlay = createSvgRect(
            svg = svg,
            size = constant(IntSize(TILE_SIZE, TILE_SIZE)),
            translate = constant(localTileCoord.times(TILE_SIZE)),
            style = DynamicStyleDeclaration(
                cursor = moveController.map { it?.let { Cursor.move } },
                pointerEvents = moveController.map {
                    if (it != null) PointerEvents.auto else PointerEvents.none
                },
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
