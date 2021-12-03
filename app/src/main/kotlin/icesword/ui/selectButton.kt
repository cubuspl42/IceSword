package icesword.ui

import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.Till
import icesword.frp.map
import icesword.html.DynamicStyleDeclaration
import icesword.html.HTMLButton
import icesword.html.HTMLWidgetB
import icesword.html.createButton
import icesword.html.createButtonWb
import icesword.html.createClippedImage
import kotlinx.css.Color
import kotlinx.css.FontWeight
import kotlinx.css.px
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

fun <A, B : A> createImageSelectButton(
    value: B,
    imagePath: String,
    selected: Cell<A>,
    select: (value: B) -> Unit,
): HTMLWidgetB<HTMLButton> =
    createButtonWb(
        style = DynamicStyleDeclaration(
            padding = constant(4.px),
            backgroundColor = selected.map { if (it == value) Color.darkGray else null },
        ),
        child = createClippedImage(
            width = 48.px,
            height = 48.px,
            path = imagePath,
        ),
        onPressed = {
            select(value)
        },
    )
