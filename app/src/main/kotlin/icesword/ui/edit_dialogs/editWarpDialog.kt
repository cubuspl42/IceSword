package icesword.ui.edit_dialogs

import icesword.editor.entities.Warp
import icesword.frp.Cell.Companion.constant
import icesword.html.createColumnWb
import icesword.html.createHeading4Wb
import icesword.html.createRow
import kotlinx.css.px

fun createWarpDialog(
    warp: Warp,
) = createBasicDialog(
    content = createColumnWb(
        children = listOf(
            createHeading4Wb(
                text = constant("Edit warp"),
            ),
            createRow(
                horizontalGap = 8.px,
                children = listOf(
                    createInputColumn(
                        inputs = listOf(
                            createIntegerInput(
                                labelText = "Target X",
                                initialValue = warp.targetPosition.sample().x,
                                onValueChanged = {
                                    warp.setTargetPosition(
                                        warp.targetPosition.sample().copy(x = it)
                                    )
                                }
                            ),
                            createIntegerInput(
                                labelText = "Target Y",
                                initialValue = warp.targetPosition.sample().y,
                                onValueChanged = {
                                    warp.setTargetPosition(
                                        warp.targetPosition.sample().copy(y = it)
                                    )
                                }
                            ),
                        ),
                    ),
                ),
            ),
        ),
    ),
)
