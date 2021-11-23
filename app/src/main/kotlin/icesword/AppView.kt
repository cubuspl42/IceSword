package icesword

import icesword.html.createHtmlElement
import icesword.html.linkChild
import icesword.editor.App
import icesword.frp.Till
import icesword.frp.mapNested
import icesword.frp.mapTillNext
import org.w3c.dom.HTMLElement

fun createAppView(
    app: App,
    tillDetach: Till,
): HTMLElement = createDragoverOverlay(
    child = createDialogOverlay(
        tillDetach = tillDetach,
    ) { dialogOverlay ->
        val theEditorView = app.editor.mapTillNext(tillDetach) { editor, tillNext ->
            editor?.let {
                editorView(
                    rezIndex = app.rezIndex,
                    textureBank = app.textureBank,
                    dialogOverlay = dialogOverlay,
                    editor = editor,
                    tillDetach = tillNext,
                )
            } ?: createHtmlElement("div")
        }

        val editorViewWrapper = createHtmlElement("div").apply {
            className = "editorViewWrapper"

            linkChild(this, theEditorView, till = tillDetach)
        }

        val loadingWorldDialog = app.loadingWorldProcess.mapNested {
            createLoadingWorldDialog(it)
        }

        dialogOverlay.linkDialog(loadingWorldDialog)

        editorViewWrapper
    },
    enableDrop = app.canLoadWorld,
    onFileDropped = { file ->
        app.loadWorld(file = file)
    },
    tillDetach = tillDetach,
)
