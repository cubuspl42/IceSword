package icesword

import icesword.editor.CrateStack
import icesword.frp.Cell.Companion.constant
import icesword.frp.reactTill
import icesword.html.HTMLWidgetB
import icesword.html.alsoTillDetach
import icesword.html.createColumnWb
import icesword.html.createHeading4Wb
import icesword.html.createTextButtonWb
import kotlinx.css.px

fun createEditCrateStackDialog(
    crateStack: CrateStack,
): HTMLWidgetB<Dialog> = createBasicDialog(
    content = createColumnWb(
        verticalGap = 8.px,
        children = listOf(
            createHeading4Wb(
                text = constant("Edit crate stack"),
            ),
            createTextButtonWb(
                text = "+",
            ).alsoTillDetach { button, tillDetach ->
                button.onPressed.reactTill(tillDetach) {
                    crateStack.pushCrate()
                }
            },
            createTextButtonWb(
                text = "-",
            ).alsoTillDetach { button, tillDetach ->
                button.onPressed.reactTill(tillDetach) {
                    crateStack.popCrate()
                }
            },
        ),
    ),
)
