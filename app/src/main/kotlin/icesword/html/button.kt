package icesword.html

import icesword.frp.Stream
import icesword.frp.Till
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

fun createButtonWb(
    style: DynamicStyleDeclaration? = null,
    text: String,
) = object : HTMLWidgetB<HTMLButton> {
    override fun build(tillDetach: Till): HTMLButton {
        val element = createStyledHtmlElement(
            tagName = "button",
            style = style,
            tillDetach = tillDetach,
        ).apply {
            innerText = text
        }

        return HTMLButton(
            element = element as HTMLButtonElement,
        )
    }
}
