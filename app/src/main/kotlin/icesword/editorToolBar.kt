package icesword

import TextureBank
import icesword.editor.EditPathElevatorMode
import icesword.editor.Editor
import icesword.editor.EditorMode
import icesword.editor.Enemy
import icesword.editor.FloorSpikeRow
import icesword.editor.KnotSelectMode
import icesword.editor.EntitySelectMode
import icesword.editor.PathElevatorPath
import icesword.editor.Tool
import icesword.frp.Till
import icesword.frp.dynamic_list.size
import icesword.frp.dynamic_ordered_set.DynamicOrderedSet
import icesword.frp.map
import icesword.frp.mapTillNext
import icesword.html.DynamicStyleDeclaration
import icesword.html.createButton
import icesword.html.createContainer
import icesword.html.createHtmlElement
import icesword.html.createRow
import icesword.html.createStyledText
import icesword.ui.createSelectButton
import kotlinx.css.FontWeight
import kotlinx.css.px
import org.w3c.dom.HTMLElement


fun createEditorToolBar(
    rezIndex: RezIndex,
    textureBank: TextureBank,
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

    val editPathElevatorButton = createModeButton<EditPathElevatorMode>(
        editor = editor,
        enterMode = { editor.enterEditPathElevatorMode() },
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
        appendChild(editPathElevatorButton)
        appendChild(moveButton)
        appendChild(knotBrushButton)
    }

    val editButton = createButton(
        text = "Edit",
        onPressed = {
            onEditPressed(
                rezIndex = rezIndex,
                textureBank = textureBank,
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


    val fixedButtonsRows = DynamicOrderedSet.of(listOf(
        toolButtonsRow,
        editButtonsRow,
        otherButtonsRow,
    ))

    val contextualButtonsRow = editor.editorMode.mapTillNext(tillDetach) { it, tillNext ->
        createContextualButtonsRow(
            editorMode = it,
            tillDetach = tillNext,
        )
    }

    val root = createContainer(
        children = DynamicOrderedSet.concat(
            fixedButtonsRows,
            DynamicOrderedSet.ofSingle(
                contextualButtonsRow,
            ),
        ),
        tillDetach = tillDetach,
    ).apply {
        className = "editorToolBar"

        style.apply {
            display = "flex"
            setProperty("gap", "16px")

            backgroundColor = "grey"
            padding = "4px"
        }
    }

    return root
}

fun createContextualButtonsRow(
    editorMode: EditorMode,
    tillDetach: Till,
): HTMLElement? =
    when (editorMode) {
        is KnotSelectMode -> createKnotSelectModeButtonsRow(
            knotSelectMode = editorMode,
            tillDetach = tillDetach,
        )
        is EditPathElevatorMode -> createEditPathElevatorModeButtonsRow(
            editMode = editorMode,
            tillDetach = tillDetach,
        )
        else -> null
    }

fun createKnotSelectModeButtonsRow(
    knotSelectMode: KnotSelectMode,
    tillDetach: Till,
): HTMLElement = createRow(
    children = listOf(
        createButton(
            text = "Remove knots",
            onPressed = {
                knotSelectMode.removeSelectedKnots()
            },
            tillDetach = tillDetach,
        ),
        createButton(
            text = "Extract",
            onPressed = {
                knotSelectMode.extractKnotMesh()
            },
            tillDetach = tillDetach,
        ),
    ),
    tillDetach = tillDetach,
)

fun createEditPathElevatorModeButtonsRow(
    editMode: EditPathElevatorMode,
    tillDetach: Till,
): HTMLElement {
    val path = editMode.pathElevator.path

    return createRow(
        children = listOf(
            createButton(
                text = "Remove step",
                onPressed = {
                    val idleMode = editMode.state.sample() as? EditPathElevatorMode.IdleMode
                    idleMode?.removeSelectedStep()
                },
                tillDetach = tillDetach,
            ),
            createButton(
                text = "Insert step",
                onPressed = {
                    val idleMode = editMode.state.sample() as? EditPathElevatorMode.IdleMode
                    idleMode?.insertStep()
                },
                tillDetach = tillDetach,
            ),
            createStyledText(
                text = path.steps.size.map { stepCount ->
                    "$stepCount/${PathElevatorPath.stepCountLimit} steps"
                },
                style = DynamicStyleDeclaration(
                    fontWeight = path.steps.size.map { stepCount ->
                        if (stepCount > PathElevatorPath.stepCountLimit) FontWeight.bold
                        else FontWeight.initial
                    },
                ),
                tillDetach = tillDetach,
            ),
        ),
        horizontalGap = 4.px,
        tillDetach = tillDetach,
    )
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
    rezIndex: RezIndex,
    textureBank: TextureBank,
    editor: Editor,
    dialogOverlay: DialogOverlay,
) {
    editor.selectedEntity.sample()?.let { selectedEntity ->
        when (selectedEntity) {
            is Enemy -> {
                dialogOverlay.showDialog(
                    dialog = createEditEnemyDialog(
                        rezIndex = rezIndex,
                        textureBank = textureBank,
                        enemy = selectedEntity,
                    ),
                )
            }
            is FloorSpikeRow -> {
                dialogOverlay.showDialog(
                    dialog = createEditFloorSpikeRowDialogWb(
                        floorSpikeRow = selectedEntity,
                    ),
                )
            }
            else -> {}
        }
    }
}
