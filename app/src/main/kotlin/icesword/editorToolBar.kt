package icesword

import icesword.html.createButton
import icesword.html.createHtmlElement
import icesword.editor.Editor
import icesword.editor.EditorMode
import icesword.editor.FloorSpikeRow
import icesword.editor.SelectMode
import icesword.editor.Tool
import icesword.frp.Till
import icesword.frp.TillMarker
import icesword.frp.map
import icesword.html.CSSStyle
import icesword.html.FontWeight
import icesword.ui.createSelectButton
import kotlinx.browser.document
import org.w3c.dom.HTMLElement


fun createEditorToolBar(
    editor: Editor,
    dialogOverlay: DialogOverlay,
    tillDetach: Till,
): HTMLElement {
    val selectButton = createModeButton<SelectMode>(
        editor = editor,
        enterMode = { editor.enterSelectMode() },
        tillDetach = tillDetach,
    )

    val moveButton = createToolButton(
        editor = editor,
        tool = Tool.MOVE,
        tillDetach = tillDetach,
    )

    val knotBrushButton = createToolButton(
        editor = editor,
        tool = Tool.KNOT_BRUSH,
        tillDetach = tillDetach,
    )

    val toolButtonsRow = createHtmlElement("div").apply {
        className = "toolButtonsRow"

        appendChild(selectButton)
        appendChild(moveButton)
        appendChild(knotBrushButton)
    }

    val editButton = createButton(
        text = "Edit",
        onPressed = {
            onEditPressed(
                editor = editor,
                dialogOverlay = dialogOverlay,
            )
        },
        tillDetach = tillDetach,
    )

    val editButtonsRow = createHtmlElement("div").apply {
        className = "editButtonsRow"

        appendChild(editButton)
    }

    val exportButton = createButton(
        text = "Export",
        onPressed = {
            editor.exportWorld()
        },
        tillDetach = tillDetach,
    )

    val saveButton = createButton(
        text = "Save",
        onPressed = {
            editor.saveProject()
        },
        tillDetach = tillDetach,
    )

    val otherButtonsRow = createHtmlElement("div").apply {
        className = "otherButtonsRow"

        appendChild(exportButton)
        appendChild(saveButton)
    }

    val root = createHtmlElement("div").apply {
        className = "editorToolBar"

        style.apply {
            display = "flex"
            setProperty("gap", "16px")

            backgroundColor = "grey"
            padding = "4px"
        }

        appendChild(toolButtonsRow)
        appendChild(editButtonsRow)
        appendChild(otherButtonsRow)
    }

    return root
}

private inline fun <reified Mode : EditorMode> createModeButton(
    editor: Editor,
    crossinline enterMode: () -> Unit,
    tillDetach: Till,
): HTMLElement =
    createSelectButton(
        value = true, // TODO: Improve this
        name = Mode::class.simpleName ?: "???",
        selected = editor.editorMode.map { it is Mode },
        select = { enterMode() },
        tillDetach = tillDetach,
    )

private fun createToolButton(
    editor: Editor,
    tool: Tool,
    tillDetach: Till,
): HTMLElement =
    createSelectButton(
        value = tool,
        name = tool.name,
        selected = editor.selectedTool,
        select = editor::selectTool,
        tillDetach = tillDetach,
    )

private fun onEditPressed(
    editor: Editor,
    dialogOverlay: DialogOverlay,
) {
    editor.selectedEntity.sample()?.let { selectedEntity ->
        if (selectedEntity is FloorSpikeRow) {
            val closeMarker = TillMarker()

            dialogOverlay.showDialog(
                dialog = createEditFloorSpikeRowDialog(
                    floorSpikeRow = selectedEntity,
                    onClosePressed = closeMarker::markReached,
                    tillDetach = Till.never, // FIXME?
                ),
                tillClose = closeMarker,
            )
        }
    }
}

fun createEditFloorSpikeRowDialog(
    floorSpikeRow: FloorSpikeRow,
    onClosePressed: () -> Unit,
    tillDetach: Till,
): HTMLElement {
    val spikes = floorSpikeRow.spikes.sample()

    return createHtmlElement("div").apply {
        className = "editFloorSpikeRowDialog"

        style.apply {
            backgroundColor = "#d1d1d1"
            padding = "16px"
            fontFamily = "sans-serif"
        }

        appendChild(
            createButton(
                text = "✕",
                onPressed = onClosePressed,
                tillDetach = tillDetach,
            )
        )

        appendChild(
            document.createTextNode(
                "Spike count: ${spikes.size}",
            )
        )
    }
}
