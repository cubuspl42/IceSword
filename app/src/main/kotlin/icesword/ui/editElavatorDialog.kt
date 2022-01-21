package icesword.ui

import icesword.editor.entities.Elevator
import icesword.editor.entities.Warp
import icesword.frp.Cell.Companion.constant
import icesword.html.createColumnWb
import icesword.html.createHeading4Wb
import icesword.html.createRow
import kotlinx.css.px

fun createElevatorDialog(
    elevator: Elevator<*>,
) = createBasicDialog(
    content = createColumnWb(
        children = listOf(
            createHeading4Wb(
                text = constant("Edit elevator"),
            ),
        ),
    ),
)
