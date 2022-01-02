package icesword.ui

import icesword.html.DynamicStyleDeclaration
import icesword.html.createHTMLElementRaw
import icesword.html.createStyledHtmlElement
import icesword.html.linkChild
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.mapNested
import icesword.frp.mapTillNext
import icesword.frp.reactTill
import icesword.html.HTMLWidget
import icesword.html.HTMLWidget.HTMLShadowWidget
import icesword.html.HTMLWidgetB
import icesword.html.map
import kotlinx.css.PointerEvents
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node

class Dialog(
    val content: HTMLWidget,
    val onClose: Stream<Unit>,
) : HTMLShadowWidget {
    override val root: HTMLWidget = content
}


class DialogOverlay(
    private val tillDetach: Till,
) {
    private val _shownDialog = MutCell<HTMLWidgetB<Dialog>?>(null)

    val shownDialog: Cell<HTMLWidget?> =
        _shownDialog.mapTillNext(tillDetach) { dialogBOrNull, tillNext ->
            dialogBOrNull?.let { dialogB ->
                val dialog = dialogB.build(tillDetach)

                dialog.onClose.reactTill(tillNext) {
                    _shownDialog.set(null)
                }

                dialog.root
            }
        }

    fun showDialog(
        dialog: HTMLWidgetB<Dialog>?,
    ) {
        _shownDialog.set(dialog)
    }

    fun linkDialog(dialogContent: Cell<HTMLWidgetB<*>?>) {
        dialogContent.reactTill(tillDetach) { contentOrNull ->
            if (contentOrNull != null) {
                val content: HTMLWidgetB<*> = contentOrNull

                showDialog(
                    content.map {
                        Dialog(
                            content = it,
                            onClose = Stream.never(),
                        )
                    },
                )
            } else {
                showDialog(null)
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

    fun createOverlay(dialog: Node) =
        createHTMLElementRaw("div").apply {
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

    val overlay = dialogOverlay.shownDialog.mapNested { createOverlay(HTMLWidget.resolve(it)) }

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
