package icesword

import icesword.editor.Editor
import icesword.editor.EditorMode
import icesword.editor.FloorSpikeRow
import icesword.editor.KnotSelectMode
import icesword.editor.EntitySelectMode
import icesword.editor.Tool
import icesword.frp.Cell.Companion.constant
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.TillMarker
import icesword.frp.dynamic_list.mapTillRemoved
import icesword.frp.map
import icesword.html.DynamicStyleDeclaration
import icesword.html.createButton
import icesword.html.createColumn
import icesword.html.createColumnDl
import icesword.html.createHtmlElement
import icesword.html.createNumberInput
import icesword.html.createRow
import icesword.html.createText
import icesword.ui.createSelectButton
import kotlinx.css.Align
import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.px
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node


fun createEditorToolBar(
    editor: Editor,
    dialogOverlay: DialogOverlay,
    tillDetach: Till,
): HTMLElement {
    val selectButton = createModeButton<EntitySelectMode>(
        editor = editor,
        enterMode = { editor.enterSelectMode() },
        tillDetach = tillDetach,
    )

    val knotSelectButton = createModeButton<KnotSelectMode>(
        editor = editor,
        enterMode = { editor.enterKnotSelectMode() },
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
        appendChild(knotSelectButton)
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
    val closeButton = createButton(
        style = DynamicStyleDeclaration(
            alignSelf = constant(Align.flexEnd),
        ),
        text = "✕",
        onPressed = onClosePressed,
        tillDetach = tillDetach,
    )

    fun createTimeInput(
        labelText: String,
        propertyMillis: MutCell<Int>,
    ): HTMLElement = createRow(
        staticStyle = {
            borderStyle = BorderStyle.dashed.toString()
            borderWidth = 1.px.toString()
            padding = 4.px.toString()
        },
        style = DynamicStyleDeclaration(
            alignItems = constant(Align.center),
        ),
        horizontalGap = 4.px,
        children = listOf(
            createText("$labelText:"),
            createNumberInput(
                staticStyle = {
                    width = 64.px.toString()
                },
                initialValue = propertyMillis.sample(),
                onValueChanged = {
                    propertyMillis.set(it)
                },
            ),
            createText("ms"),
        ),
        tillDetach = tillDetach,
    )

    fun createEditSpikeRow(
        config: FloorSpikeRow.FloorSpikeConfig,
        tillDetach: Till,
    ): Node = createRow(
        style = DynamicStyleDeclaration(
            alignItems = constant(Align.center),
        ),
        horizontalGap = 8.px,
        children = listOf(
            createText("Spike"),
            createTimeInput(
                labelText = "Start delay",
                propertyMillis = config.startDelayMillis,
            ),
            createTimeInput(
                labelText = "Time off",
                propertyMillis = config.timeOffMillis,
            ),
            createTimeInput(
                labelText = "Time on",
                propertyMillis = config.timeOnMillis,
            ),
            createButton(
                text = "Remove",
                onPressed = {
                    floorSpikeRow.removeSpike(
                        floorSpikeConfig = config,
                    )
                },
                tillDetach = tillDetach,
            ),
        ),
        tillDetach = tillDetach,
    )

    return createColumn(
        style = DynamicStyleDeclaration(
            backgroundColor = constant(Color("#d1d1d1")),
            padding = constant("16px"),
            fontFamily = constant("sans-serif"),
        ),
        verticalGap = 8.px,
        children = listOf(
            closeButton,
            createColumnDl(
                children = floorSpikeRow.spikeConfigs
                    .mapTillRemoved(tillAbort = tillDetach) { config, tillRemoved ->
                        createEditSpikeRow(
                            config = config,
                            tillDetach = tillRemoved,
                        )
                    },
                verticalGap = 8.px,
                tillDetach = tillDetach,
            ),
            createButton(
                style = DynamicStyleDeclaration(
                    alignSelf = constant(Align.flexStart),
                ),
                text = "Add",
                onPressed = {
                    floorSpikeRow.addSpike()
                },
                tillDetach = tillDetach,
            ),
        ),
        tillDetach = tillDetach,
    )
}
