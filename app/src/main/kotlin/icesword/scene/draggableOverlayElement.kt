package icesword.scene

import icesword.editor.Editor
import icesword.editor.Entity
import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import icesword.ui.EntityMoveDragController
import kotlinx.css.Cursor
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement

fun createDraggableOverlayElement(
    editor: Editor,
    entity: Entity,
    outer: HTMLElement,
    till: Till,
    buildChild: (cursor: Cell<Cursor?>) -> SVGElement,
): SVGElement {
    val moveController = EntityMoveDragController.create(
        editor = editor,
        entity = entity,
    )

    val cursor = moveController.map { it?.let { Cursor.move } }

    val child = buildChild(
        cursor,
    )

    EntityMoveDragController.linkDragHandler(
        element = child,
        outer = outer,
        controller = moveController,
        till = till,
    )

    return child
}
