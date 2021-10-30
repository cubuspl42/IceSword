package icesword

import icesword.html.DynamicStyleDeclaration
import icesword.html.createHtmlElement
import icesword.html.createStyledHtmlElement
import icesword.html.linkChild
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.mapNotNull
import icesword.frp.reactTillNext
import kotlinx.css.PointerEvents
import org.w3c.dom.HTMLElement

class DialogOverlay(
    private val tillDetach: Till,
) {
    private val _shownDialog = MutCell<HTMLElement?>(null)

    val shownDialog: Cell<HTMLElement?> = _shownDialog

    fun showDialog(
        dialog: HTMLElement,
        tillClose: Till,
    ) {
        if (_shownDialog.sample() != null) {
            throw IllegalStateException()
        }

        _shownDialog.set(dialog)

        tillClose.subscribe {
            _shownDialog.set(null)
        }
    }

    fun linkDialog(dialog: Cell<HTMLElement?>) {
        dialog.reactTillNext(tillAbort = tillDetach) { dialogOrNull, tillNext ->
            dialogOrNull?.let { dialog ->
                showDialog(
                    dialog = dialog,
                    tillClose = tillNext,
                )
            }
        }
    }
}

fun createDialogOverlay(
    tillDetach: Till,
    build: (DialogOverlay) -> HTMLElement,
): HTMLElement {
    val dialogOverlay = DialogOverlay(
        tillDetach = tillDetach,
    )

    fun createOverlay(dialog: HTMLElement) =
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

    val overlay = dialogOverlay.shownDialog.mapNotNull(::createOverlay)

    val dialogOverlayWrapper = createStyledHtmlElement(
        tagName = "div",
        style = DynamicStyleDeclaration(
            pointerEvents = dialogOverlay.shownDialog.map {
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
        child = overlay,
        till = tillDetach,
    )

    val child = build(dialogOverlay)

    val root = createStackLayout(
        children = listOf(
            child,
            dialogOverlayWrapper,
        )
    )

    return root
}
