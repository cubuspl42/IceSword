package icesword.ui

import html.onMouseDrag
import icesword.MouseDrag
import icesword.TILE_SIZE
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
                editor.selectedEntity,
                editor.selectedTool,
            ) { selectedEntity, selectedTool ->
                if (selectedEntity == entity && selectedTool == Tool.MOVE) {
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
                button = 0,
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

        editor.selectedEntity.sample()?.let { selectedEntity ->
            val worldPosition = world.transformToWorld(mouseDrag.position)
            val initialWorldPosition = worldPosition.sample()
            val tileOffsetDelta = worldPosition.map {
                (it - initialWorldPosition).divRound(TILE_SIZE)
            }

            selectedEntity.move(
                tileOffsetDelta = tileOffsetDelta,
                tillStop = mouseDrag.tillEnd,
            )
        }
    }
}
