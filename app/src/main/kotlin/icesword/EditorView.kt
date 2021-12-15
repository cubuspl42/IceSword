package icesword

import TextureBank
import icesword.html.createHTMLElementRaw
import icesword.editor.Editor
import icesword.editor.Retail
import icesword.frp.Till
import icesword.ui.retails.RetailUiPrototype
import org.w3c.dom.HTMLElement


fun editorView(
    rezIndex: RezIndex,
    textureBank: TextureBank,
    dialogOverlay: DialogOverlay,
    editor: Editor,
    tillDetach: Till,
): HTMLElement {
    val retailUiPrototype =
        RetailUiPrototype.forRetail(editor.retail)

    val toolBar = createEditorToolBar(
        rezIndex = rezIndex,
        textureBank = textureBank,
        editor = editor,
        dialogOverlay = dialogOverlay,
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

    return root
}
