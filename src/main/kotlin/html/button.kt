package html

import createHtmlElement
import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.reactTill
import org.w3c.dom.HTMLElement

fun createButton(
    style: Cell<CSSStyle>,
    text: String,
    onPressed: () -> Unit,
    tillDetach: Till,
): HTMLElement {
    val root = createHtmlElement("button").apply {
        innerText = text

    }

    style.reactTill(tillDetach) { s ->
        root.style.apply {
            s.fontWeight?.let { setProperty("font-weight", it.toCssString()) }
                ?: removeProperty("font-weight")
        }
    }


    root.addEventListener("click", {
        onPressed()
    })

    return root
}
