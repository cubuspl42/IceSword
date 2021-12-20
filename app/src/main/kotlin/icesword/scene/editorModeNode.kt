package icesword.scene

import icesword.editor.EditPathElevatorMode
import icesword.editor.EditorMode
import icesword.frp.map
import icesword.html.createSvgSwitch
import org.w3c.dom.svg.SVGElement

fun createEditorModeModeNode(
    editorMode: EditorMode,
): HybridNode? = when (editorMode) {
    is EditPathElevatorMode -> EditPathElevatorModeNode(editorMode)
    else -> null
}

class EditPathElevatorModeNode(
    private val editMode: EditPathElevatorMode,
) : HybridNode() {
    override fun buildOverlayElement(context: HybridNode.OverlayBuildContext): SVGElement = context.run {
        createSvgSwitch(
            child = editMode.state.map { editModeState ->
                if (editModeState is EditPathElevatorMode.MoveStepMode)
                    null
//                    createSvgCircle(
//                        svg = svg,
//                        radius = 32.0f,
//                        translate = viewTransform.transform(editModeState.targetPosition),
//                        stroke = Cell.constant("blue"),
//                        tillDetach = tillDetach,
//                    )
                else null
            },
            tillDetach = tillDetach,
        )
    }
}
