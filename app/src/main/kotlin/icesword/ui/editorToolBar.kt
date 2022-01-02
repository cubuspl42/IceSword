package icesword.ui

import icesword.editor.App
import icesword.editor.CrateStackSelectionContext
import icesword.editor.CrumblingPegSelectionContext
import icesword.editor.modes.EditPathElevatorMode
import icesword.editor.Editor
import icesword.editor.EditorMode
import icesword.editor.EnemySelectionContext
import icesword.editor.modes.KnotSelectMode
import icesword.editor.modes.EntitySelectMode
import icesword.editor.FloorSpikeRowSelectionContext
import icesword.editor.KnotBrush
import icesword.editor.modes.KnotBrushMode
import icesword.editor.MultipleKnotMeshesSelectionContext
import icesword.editor.SingleKnotMeshSelectionContext
import icesword.editor.entities.PathElevatorPath
import icesword.editor.PathElevatorSelectionContext
import icesword.editor.RopeSelectionContext
import icesword.editor.SelectionContext
import icesword.editor.TogglePegSelectionContext
import icesword.editor.Tool
import icesword.editor.WapObjectSelectionContext
import icesword.editor.WarpSelectionContext
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
import icesword.html.createButtonWb
import icesword.html.createContainer
import icesword.html.createHTMLElementRaw
import icesword.html.createRow
import icesword.html.createRowElement
import icesword.html.createStyledText
import icesword.html.createTextButtonWb
import icesword.html.createTextWb
import icesword.html.resolve
import kotlinx.css.Color
import kotlinx.css.FontWeight
import kotlinx.css.JustifyContent
import kotlinx.css.px
import org.w3c.dom.HTMLElement


