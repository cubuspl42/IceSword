package icesword

import TextureBank
import html.createHtmlElement
import icesword.editor.Editor
import icesword.frp.Till
import org.w3c.dom.HTMLElement


fun editorView(
    textureBank: TextureBank,
    editor: Editor,
    tillDetach: Till,
): HTMLElement {
    val toolBar = editorToolBar(
        editor = editor,
        tillDetach = tillDetach,
    )

    val bottomRow = createHtmlElement("div").apply {
        className = "editorBottomRow"

        style.apply {
            display = "flex"
            flexDirection = "row"

            minHeight = "0"
        }

        appendChild(
            editorSideBar(
                editor = editor,
                tillDetach = tillDetach,
            )
        )
        appendChild(
            worldView(
                editor = editor,
                textureBank = textureBank,
                tillDetach = tillDetach,
            ).apply {
                style.apply {
                    flex = "1"
                }
            },
        )
    }

    val root = createHtmlElement("div").apply {
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
