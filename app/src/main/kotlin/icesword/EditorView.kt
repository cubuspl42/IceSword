package icesword

import html.CSSStyle
import html.FontWeight
import html.createButton
import html.createHtmlElement
import icesword.editor.Editor
import icesword.editor.Tool
import icesword.frp.Till
import icesword.frp.map
import org.w3c.dom.HTMLElement


fun editorView(
    editor: Editor,
    tillDetach: Till,
): HTMLElement {
    val toolBar = editorToolBar(
        editor = editor,
        tillDetach = tillDetach,
    )

    val bottomRow = createHtmlElement("div").apply {
        className = "bottomRow"

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
                tileset = editor.tileset,
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
        className = "editorToolBar"

//        style.minHeight = "32px"
        style.backgroundColor = "grey"
        style.padding = "4px"
    }

    val selectButton = toolButton(
        editor = editor,
        tool = Tool.SELECT,
        tillDetach = tillDetach,
    )

    val moveButton = toolButton(
        editor = editor,
        tool = Tool.MOVE,
        tillDetach = tillDetach,
    )

    val knotBrushButton = toolButton(
        editor = editor,
        tool = Tool.KNOT_BRUSH,
        tillDetach = tillDetach,
    )

    return root.apply {
        appendChild(selectButton)
        appendChild(moveButton)
        appendChild(knotBrushButton)
    }
}