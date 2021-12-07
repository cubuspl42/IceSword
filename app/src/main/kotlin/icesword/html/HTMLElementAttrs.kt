package icesword.html

import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.reactTill
import icesword.geometry.IntVec2
import kotlinx.css.Align
import kotlinx.css.BackgroundRepeat
import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.Cursor
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.FontWeight
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension
import kotlinx.css.ObjectFit
import kotlinx.css.Overflow
import kotlinx.css.PointerEvents
import kotlinx.css.properties.Transforms
import kotlinx.css.properties.translate
import kotlinx.css.px
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.css.CSSStyleDeclaration

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
