package icesword.html

import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.reactTill
import icesword.html.HTMLWidget.HTMLElementWidget
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent

fun createButton(
    style: DynamicStyleDeclaration? = null,
    text: String,
    onPressed: () -> Unit,
    tillDetach: Till,
): HTMLElement {
    val root = createStyledHtmlElement(
        tagName = "button",
        style = style,
        tillDetach = tillDetach,
    ).apply {
        innerText = text
    }

    root.addEventListener("mousedown", {
        it.preventDefault()
    })

    root.addEventListener("click", {
        onPressed()
    })

    return root
}

class HTMLButton(
    override val element: HTMLButtonElement,
) : HTMLElementWidget {
    val onPressed: Stream<MouseEvent> by lazy { element.onEvent("click") }
}

fun createTextButtonWb(
    style: DynamicStyleDeclaration? = null,
    text: String,
    onPressed: (() -> Unit)? = null,
): HTMLWidgetB<HTMLButton> = createButtonWb(
    style = style,
    child = createTextWb(Cell.constant(text)),
    onPressed = onPressed,
)

fun createButtonWb(
    style: DynamicStyleDeclaration? = null,
    child: HTMLWidgetB<*>,
    onPressed: (() -> Unit)? = null,
) = object : HTMLWidgetB<HTMLButton> {
    override fun build(tillDetach: Till): HTMLButton {
        val childElement = HTMLWidget.resolve(child.build(tillDetach))

        val element = createStyledHtmlElement(
            tagName = "button",
            style = style,
            tillDetach = tillDetach,
        ).apply {
            appendChild(childElement)
        }

        if (onPressed != null) {
            element.onClick().reactTill(tillDetach) {
                onPressed()
            }
        }

        return HTMLButton(
            element = element as HTMLButtonElement,
        )
    }
}
