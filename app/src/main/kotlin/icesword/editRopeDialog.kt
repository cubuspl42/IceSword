package icesword

import icesword.editor.Rope
import icesword.frp.Cell.Companion.constant
import icesword.html.HTMLWidgetB
import icesword.html.createHeading4Wb

fun createEditRopeDialog(
    rope: Rope,
): HTMLWidgetB<Dialog> = createBasicDialog(
    content = createHeading4Wb(
        text = constant("Edit rope"),
    ),
)
