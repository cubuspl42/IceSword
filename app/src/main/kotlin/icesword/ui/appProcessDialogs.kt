package icesword.ui

import icesword.html.createHTMLElementRaw
import icesword.editor.LoadingWorldProcess
import icesword.html.FontFamily
import icesword.html.HTMLWidget
import icesword.html.HTMLWidgetB
import kotlinx.browser.document

fun createCreatingNewProjectDialog(): HTMLWidgetB<*> =
    HTMLWidget.of(
        createHTMLElementRaw("div").apply {
            className = "creatingNewProjectDialog"

            style.apply {
                backgroundColor = "#d1d1d1"
                padding = "16px"
                fontFamily = FontFamily.sansSerif
            }

            appendChild(
                document.createTextNode(
                    "Creating new project...",
                )
            )
        }
    )

fun createLoadingWorldDialog(
    loadingWorldProcess: LoadingWorldProcess,
): HTMLWidgetB<*> = HTMLWidget.of(
    createHTMLElementRaw("div").apply {
        className = "loadingWorldDialog"

        style.apply {
            backgroundColor = "#d1d1d1"
            padding = "16px"
            fontFamily = FontFamily.sansSerif
        }

        appendChild(
            document.createTextNode(
                "Loading world ${loadingWorldProcess.worldFilename}...",
            )
        )
    }
)
