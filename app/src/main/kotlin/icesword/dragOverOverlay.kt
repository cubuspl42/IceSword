package icesword

import html.createHtmlElement
import html.linkChild
import html.linkProperty
import html.onEvent
import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.hold
import icesword.frp.map
import icesword.frp.mapTo
import icesword.frp.mergeWith
import icesword.frp.reactTill
import kotlinx.browser.document
import org.w3c.dom.DragEvent
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.files.File

fun createDragoverOverlay(
    child: HTMLElement,
    enableDrop: Cell<Boolean>,
    onFileDragged: (file: File) -> Unit,
    tillDetach: Till,
): HTMLElement {
    val dragOverPreview = createDragoverPreview()

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
            child,
            dragoverPreviewWrapper,
        )
    )

    val onDragEnter = root.onEvent<DragEvent>("dragenter")
    val onDragOver = dragOverPreview.onEvent<DragEvent>("dragover")
    val onDragLeave = dragOverPreview.onEvent<DragEvent>("dragleave")
    val onDrop = dragOverPreview.onEvent<DragEvent>("drop")

    val isDraggedOver = onDragEnter.mapTo(true)
        .mergeWith(onDragLeave.mergeWith(onDrop).mapTo(false))
        .hold(false, tillDetach)

    linkProperty(
        style = dragoverPreviewWrapper.style,
        propertyName = "pointer-events",
        property = isDraggedOver.map { if (it) null else "none" },
        till = tillDetach,
    )

    val dragoverPreviewCell = isDraggedOver.map { isDragged ->
        if (isDragged) dragOverPreview else null
    }

    linkChild(
        element = dragoverPreviewWrapper,
        child = dragoverPreviewCell,
        till = tillDetach,
    )

    onDragOver.reactTill(tillDetach) {
        it.preventDefault()
    }

    onDrop.reactTill(tillDetach) { event ->
        event.preventDefault()

        if (enableDrop.sample()) {
            event.dataTransfer?.items?.get(0)?.getAsFile()?.let { file ->
                onFileDragged(file)
            }
        }
    }

    return root
}

private fun createDragoverPreview(): HTMLElement {
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
                    fontFamily = "sans-serif"
                    color = "white"
                    setProperty("pointer-events", "none")
                }

                appendChild(document.createTextNode("Drop the file to load the world..."))
            }
        )
    }

    return root
}
