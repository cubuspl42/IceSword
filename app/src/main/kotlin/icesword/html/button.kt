package icesword.html

import icesword.frp.Till
import org.w3c.dom.HTMLElement

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
