package icesword

import TextureBank
import icesword.editor.CrateStack
import icesword.editor.CrateStackSelectionMode
import icesword.editor.EditPathElevatorMode
import icesword.editor.Editor
import icesword.editor.EditorMode
import icesword.editor.EnemySelectionMode
import icesword.editor.FloorSpikeRow
import icesword.editor.KnotSelectMode
import icesword.editor.EntitySelectMode
import icesword.editor.FloorSpikeRowSelectionMode
import icesword.editor.KnotMeshSelectionMode
import icesword.editor.PathElevatorPath
import icesword.editor.PathElevatorSelectionMode
import icesword.editor.Rope
import icesword.editor.RopeSelectionMode
import icesword.editor.SelectionMode
import icesword.editor.Tool
import icesword.editor.WapObject
import icesword.editor.WapObjectSelectionMode
import icesword.frp.Cell.Companion.constant
import icesword.frp.Till
import icesword.frp.dynamic_list.size
import icesword.frp.dynamic_ordered_set.DynamicOrderedSet
import icesword.frp.map
import icesword.frp.mapTillNext
import icesword.html.DynamicStyleDeclaration
import icesword.html.HTMLWidget
import icesword.html.HTMLWidgetB
import icesword.html.RowStyleDeclaration
import icesword.html.buildElement
import icesword.html.createButton
import icesword.html.createContainer
import icesword.html.createHTMLElementRaw
import icesword.html.createRow
import icesword.html.createRowElement
import icesword.html.createStyledText
import icesword.html.createTextButtonWb
import icesword.html.resolve
import icesword.ui.createSelectButton
import kotlinx.css.Color
import kotlinx.css.FontWeight
import kotlinx.css.JustifyContent
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
        name = "Select",
        enterMode = { editor.enterSelectMode() },
        tillDetach = tillDetach,
    )

    val moveButton = createToolButton(
        editor = editor,
        name = "Move",
        tool = Tool.MOVE,
        enterMode = { editor.enterMoveMode() },
        tillDetach = tillDetach,
    )

    val toolButtonsRow = createHTMLElementRaw("div").apply {
        className = "toolButtonsRow"

        appendChild(selectButton)
        appendChild(moveButton)
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

    val otherButtonsRow = createHTMLElementRaw("div").apply {
        className = "otherButtonsRow"

        appendChild(exportButton)
        appendChild(saveButton)
    }

    val selectionModeButtonsRow = editor.selectionMode.mapTillNext(tillDetach) { it, tillNext ->
        createSelectionModeButtonsRow(
            editor = editor,
            selectionMode = it,
            tillDetach = tillNext,
        )
    }

    val contextualButtonsRow = editor.editorMode.mapTillNext(tillDetach) { it, tillNext ->
        createEditorModeButtonsRow(
            editorMode = it,
            tillDetach = tillNext,
        )
    }

    val leftButtonRow = createContainer(
        children = DynamicOrderedSet.concatAll(
            DynamicOrderedSet.ofSingle(
                constant(toolButtonsRow),
            ),
            DynamicOrderedSet.ofSingle(
                selectionModeButtonsRow,
            ),
            DynamicOrderedSet.ofSingle(
                contextualButtonsRow,
            ),
        ),
        tillDetach = tillDetach,
    ).apply {
        style.apply {
            display = "flex"
            setProperty("gap", "16px")
        }
    }

    val root = createRow(
        className = "editorToolBar",
        style = DynamicStyleDeclaration(
            padding = constant(4.px),
            backgroundColor = constant(Color.gray),
        ),
        rowStyle = RowStyleDeclaration(
            justifyContentHorizontally = constant(JustifyContent.spaceBetween),
        ),
        children = listOf(
            HTMLWidget.of(leftButtonRow),
            HTMLWidget.of(otherButtonsRow),
        ),
    ).build(tillDetach).resolve() as HTMLElement

    return root
}


fun createSelectionModeButtonsRow(
    editor: Editor,
    selectionMode: SelectionMode?,
    tillDetach: Till,
): HTMLElement? = when (selectionMode) {
    is KnotMeshSelectionMode -> createKnotMeshSelectionModeButtonsRow(
        editor = editor,
        knotMeshSelectionMode = selectionMode,
        tillDetach = tillDetach,
    )
    is PathElevatorSelectionMode -> createPathElevatorSelectionModeButtonsRow(
        editor = editor,
        pathElevatorSelectionMode = selectionMode,
        tillDetach = tillDetach,
    )
    is EnemySelectionMode -> createEnemySelectionModeButtonsRow(
        selectionMode = selectionMode,
    ).buildElement(tillDetach = tillDetach)
    is FloorSpikeRowSelectionMode -> createFloorSpikeRowSelectionModeButtonsRow(
        selectionMode = selectionMode,
    ).buildElement(tillDetach = tillDetach)
    is RopeSelectionMode -> createRopeSelectionModeButtonsRow(
        selectionMode = selectionMode,
    ).buildElement(tillDetach = tillDetach)
    is CrateStackSelectionMode -> createCrateStackSelectionModeButtonsRow(
        selectionMode = selectionMode,
    ).buildElement(tillDetach = tillDetach)
    is WapObjectSelectionMode -> createWapObjectSelectionModeButtonsRow(
        selectionMode = selectionMode,
    ).buildElement(tillDetach = tillDetach)
    null -> null
}

