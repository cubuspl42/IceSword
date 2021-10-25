package icesword.ui

import html.MouseButton
import html.onMouseDrag
import icesword.MouseDrag
import icesword.editor.Editor
import icesword.editor.Entity
import icesword.editor.Tool
import icesword.frp.*
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

class EntityMoveDragController(
    private val editor: Editor,
) {
    companion object {
        fun create(editor: Editor, entity: Entity): Cell<EntityMoveDragController?> =
            Cell.map2(
                editor.isEntitySelected(entity),
                editor.selectedTool,
            ) { isSelected, selectedTool ->
                if (isSelected && selectedTool == Tool.MOVE) {
                    EntityMoveDragController(editor)
                } else null
            }

        fun linkDragHandler(
            element: Element,
            outer: HTMLElement,
            controller: Cell<EntityMoveDragController?>,
            till: Till,
        ) {
            element.onMouseDrag(
                button = MouseButton.Primary,
                outer = outer,
                filterTarget = true,
                till = till,
            ).reactDynamicNotNullTill(
                dynamicHandler = controller.mapNotNull { it::handleDrag },
                till = till,
            )
        }
    }

    fun handleDrag(mouseDrag: MouseDrag) {
        println("handleDrag: $mouseDrag")

        val world = editor.world

        val worldPosition = world.transformToWorld(mouseDrag.position)
        val initialWorldPosition = worldPosition.sample()
        val positionDelta = worldPosition.map {
            (it - initialWorldPosition)
        }

        editor.moveSelectedEntities(
            positionDelta = positionDelta,
            tillStop = mouseDrag.tillEnd,
        )
    }
}
