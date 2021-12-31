package icesword.scene

import icesword.editor.EditPathElevatorMode
import icesword.editor.EditorMode
import icesword.editor.EntitySelectMode
import icesword.editor.KnotPaintMode
import icesword.editor.knotRect
import icesword.frp.Cell.Companion.constant
import icesword.frp.map
import icesword.frp.mapNested
import icesword.frp.switchMapNested
import icesword.html.DynamicStyleDeclaration
import icesword.html.createSvgRectR
import icesword.html.createSvgSwitch
import kotlinx.css.Color
import kotlinx.css.PointerEvents
import org.w3c.dom.svg.SVGElement

fun createEditorModeModeNode(
    editorMode: EditorMode,
): HybridNode? = when (editorMode) {
    is EditPathElevatorMode -> EditPathElevatorModeNode(editorMode)
    is KnotPaintMode -> KnotPaintModeNode(editorMode)
    is EntitySelectMode -> EntitySelectModeNode(entitySelectMode = editorMode)
    else -> null
}

class EditPathElevatorModeNode(
    private val editMode: EditPathElevatorMode,
) : HybridNode() {
    override fun buildOverlayElement(context: OverlayBuildContext): SVGElement = context.run {
        createSvgSwitch(
            child = editMode.state.map { editModeState ->
                if (editModeState is EditPathElevatorMode.MoveStepMode) null
                else null
            },
            tillDetach = tillDetach,
        )
    }
}

class KnotPaintModeNode(
    private val knotPaintMode: KnotPaintMode,
) : HybridNode() {
    override fun buildOverlayElement(context: OverlayBuildContext): SVGElement = context.run {
        val brushElement = knotPaintMode.brushOverMode.mapNested { brushOverModeNow ->
            val brushViewRect = viewTransform.transform(
                rect = brushOverModeNow.brushCursor.map {
                    knotRect(globalKnotCoord = it.knotCoord)
                },
            )

            createSvgRectR(
                svg = svg,
                style = DynamicStyleDeclaration(
                    pointerEvents = constant(PointerEvents.none),
                ),
                rect = brushViewRect,
                fillOpacity = constant(0.0),
                stroke = constant(Color.black),
                strokeWidth = constant(2),
                tillDetach = tillDetach,
            )
        }

        createSvgSwitch(
            child = brushElement,
            tillDetach = tillDetach,
        )
    }
}

class EntitySelectModeNode(
    private val entitySelectMode: EntitySelectMode,
) : HybridNode() {
    override fun buildOverlayElement(context: OverlayBuildContext): SVGElement = context.run {
        val selectionAreaRect = entitySelectMode.selectingMode
            .switchMapNested { selectingModeNow ->
                selectingModeNow.selectionForm.map { it as? EntitySelectMode.AreaSelection }
            }
            .mapNested { areaSelection ->
                createAreaSelectionRectElement(
                    svg = svg,
                    viewTransform = viewTransform,
                    worldArea = constant(areaSelection.worldArea),
                    tillDetach = tillDetach,
                )
            }

        createSvgSwitch(
            child = selectionAreaRect,
            tillDetach = tillDetach,
        )
    }
}
