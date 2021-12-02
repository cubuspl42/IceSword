package icesword

import icesword.editor.Rope
import icesword.frp.Cell.Companion.constant
import icesword.frp.Till
import icesword.frp.dynamic_list.staticListOf
import icesword.frp.reactTill
import icesword.frp.values
import icesword.html.HTMLWidget
import icesword.html.HTMLWidgetB
import icesword.html.alsoTillDetach
import icesword.html.createColumnWb
import icesword.html.createHeading4
import icesword.html.createHeading4Wb
import icesword.html.createLabel
import icesword.html.createNumberInput
import icesword.html.createRowWb
import kotlinx.css.px

fun createEditRopeDialog(
    rope: Rope,
): HTMLWidgetB<Dialog> = createBasicDialog(
    content = createColumnWb(
        verticalGap = 8.px,
        children = listOf(
            createHeading4Wb(
                text = constant("Edit rope"),
            ),
            createRowWb(
                horizontalGap = 16.px,
                children = listOf(
                    createLabel(
                        text = "Swing duration [ms]",
                    ),
                    createNumberInput(
                        initialValue = rope.swingDurationMs.sample(),
                    ).alsoTillDetach { input, tillDetach ->
                        input.value.values().reactTill(tillDetach) { newValue ->
                            rope.setSwingDuration(newValue)
                        }
                    },
                ),
            ),
        ),
    ),
)
