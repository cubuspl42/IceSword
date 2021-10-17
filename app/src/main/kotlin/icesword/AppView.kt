package icesword

import html.createHtmlElement
import html.linkChild
import icesword.editor.App
import icesword.frp.Till
import icesword.frp.mapTillNext
import org.w3c.dom.HTMLElement

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

    return createDragoverOverlay(
        child = editorViewWrapper,
        onFileDragged = { file ->
            app.loadWorld(file)
        },
        tillDetach = tillDetach,
    )
}
