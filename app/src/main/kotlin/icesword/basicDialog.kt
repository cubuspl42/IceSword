package icesword

import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.mergeWith
import icesword.frp.units
import icesword.html.DynamicStyleDeclaration
import icesword.html.FlexStyleDeclaration
import icesword.html.HTMLWidgetB
import icesword.html.createColumnWb
import icesword.html.createTextButtonWb
import icesword.html.flatMap
import icesword.html.map
import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.px

fun createBasicDialog(
    content: HTMLWidgetB<*>,
    onClose: Stream<Unit> = Stream.never(),
): HTMLWidgetB<Dialog> = createTextButtonWb(
    style = DynamicStyleDeclaration(
        alignSelf = Cell.constant(Align.flexEnd),
    ),
    text = "✕",
).flatMap { closeButton ->
    createColumnWb(
        style = DynamicStyleDeclaration(
            backgroundColor = Cell.constant(Color("#d1d1d1")),
            paddingString = Cell.constant("16px"),
            fontFamily = Cell.constant("sans-serif"),
        ),
        flexStyle = FlexStyleDeclaration(
            alignItems = Cell.constant(Align.center),
        ),
        verticalGap = 8.px,
        children = listOf(
            closeButton,
            content,
        ),
    ).map { content ->
        Dialog(
            content = content,
            onClose = closeButton.onPressed.units().mergeWith(onClose),
        )
    }
}
