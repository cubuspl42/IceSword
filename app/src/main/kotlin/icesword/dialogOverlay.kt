package icesword

import icesword.html.DynamicStyleDeclaration
import icesword.html.createHtmlElement
import icesword.html.createStyledHtmlElement
import icesword.html.linkChild
import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.mapNotNull
import kotlinx.css.PointerEvents
import org.w3c.dom.HTMLElement

fun createDialogOverlay(
    child: HTMLElement,
    dialog: Cell<HTMLElement?>,
    tillDetach: Till,
): HTMLElement {
    fun createDialogOverlay(dialog: HTMLElement) =
        createHtmlElement("div").apply {
            className = "dialogOverlay"

            style.apply {
                display = "grid"
                setProperty("grid-template-columns", "minmax(0, 1fr)")
                setProperty("grid-template-rows", "minmax(0, 1fr)")
                setProperty("place-items", "center")

                backgroundColor = "#12121280"
            }

            appendChild(dialog)
        }

    val dialogOverlay = dialog.mapNotNull(::createDialogOverlay)

    val dialogOverlayWrapper = createStyledHtmlElement(
        tagName = "div",
        style = DynamicStyleDeclaration(
            pointerEvents = dialog.map {
                if (it == null) PointerEvents.none else null
            },
        ),
        tillDetach = tillDetach,
    ).apply {
        className = "dialogOverlayWrapper"

        style.apply {
            display = "grid"
            setProperty("place-items", "stretch")
        }
    }

    linkChild(
        element = dialogOverlayWrapper,
        child = dialogOverlay,
        till = tillDetach,
    )

    val root = createStackLayout(
        children = listOf(
            child,
            dialogOverlayWrapper,
        )
    )

    return root
}