fun createEditorModeButtonsRow(
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

fun createKnotMeshSelectionModeButtonsRow(
    editor: Editor,
    knotMeshSelectionMode: KnotMeshSelectionMode,
    tillDetach: Till,
): HTMLElement {
    val knotBrushButton = createToolButton(
        editor = editor,
        name = "Knot brush",
        tool = Tool.KNOT_BRUSH,
        enterMode = { knotMeshSelectionMode.enterKnotBrushMode() },
        tillDetach = tillDetach,
    )

    val knotSelectButton = createModeButton<KnotSelectMode>(
        editor = editor,
        name = "Select knots",
        enterMode = { knotMeshSelectionMode.enterKnotSelectMode() },
        tillDetach = tillDetach,
    )


    return createRow(
        children = listOf(
            HTMLWidget.of(knotBrushButton),
            HTMLWidget.of(knotSelectButton),
        ),
    ).buildElement(tillDetach = tillDetach)
}

fun createKnotSelectModeButtonsRow(
    knotSelectMode: KnotSelectMode,
    tillDetach: Till,
): HTMLElement = createRowElement(
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

fun createPathElevatorSelectionModeButtonsRow(
    editor: Editor,
    pathElevatorSelectionMode: PathElevatorSelectionMode,
    tillDetach: Till,
): HTMLElement {
    val editPathElevatorButton = createModeButton<EditPathElevatorMode>(
        editor = editor,
        name = "Edit path",
        enterMode = { pathElevatorSelectionMode.enterEditPathElevatorMode() },
        tillDetach = tillDetach,
    )

    return createRow(
        children = listOf(
            HTMLWidget.of(editPathElevatorButton),
        ),
    ).buildElement(tillDetach = tillDetach)
}

fun createEditPathElevatorModeButtonsRow(
    editMode: EditPathElevatorMode,
    tillDetach: Till,
): HTMLElement {
    val path = editMode.pathElevator.path

    return createRowElement(
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


fun createEnemySelectionModeButtonsRow(
    selectionMode: EnemySelectionMode,
): HTMLWidgetB<*> {
    val editTreasuresButton = createTextButtonWb(
        text = "Edit pickups",
        onPressed = {
            selectionMode.editPickups()
        },
    )

    return createRow(
        children = listOf(
            editTreasuresButton,
        ),
    )
}

fun createFloorSpikeRowSelectionModeButtonsRow(
    selectionMode: FloorSpikeRowSelectionMode,
): HTMLWidgetB<*> {
    val editTreasuresButton = createTextButtonWb(
        text = "Edit spikes",
        onPressed = {
            selectionMode.editSpikes()
        },
    )

    return createRow(
        children = listOf(
            editTreasuresButton,
        ),
    )
}

fun createRopeSelectionModeButtonsRow(
    selectionMode: RopeSelectionMode,
): HTMLWidgetB<*> {
    val editTreasuresButton = createTextButtonWb(
        text = "Edit speed",
        onPressed = {
            selectionMode.editSpeed()
        },
    )

    return createRow(
        children = listOf(
            editTreasuresButton,
        ),
    )
}

fun createCrateStackSelectionModeButtonsRow(
    selectionMode: CrateStackSelectionMode,
): HTMLWidgetB<*> {
    val editTreasuresButton = createTextButtonWb(
        text = "Edit pickups",
        onPressed = {
            selectionMode.editPickups()
        },
    )

    return createRow(
        children = listOf(
            editTreasuresButton,
        ),
    )
}

fun createWapObjectSelectionModeButtonsRow(
    selectionMode: WapObjectSelectionMode,
): HTMLWidgetB<*> {
    val editTreasuresButton = createTextButtonWb(
        text = "Edit properties",
        onPressed = {
            selectionMode.editProperties()
        },
    )

    return createRow(
        children = listOf(
            editTreasuresButton,
        ),
    )
}

private inline fun <reified Mode : EditorMode> createModeButton(
    editor: Editor,
    name: String,
    crossinline enterMode: () -> Unit,
    tillDetach: Till,
): HTMLElement =
    createSelectButton(
        value = true, // TODO: Improve this
        name = name,
        selected = editor.editorMode.map { it is Mode },
        select = { enterMode() },
        tillDetach = tillDetach,
    )

private fun createToolButton(
    editor: Editor,
    name: String,
    tool: Tool,
    enterMode: () -> Unit,
    tillDetach: Till,
): HTMLElement =
    createSelectButton(
        value = tool,
        name = name,
        selected = editor.selectedTool,
        select = { enterMode() },
        tillDetach = tillDetach,
    )
