package icesword

import icesword.editor.TogglePeg
import icesword.frp.Cell.Companion.constant
import icesword.html.createColumnWb
import icesword.html.createHeading4Wb
import icesword.html.createRow
import kotlinx.css.px

fun createTogglePegDialog(
    togglePeg: TogglePeg,
) = createBasicDialog(
    content = createColumnWb(
        children = listOf(
            createHeading4Wb(
                text = constant("Edit toggle peg"),
            ),
            createRow(
                horizontalGap = 8.px,
                children = listOf(
                    createInputColumn(
                        inputs = listOf(
                            createIntegerInput(
                                labelText = "Time on [ms]",
                                initialValue = togglePeg.timeOnMs.sample(),
                                onValueChanged = togglePeg::setTimeOnMs,
                            ),
                            createIntegerInput(
                                labelText = "Time off [ms]",
                                initialValue = togglePeg.timeOffMs.sample(),
                                onValueChanged = togglePeg::setTimeOffMs,
                            ),
                            createIntegerInput(
                                labelText = "Delay [ms]",
                                initialValue = togglePeg.delayMs.sample(),
                                onValueChanged = togglePeg::setDelayMs,
                            ),
                        ),
                    ),
                ),
            ),
        ),
    ),
)
