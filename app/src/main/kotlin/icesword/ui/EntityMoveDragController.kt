package icesword.ui

import icesword.html.MouseButton
import icesword.html.onMouseDrag
import icesword.editor.Editor
import icesword.editor.entities.Entity
import icesword.editor.Tool
import icesword.frp.*
import icesword.geometry.DynamicTransform
import icesword.geometry.IntVec2
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
                dynamicHandler = controller.mapNested { it::handleDrag },
                till = till,
            )
        }
    }

    fun handleDrag(mouseDrag: MouseDrag) {
        println("handleDrag: $mouseDrag")

        val world = editor.world

        val worldPosition = editor.camera.transformToWorld(mouseDrag.clientPosition)
        val initialWorldPosition = worldPosition.sample()
        val positionDelta = worldPosition.map {
            (it - initialWorldPosition)
        }

        editor.moveSelectedEntities(
            positionDelta = positionDelta,
            tillStop = mouseDrag.released,
        )
    }
}

fun setupMoveController(
    viewTransform: DynamicTransform,
    outer: HTMLElement,
    element: Element,
    move: (positionDelta: Cell<IntVec2>, till: Till) -> Unit,
    till: Till,
) {
    element.onMouseDrag(
        button = MouseButton.Primary,
        outer = outer,
        filterTarget = true,
        till = till,
    ).reactTill(till) { mouseDrag ->
        val worldPosition = viewTransform.inversed.transform(mouseDrag.clientPosition)
        val initialWorldPosition = worldPosition.sample()
        val positionDelta = worldPosition.map {
            (it - initialWorldPosition)
        }

        move(positionDelta, mouseDrag.released)
    }
}
