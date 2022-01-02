package icesword.ui

import icesword.html.createHTMLElementRaw
import icesword.html.linkChild
import icesword.editor.App
import icesword.frp.Till
import icesword.frp.mapNested
import icesword.frp.mapTillNext
import icesword.frp.reactTill
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
                    rezIndex = editor.rezIndex,
                    textureBank = editor.textureBank,
                    dialogOverlay = dialogOverlay,
                    app = app,
                    editor = editor,
                    tillDetach = tillNext,
                )
            } ?: createHTMLElementRaw("div")
        }

        val editorViewWrapper = createHTMLElementRaw("div").apply {
            className = "editorViewWrapper"

            linkChild(this, theEditorView, till = tillDetach)
        }

        dialogOverlay.linkDialog(
            dialogContent = app.creatingNewProjectProcess.mapNested {
                createCreatingNewProjectDialog()
            }
        )

        dialogOverlay.linkDialog(
            dialogContent = app.loadingWorldProcess.mapNested {
                createLoadingWorldDialog(it)
            }
        )

        setupNewProjectDialogController(
            app = app,
            dialogOverlay = dialogOverlay,
            tillDetach = tillDetach,
        )

        editorViewWrapper
    },
    enableDrop = app.canLoadWorld,
    onFileDropped = { file ->
        app.loadWorld(file = file)
    },
    tillDetach = tillDetach,
)

private fun setupNewProjectDialogController(
    app: App,
    dialogOverlay: DialogOverlay,
    tillDetach: Till,
) {
    app.configureNewProject.reactTill(tillDetach) { newProjectContext ->
        dialogOverlay.showDialog(
            createConfigureNewProjectDialog(
                newProjectContext = newProjectContext,
            ),
        )
    }
}
