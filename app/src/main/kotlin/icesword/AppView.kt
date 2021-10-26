package icesword

import icesword.html.createHtmlElement
import icesword.html.linkChild
import icesword.editor.App
import icesword.frp.Till
import icesword.frp.mapNotNull
import icesword.frp.mapTillNext
import org.w3c.dom.HTMLElement

fun createAppView(
    app: App,
    tillDetach: Till,
): HTMLElement {
    val theEditorView = app.editor.mapTillNext(tillDetach) { editor, tillNext ->
        editor?.let {
            editorView(
                textureBank = app.textureBank,
                editor = editor,
                tillDetach = tillNext,
            )
        } ?: createHtmlElement("div")
    }

    val editorViewWrapper = createHtmlElement("div").apply {
        className = "editorViewWrapper"

        linkChild(this, theEditorView, till = tillDetach)
    }

    val loadingWorldDialog = app.loadingWorldProcess.mapNotNull {
        createLoadingWorldDialog(it)
    }

    return createDragoverOverlay(
        child = createDialogOverlay(
            child = editorViewWrapper,
            dialog = loadingWorldDialog,
            tillDetach = tillDetach,
        ),
        enableDrop = app.canLoadWorld,
        onFileDropped = { file ->
            app.loadWorld(file = file)
        },
        tillDetach = tillDetach,
    )
}