fun createEditorToolBar(
    app: App,
    editor: Editor,
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

    val newButton = createButton(
        text = "New",
        onPressed = {
            app.createNewProject()
        },
        tillDetach = tillDetach,
    )

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

    val otherButtonsRow = createRow(
        className = "otherButtonsRow",
        children = listOf(
            HTMLWidget.of(newButton),
            HTMLWidget.of(exportButton),
            HTMLWidget.of(saveButton),
        )
    ).buildElement(tillDetach)

    val selectionModeButtonsRow = editor.selectionContext.mapTillNext(tillDetach) { it, tillNext ->
        createSelectionModeButtonsRow(
            editor = editor,
            selectionContext = it,
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
    selectionContext: SelectionContext?,
    tillDetach: Till,
): HTMLElement? {
    val htmlWidgetB = when (selectionContext) {
        null -> null
        is SingleKnotMeshSelectionContext -> HTMLWidget.of(
            createSingleKnotMeshSelectionModeButtonsRow(
                editor = editor,
                knotMeshSelectionMode = selectionContext,
                tillDetach = tillDetach,
            ),
        )
        is MultipleKnotMeshesSelectionContext ->
            createMultipleKnotMeshesSelectionContextButtonsRow(
                editor = editor,
                selectionContext = selectionContext,
                tillDetach = tillDetach,
            )
        is PathElevatorSelectionContext,
        -> HTMLWidget.of(
            createPathElevatorSelectionModeButtonsRow(
                editor = editor,
                pathElevatorSelectionMode = selectionContext,
                tillDetach = tillDetach,
            ),
        )
        is EnemySelectionContext -> createEnemySelectionModeButtonsRow(
            selectionMode = selectionContext,
        )
        is FloorSpikeRowSelectionContext -> createFloorSpikeRowSelectionModeButtonsRow(
            selectionMode = selectionContext,
        )
        is RopeSelectionContext -> createRopeSelectionModeButtonsRow(
            selectionMode = selectionContext,
        )
        is CrateStackSelectionContext -> createCrateStackSelectionModeButtonsRow(
            selectionMode = selectionContext,
        )
        is WapObjectSelectionContext -> createWapObjectSelectionModeButtonsRow(
            selectionMode = selectionContext,
        )
        is CrumblingPegSelectionContext -> createCrumblingPegSelectionModeButtonsRow(
            selectionMode = selectionContext,
        )
        is TogglePegSelectionContext -> createTogglePegSelectionModeButtonsRow(
            selectionMode = selectionContext,
        )
        is WarpSelectionContext -> createWarpSelectionModeButtonsRow(
            selectionMode = selectionContext,
        )
    }

    return htmlWidgetB?.buildElement(tillDetach = tillDetach)
}

fun createEditorModeButtonsRow(
    editorMode: EditorMode,
    tillDetach: Till,
): HTMLElement? =
    when (editorMode) {
        is KnotBrushMode -> createKnotBrushModeButtonsRow(
            knotBrushMode = editorMode,
        ).buildElement(tillDetach)
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

fun createSingleKnotMeshSelectionModeButtonsRow(
    editor: Editor,
    knotMeshSelectionMode: SingleKnotMeshSelectionContext,
    tillDetach: Till,
): HTMLElement {
    val knotBrushButton = createModeButton<KnotBrushMode>(
        editor = editor,
        name = "Knot brush",
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


fun createMultipleKnotMeshesSelectionContextButtonsRow(
    editor: Editor,
    selectionContext: MultipleKnotMeshesSelectionContext,
    tillDetach: Till,
): HTMLWidgetB<*> {
    val mergeButton = createButton(
        text = "Merge",
        onPressed = {
        },
        tillDetach = tillDetach,
    )

    return createRow(
        children = listOf(
            HTMLWidget.of(mergeButton),
        ),
    )
}


fun createKnotBrushModeButtonsRow(
    knotBrushMode: KnotBrushMode,
): HTMLWidgetB<*> = createRow(
    children = listOf(
        createKnotBrushButton(
            knotBrushMode = knotBrushMode,
            knotBrush = KnotBrush.Additive,
        ),
        createKnotBrushButton(
            knotBrushMode = knotBrushMode,
            knotBrush = KnotBrush.Eraser,
        ),
    ),
)

private fun createKnotBrushButton(
    knotBrushMode: KnotBrushMode,
    knotBrush: KnotBrush,
): HTMLWidgetB<*> = object : HTMLWidgetB<HTMLWidget> {
    override fun build(tillDetach: Till): HTMLWidget = HTMLWidget.of(
        createSelectButton(
            value = knotBrush,
            name = knotBrush.name,
            selected = knotBrushMode.selectedKnotBrush,
            select = knotBrushMode::selectKnotBrush,
            tillDetach = tillDetach,
        ),
    )
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
    pathElevatorSelectionMode: PathElevatorSelectionContext,
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
    selectionMode: EnemySelectionContext,
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
    selectionMode: FloorSpikeRowSelectionContext,
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
    selectionMode: RopeSelectionContext,
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
    selectionMode: CrateStackSelectionContext,
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

fun createCrumblingPegSelectionModeButtonsRow(
    selectionMode: CrumblingPegSelectionContext,
): HTMLWidgetB<*> {
    val crumblingPeg = selectionMode.crumblingPeg

    val canRespawnButton = createButtonWb(
        child = createTextWb(
            crumblingPeg.canRespawn.map { "Can respawn: $it" }
        ),
        onPressed = {
            crumblingPeg.setCanRespawn(!crumblingPeg.canRespawn.sample())
        },
    )

    return createRow(
        children = listOf(
            canRespawnButton,
        ),
    )
}


fun createTogglePegSelectionModeButtonsRow(
    selectionMode: TogglePegSelectionContext,
): HTMLWidgetB<*> {
    val editTimingButton = createTextButtonWb(
        text = "Edit timing",
        onPressed = {
            selectionMode.editTiming()
        },
    )

    return createRow(
        children = listOf(
            editTimingButton,
        ),
    )
}

fun createWapObjectSelectionModeButtonsRow(
    selectionMode: WapObjectSelectionContext,
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

fun createWarpSelectionModeButtonsRow(
    selectionMode: WarpSelectionContext,
): HTMLWidgetB<*> {
    val editTimingButton = createTextButtonWb(
        text = "Edit target",
        onPressed = {
            selectionMode.editTarget()
        },
    )

    return createRow(
        children = listOf(
            editTimingButton,
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
