package icesword.scene

import icesword.editor.EditPathElevatorMode
import icesword.editor.EditorMode
import icesword.editor.KnotPaintMode
import icesword.editor.knotRect
import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.map
import icesword.frp.mapNested
import icesword.html.createSvgRectR
import icesword.html.createSvgSwitch
import kotlinx.css.Color
import kotlinx.css.br
import org.w3c.dom.svg.SVGElement

fun createEditorModeModeNode(
    editorMode: EditorMode,
): HybridNode? = when (editorMode) {
    is EditPathElevatorMode -> EditPathElevatorModeNode(editorMode)
    is KnotPaintMode -> KnotPaintModeNode(editorMode)
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
        val brushElement = knotPaintMode.brushCursor.mapNested { brushCursor ->
            val brushViewRect = viewTransform.transform.sample().transform(
                rect = knotRect(globalKnotCoord = brushCursor.knotCoord),
            )

            createSvgRectR(
                svg = svg,
                rect = constant(brushViewRect),
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
