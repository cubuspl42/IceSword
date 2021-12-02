package icesword

import icesword.html.createHTMLElementRaw
import icesword.editor.LoadingWorldProcess
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

fun createLoadingWorldDialog(
    loadingWorldProcess: LoadingWorldProcess
): HTMLElement =
    createHTMLElementRaw("div").apply {
        className = "loadingWorldDialog"

        style.apply {
            backgroundColor = "#d1d1d1"
            padding = "16px"
            fontFamily = "sans-serif"
        }

        appendChild(
            document.createTextNode(
                "Loading world ${loadingWorldProcess.worldFilename}...",
            )
        )
    }
