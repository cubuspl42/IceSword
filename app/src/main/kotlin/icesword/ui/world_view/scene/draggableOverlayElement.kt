package icesword.ui.world_view.scene

import icesword.editor.Editor
import icesword.editor.entities.Entity
import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import icesword.ui.EntityMoveDragController
import kotlinx.css.Cursor
import kotlinx.css.PointerEvents
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement

data class DraggableOverlayElementContext(
    val cursor: Cursor?,
    val pointerEvents: PointerEvents?,
)

fun createDraggableOverlayElement(
    editor: Editor,
    entity: Entity,
    outer: HTMLElement,
    till: Till,
    buildChild: (context: Cell<DraggableOverlayElementContext>) -> SVGElement,
): SVGElement {
    val moveController = EntityMoveDragController.create(
        editor = editor,
        entity = entity,
    )

    val context = moveController.map {
        it?.let {
            DraggableOverlayElementContext(
                cursor = Cursor.move,
                pointerEvents = null,
            )
        } ?: DraggableOverlayElementContext(
            cursor = null,
            pointerEvents = PointerEvents.none,
        )
    }

    val child = buildChild(context)

    EntityMoveDragController.linkDragHandler(
        element = child,
        outer = outer,
        controller = moveController,
        till = till,
    )

    return child
}
