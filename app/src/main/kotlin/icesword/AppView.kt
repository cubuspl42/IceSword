package icesword

import html.createHtmlElement
import html.linkChild
import html.onEvent
import icesword.editor.App
import icesword.frp.Till
import icesword.frp.mapTillNext
import icesword.frp.reactTill
import kotlinx.browser.document
import org.w3c.dom.DragEvent
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

fun createAppView(
    app: App,
    tillDetach: Till,
): HTMLElement {
    val theEditorView = app.editor.mapTillNext(tillDetach) { editor, tillNext ->
        editor?.let {
            editorView(
                editor = editor,
                tillDetach = tillNext,
            )
        } ?: createHtmlElement("div")
    }

    val editorViewWrapper = createHtmlElement("div").apply {
        className = "editorViewWrapper"

        linkChild(this, theEditorView, till = tillDetach)
    }

    val dragoverPreview = createDragoverPreview(
        app = app,
        tillDetach = tillDetach,
    )

//    val root = createHtmlElement("div").apply {
//        className = "appView"
//
//        style.width = "100%"
//        style.height = "100%"
//
//        linkChild(this, theEditorView, till = tillDetach)
//    }

    val root = createStackLayout(
        children = listOf(
            editorViewWrapper,
//            dragoverPreview,
        )
    )

    root.onEvent<DragEvent>("dragover").reactTill(tillDetach) { event ->
        event.preventDefault()
    }

    root.onEvent<DragEvent>("drop").reactTill(tillDetach) { event ->
        event.preventDefault()

        event.dataTransfer?.items?.get(0)?.getAsFile()?.let { file ->
            app.loadWorld(file = file)
        }
    }

    return root
}

fun createStackLayout(children: List<HTMLElement>): HTMLElement =
    createHtmlElement("div").apply {
        className = "stack"

        style.apply {
            display = "grid"

            setProperty("grid-template-columns", "minmax(0, 1fr)")
            setProperty("grid-template-rows", "minmax(0, 1fr)")
        }

        children.mapIndexed { index, child ->
            createHtmlElement("div").apply {
                style.apply {
                    setProperty("grid-column", "1")
                    setProperty("grid-row", "1")

                    display = "flex"

                    width = "100%"
                    height = "100%"

                    zIndex = index.toString()
                }

                appendChild(child.apply {
                    style.apply {
                        flex = "1"
                    }
                })
            }
        }.forEach(this::appendChild)
    }

fun createDragoverPreview(
    app: App,
    tillDetach: Till,
): HTMLElement {
    val root = createHtmlElement("div").apply {
        className = "dragoverPreview"

//        style.width = "100%"
//        style.height = "100%"

        style.apply {
            display = "flex"
            justifyContent = "center"
            alignItems = "center"
        }

        appendChild(
            createHtmlElement("p").apply {
                appendChild(document.createTextNode("Drop the file to load the world..."))
            }
        )
    }

    return root
}

