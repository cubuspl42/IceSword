package icesword

import createHtmlElement
import html.CSSStyle
import html.FontWeight
import html.createButton
import icesword.editor.Editor
import icesword.editor.Tool
import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import org.w3c.dom.HTMLElement


fun editorView(
    editor: Editor,
    tillDetach: Till,
): HTMLElement {
    val root = createHtmlElement("div").apply {
        style.width = "100%"
        style.height = "100%"

        style.display = "flex"
        style.flexDirection = "column"
    }

    val toolBarDiv = editorToolBar(
        editor = editor,
        tillDetach = tillDetach,
    )

    return root.apply {
        appendChild(
            toolBarDiv,
        )
        appendChild(
            worldView(
                editor = editor,
                tileset = editor.tileset,
                tillDetach = tillDetach,
            )
        )
    }
}

fun toolButton(
    editor: Editor,
    tool: Tool,
    tillDetach: Till,
): HTMLElement {
    return createButton(
        style = editor.selectedTool.map {
            CSSStyle(
                fontWeight = if (it == tool) FontWeight.bold else null,
            )
        },
        text = tool.name,
        onPressed = {
            editor.selectTool(tool)
        },
        tillDetach = tillDetach,
    )
}

fun editorToolBar(
    editor: Editor,
    tillDetach: Till,
): HTMLElement {
    val root = createHtmlElement("div").apply {
        style.width = "100%"
//        style.minHeight = "32px"
        style.backgroundColor = "grey"
        style.padding = "4px"
    }

    val button1 = toolButton(
        editor = editor,
        tool = Tool.select,
        tillDetach = tillDetach,
    )

    val button2 = toolButton(
        editor = editor,
        tool = Tool.move,
        tillDetach = tillDetach,
    )

    return root.apply {
        appendChild(button1)
        appendChild(button2)
    }
}