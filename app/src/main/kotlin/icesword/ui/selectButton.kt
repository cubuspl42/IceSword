package icesword.ui

import html.CSSStyle
import html.FontWeight
import html.createButton
import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import org.w3c.dom.HTMLElement

fun <A> createSelectButton(
    value: A,
    name: String,
    selected: Cell<A>,
    select: (value: A) -> Unit,
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