package icesword.html

import icesword.frp.Cell
import icesword.frp.DynamicMap
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.reactTill
import org.w3c.dom.HTMLElement

data class GenericElementAttrs(
    val attrs: DynamicMap<String, String>,
) {
    fun linkTo(
        element: HTMLElement,
        tillDetach: Till,
    ) {
        attrs.changes.reactTill(tillDetach) { change ->
            change.added.forEach { (qualifiedName, value) ->
                element.setAttribute(qualifiedName, value)
            }

            change.removed.forEach { qualifiedName ->
                element.removeAttribute(qualifiedName)
            }
        }
    }
}

data class HTMLElementAttrs(
    val draggable: Cell<Boolean>? = null,
) {
    fun linkTo(
        element: HTMLElement,
        tillDetach: Till,
    ) {
        linkAttribute(
            element = element,
            attributeName = "draggable",
            attribute = draggable?.map { if (it) "true" else null },
            till = tillDetach,
        )
    }
}

private fun linkAttribute(
    element: HTMLElement,
    attributeName: String,
    attribute: Cell<String?>?,
    till: Till,
) {
    attribute?.reactTill(till) {
        if (it != null) {
            element.setAttribute(attributeName, it)
        } else {
            element.removeAttribute(attributeName)
        }
    }
}
