package icesword.ui

import icesword.html.CSSStyle
import icesword.html.FontWeight
import icesword.html.createButton
import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import org.w3c.dom.HTMLElement

fun <A, B : A> createSelectButton(
    value: B,
    name: String,
    selected: Cell<A>,
    select: (value: B) -> Unit,
    tillDetach: Till,
): HTMLElement =
    createButton(
        style = selected.map {
            CSSStyle(
                fontWeight = if (it == value) FontWeight.bold else null,
            )
        },
        text = name,
        onPressed = {
            select(value)
        },
        tillDetach = tillDetach,
    )
