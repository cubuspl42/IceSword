package icesword

import html.createHtmlElement
import html.linkChild
import html.onEvent
import icesword.editor.App
import icesword.frp.Till
import icesword.frp.mapTillNext
import icesword.frp.reactTill
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

    val root = createHtmlElement("div").apply {
        className = "appView"

        style.width = "100%"
        style.height = "100%"

        linkChild(this, theEditorView, till = tillDetach)
    }

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

