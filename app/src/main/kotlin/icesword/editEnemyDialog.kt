package icesword

import icesword.editor.Enemy
import icesword.frp.Cell.Companion.constant
import icesword.frp.units
import icesword.html.DynamicStyleDeclaration
import icesword.html.HTMLWidgetB
import icesword.html.createButtonWb
import icesword.html.createColumnWb
import icesword.html.createTextWb
import icesword.html.flatMap
import icesword.html.map
import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.px


fun createEditEnemyDialog(
    enemy: Enemy,
): HTMLWidgetB<Dialog> = createButtonWb(
    style = DynamicStyleDeclaration(
        alignSelf = constant(Align.flexEnd),
    ),
    text = "✕",
).flatMap { closeButton ->
    createColumnWb(
        style = DynamicStyleDeclaration(
            backgroundColor = constant(Color("#d1d1d1")),
            padding = constant("16px"),
            fontFamily = constant("sans-serif"),
        ),
        verticalGap = 8.px,
        children = listOf(
            closeButton,
            createTextWb(
                text = constant("Enemy: ${enemy.imageSetId.fullyQualifiedId}"),
            ),
            createButtonWb(
                style = DynamicStyleDeclaration(
                    alignSelf = constant(Align.flexStart),
                ),
                text = "Save",
            ),
        ),
    ).map { content ->
        Dialog(
            content = content,
            onClose = closeButton.onPressed.units(),
        )
    }
}
