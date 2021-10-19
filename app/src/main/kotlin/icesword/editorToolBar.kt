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


fun editorToolBar(
    editor: Editor,
    tillDetach: Till,
): HTMLElement {
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

    val toolButtonsRow = createHtmlElement("div").apply {
        className = "toolButtonsRow"

        appendChild(selectButton)
        appendChild(moveButton)
        appendChild(knotBrushButton)
    }

    val exportButton = createButton(
        text = "Export",
        onPressed = {
            editor.exportWorld()
        },
        tillDetach = tillDetach,
    )

    val otherButtonsRow = createHtmlElement("div").apply {
        className = "otherButtonsRow"

        appendChild(exportButton)
    }

    val root = createHtmlElement("div").apply {
        className = "editorToolBar"

        style.apply {
            display = "flex"
            setProperty("gap", "8px")

            backgroundColor = "grey"
            padding = "4px"
        }

        appendChild(toolButtonsRow)
        appendChild(otherButtonsRow)
    }

    return root
}

private fun toolButton(
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
