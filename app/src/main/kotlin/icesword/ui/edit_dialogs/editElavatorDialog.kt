package icesword.ui.edit_dialogs

import icesword.editor.entities.Elevator
import icesword.frp.Cell.Companion.constant
import icesword.html.createColumnWb
import icesword.html.createHeading4Wb

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
