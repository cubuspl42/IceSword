package icesword

import icesword.editor.Editor
import icesword.editor.KnotBrush
import icesword.editor.Retail
import icesword.frp.Cell.Companion.constant
import icesword.frp.Till
import icesword.html.DynamicStyleDeclaration
import icesword.html.HTMLWidget
import icesword.html.createGrid
import icesword.html.createHTMLElementRaw
import icesword.html.createHeading4
import icesword.html.resolve
import icesword.ui.retails.retail3.buildInsertionButtons
import kotlinx.css.Align
import kotlinx.css.px
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node


fun editorSideBar(
    editor: Editor,
    tillDetach: Till,
): HTMLElement {
    val root = createHTMLElementRaw("div").apply {
        className = "editorSideBar"

        style.apply {
            display = "flex"
            flexDirection = "column"

            setProperty("gap", "16px")

            width = "140px"
            padding = "8px"
            backgroundColor = "lightgrey"
        }

    }

    return root.apply {
        listOf(
            createInsertSection(
                editor = editor,
                tillDetach = tillDetach,
            ),
            createSection(
                title = "Knot brush",
                children = listOf(
                    createKnotBrushButton(
                        editor = editor,
                        knotBrush = KnotBrush.Additive,
                        tillDetach = tillDetach,
                    ),
                    createKnotBrushButton(
                        editor = editor,
                        knotBrush = KnotBrush.Eraser,
                        tillDetach = tillDetach,
                    ),
                ),
            ),
        ).forEach(::appendChild)
    }
}

private fun createInsertSection(
    editor: Editor,
    tillDetach: Till,
): HTMLElement {
    val children = buildInsertionButtons(
        retail = Retail.theRetail,
        editor = editor,
        tillDetach = tillDetach,
    )

    return createTitledSection(
        title = "Insert...",
        child = createGrid(
            style = DynamicStyleDeclaration(
                justifyItems = constant(Align.center),
            ),
            columnCount = 2,
            gap = 4.px,
            children = children.map { HTMLWidget.of(it) },
        ).build(tillDetach).resolve()
    )
}

private fun createSection(
    title: String,
    alignItems: Align = Align.center,
    children: List<HTMLElement>,
): HTMLElement = createTitledSection(
    title = title,
    child = createHTMLElementRaw("div").apply {
        style.apply {
            display = "flex"
            flexDirection = "column"
            this.alignItems = alignItems.toString()

            setProperty("gap", "4px")
        }

        children.forEach(this::appendChild)
    },
)

private fun createTitledSection(
    title: String,
    child: Node,
): HTMLElement =
    createHTMLElementRaw("div").apply {
        className = "editorSideBarSection"

        style.apply {
            display = "flex"
            flexDirection = "column"

            setProperty("gap", "8px")
        }

        appendChild(
            createHeading4(text = title).apply {
                style.apply {
                    margin = "0"
                    fontFamily = "sans-serif"
                }
            }
        )

        appendChild(child)
    }

