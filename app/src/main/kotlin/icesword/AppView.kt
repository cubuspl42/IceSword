package icesword

import html.createHtmlElement
import html.linkChild
import html.linkProperty
import html.onEvent
import icesword.editor.App
import icesword.frp.Till
import icesword.frp.hold
import icesword.frp.map
import icesword.frp.mapTillNext
import icesword.frp.mergeWith
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


    val dragoverPreviewWrapper = createHtmlElement("div").apply {
        className = "dragoverPreviewWrapper"

        style.apply {
            display = "grid"
            setProperty("grid-template-columns", "minmax(0, 1fr)")
            setProperty("grid-template-rows", "minmax(0, 1fr)")

            setProperty("pointer-events", "none")
        }
    }

    val root = createStackLayout(
        children = listOf(
            editorViewWrapper,
            dragoverPreviewWrapper,
        )
    )

    val dragOverPreview = createDragoverPreview(
        app = app,
        tillDetach = tillDetach,
    )

    val onDragEnter = root.onEvent<DragEvent>("dragenter")
    val onDragOver = dragOverPreview.onEvent<DragEvent>("dragover")
    val onDragLeave = dragOverPreview.onEvent<DragEvent>("dragleave")
    val onDrop = dragOverPreview.onEvent<DragEvent>("drop")

    onDragOver.reactTill(tillDetach) { it.preventDefault() }

    val isDragOver = onDragEnter.map { true }
        .mergeWith(onDragLeave.map { false })
        .mergeWith(onDrop.map { false })
        .hold(false, tillDetach)

    linkProperty(
        style = dragoverPreviewWrapper.style,
        propertyName = "pointer-events",
        property = isDragOver.map { if (it) null else "none" },
        till = tillDetach,
    )

    val dragoverPreviewCell = isDragOver.map { isDragged ->
        if (isDragged) dragOverPreview else null
    }

    linkChild(
        element = dragoverPreviewWrapper,
        child = dragoverPreviewCell,
        till = tillDetach,
    )

    onDrop.reactTill(tillDetach) { event ->
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

        children.forEachIndexed { index, child ->
            child.style.apply {
                setProperty("grid-column", "1")
                setProperty("grid-row", "1")

                display = "grid"
                setProperty("grid-template-columns", "minmax(0, 1fr)")
                setProperty("grid-template-rows", "minmax(0, 1fr)")

                zIndex = index.toString()
            }

            appendChild(child)
        }
    }

fun createDragoverPreview(
    app: App,
    tillDetach: Till,
): HTMLElement {
    val root = createHtmlElement("div").apply {
        className = "dragoverPreview"

        style.apply {
            display = "flex"
            justifyContent = "center"
            alignItems = "center"

            backgroundColor = "#000000ad"
        }

        appendChild(
            createHtmlElement("p").apply {
                style.apply {
                    color = "white"
                    setProperty("pointer-events", "none")
                }

                appendChild(document.createTextNode("Drop the file to load the world..."))
            }
        )
    }

    return root
}
