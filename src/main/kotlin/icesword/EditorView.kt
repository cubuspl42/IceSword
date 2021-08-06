package icesword

import createHtmlElement
import icesword.editor.Editor
import icesword.frp.Till
import org.w3c.dom.HTMLElement


fun editorView(
    editor: Editor,
    tillDetach: Till,
): HTMLElement {
    val root = createHtmlElement("div").apply {
        style.width = "100%"
        style.height = "100%"
    }

    return root.apply {
        appendChild(
            worldView(
                world = editor.world,
                tileset = editor.tileset,
                tillDetach = tillDetach,
            )
        )
    }
}
