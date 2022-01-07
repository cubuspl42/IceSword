package icesword.ui

import icesword.RezIndex
import icesword.RezTextureBank
import icesword.editor.App
import icesword.html.createHTMLElementRaw
import icesword.editor.Editor
import icesword.frp.Till
import icesword.frp.reactTill
import icesword.ui.retails.RetailUiPrototype
import org.w3c.dom.HTMLElement


fun editorView(
    rezIndex: RezIndex,
    textureBank: RezTextureBank,
    dialogOverlay: DialogOverlay,
    app: App,
    editor: Editor,
    tillDetach: Till,
): HTMLElement {
    val retailUiPrototype =
        RetailUiPrototype.forRetail(editor.retail)

    val toolBar = createEditorToolBar(
        app = app,
        editor = editor,
        tillDetach = tillDetach,
    )

    val bottomRow = createHTMLElementRaw("div").apply {
        className = "editorBottomRow"

        style.apply {
            display = "flex"
            flexDirection = "row"

            minHeight = "0"
        }

        appendChild(
            editorSideBar(
                editor = editor,
                retailUiPrototype = retailUiPrototype,
                tillDetach = tillDetach,
            )
        )
        appendChild(
            worldView(
                rezIndex = rezIndex,
                textureBank = textureBank,
                dialogOverlay = dialogOverlay,
                editor = editor,
                tillDetach = tillDetach,
            ).apply {
                style.apply {
                    flex = "1"
                }
            },
        )
    }

    val root = createHTMLElementRaw("div").apply {
        className = "editorView"

        style.display = "flex"
        style.flexDirection = "column"

        appendChild(toolBar)
        appendChild(
            bottomRow.apply {
                style.apply {
                    flex = "1"
                }
            },
        )
    }

    setupEditDialogController(
        editor = editor,
        textureBank = textureBank,
        rezIndex = rezIndex,
        dialogOverlay = dialogOverlay,
        tillDetach = tillDetach,
    )

    return root
}

private fun setupEditDialogController(
    rezIndex: RezIndex,
    textureBank: RezTextureBank,
    editor: Editor,
    dialogOverlay: DialogOverlay,
    tillDetach: Till,
) {
    editor.editEnemyPickups.reactTill(tillDetach) { enemy ->
        dialogOverlay.showDialog(
            dialog = createEditEnemyDialog(
                rezIndex = rezIndex,
                enemy = enemy,
            ),
        )
    }

    editor.editFloorSpikeRowSpikes.reactTill(tillDetach) { floorSpikeRow ->
        dialogOverlay.showDialog(
            dialog = createEditFloorSpikeRowDialogWb(
                floorSpikeRow = floorSpikeRow,
            ),
        )
    }

    editor.editRopeSpeed.reactTill(tillDetach) { rope ->
        dialogOverlay.showDialog(
            dialog = createEditRopeDialog(
                rope = rope,
            ),
        )
    }

    editor.editCrateStackPickups.reactTill(tillDetach) { crateStack ->
        dialogOverlay.showDialog(
            dialog = createEditCrateStackDialog(
                rezIndex = rezIndex,
                textureBank = textureBank,
                crateStack = crateStack,
            ),
        )
    }

    editor.editWapObjectProperties.reactTill(tillDetach) { wapObject ->
        dialogOverlay.showDialog(
            dialog = createWapObjectDialog(
                wapObject = wapObject,
            )
        )
    }

    editor.editTogglePegTiming.reactTill(tillDetach) { togglePeg ->
        dialogOverlay.showDialog(
            dialog = createTogglePegDialog(
                togglePeg = togglePeg,
            )
        )
    }

    editor.editWarpTarget.reactTill(tillDetach) { warp ->
        dialogOverlay.showDialog(
            dialog = createWarpDialog(
                warp = warp,
            )
        )
    }
}
