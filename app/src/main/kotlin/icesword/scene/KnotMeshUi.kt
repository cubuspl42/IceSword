package icesword.scene

import icesword.html.*
import icesword.TILE_SIZE
import icesword.editor.*
import icesword.editor.entities.KnotMesh
import icesword.editor.entities.knotCenter
import icesword.editor.entities.knotRect
import icesword.editor.modes.EntitySelectMode
import icesword.editor.modes.KnotSelectMode
import icesword.frp.*
import icesword.frp.Cell.Companion.constant
import icesword.geometry.DynamicTransform
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.tileRect
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
        strokeColor = Cell.map4(
            editor.isEntitySelected(knotMesh),
            editor.projectEntitySelectionState(knotMesh),
            editor.editorMode,
            editor.knotPaintOverReadyMode,
        ) {
                isSelectedNow,
                projectedSelectionStateNow,
                editorModeNow,
                knotPaintOverReadyModeNow,
            ->
            when {
                knotPaintOverReadyModeNow?.targetKnotMesh == knotMesh -> Color.lightBlue
                isSelectedNow && editorModeNow is KnotSelectMode -> selectionColor.withAlpha(0.3)

                // TODO: Deduplicate
                isSelectedNow && projectedSelectionStateNow == EntitySelectMode.SelectionState.NonSelected ->
                    Color.red.withAlpha(0.3)
                isSelectedNow || projectedSelectionStateNow == EntitySelectMode.SelectionState.Selected -> Color.red

                else -> {
                    val a = 128
                    rgba(a, a, a, 0.4)
                }
            }
        },
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

            val selectionColor = Color.red

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

    override val onDirty: Stream<Unit> =
        viewTransform.transform.values().units()
            .mergeWith(isSelected.values().units())
            .mergeWith(knotMesh.tileOffset.values().units())
            .mergeWith(knotMesh.localKnots.changes.units())
            .mergeWith(knotsInSelectionArea.changes.units())
            .mergeWith(selectedKnots.changes.units())
            .mergeWith(strokeColor.changes.units())
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
