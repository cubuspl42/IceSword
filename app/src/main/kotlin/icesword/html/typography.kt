package icesword.html

import icesword.frp.Cell
import icesword.frp.dynamic_list.staticListOf
import kotlinx.browser.document
import kotlinx.css.em
import org.w3c.dom.HTMLElement

fun createHeading4(text: String): HTMLElement =
    createHTMLElementRaw("h4").apply {
        appendChild(
            document.createTextNode(text),
        )
    }

fun createLabel(text: String): HTMLWidgetB<*> =
    createHTMLWidgetB(
        tagName = "label",
        children = staticListOf(
            createTextWb(Cell.constant(text)),
        ),
    )

fun createHeading4Wb(text: Cell<String>): HTMLWidgetB<*> =
    createHTMLWidgetB(
        tagName = "h4",
        style = DynamicStyleDeclaration(
            marginBlock = Cell.constant(0.25.em),
        ),
        children = staticListOf(
            createTextWb(text),
        ),
    )

fun createHeading5Wb(text: Cell<String>): HTMLWidgetB<*> =
    createHTMLWidgetB(
        tagName = "h5",
        style = DynamicStyleDeclaration(
            marginBlock = Cell.constant(0.25.em),
        ),
        children = staticListOf(
            createTextWb(text),
        ),
    )
