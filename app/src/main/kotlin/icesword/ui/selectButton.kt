package icesword.ui

import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import icesword.html.DynamicStyleDeclaration
import icesword.html.createButton
import kotlinx.css.FontWeight
import org.w3c.dom.HTMLElement

fun <A, B : A> createSelectButton(
    value: B,
    name: String,
    selected: Cell<A>,
    select: (value: B) -> Unit,
    tillDetach: Till,
): HTMLElement =
    createButton(
        style = DynamicStyleDeclaration(
            fontWeight = selected.map { if (it == value) FontWeight.bold else null },
        ),
        text = name,
        onPressed = {
            select(value)
        },
        tillDetach = tillDetach,
    )
