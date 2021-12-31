package icesword.scene

import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.reactTill
import icesword.geometry.DynamicTransform
import icesword.geometry.IntVec2
import icesword.html.MouseButton
import icesword.html.onMouseDrag
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

fun setupDeltaDragController(
    outer: HTMLElement,
    viewTransform: DynamicTransform,
    element: Element,
    tillDetach: Till,
    apply: (
        worldDelta: Cell<IntVec2>,
        tillStop: Till,
    ) -> Unit,
) {
    val reverseTransform = viewTransform.inversed

    element.onMouseDrag(
        button = MouseButton.Primary,
        outer = outer,
        till = tillDetach,
    ).reactTill(tillDetach) { mouseDrag ->
        val worldPointerPosition = reverseTransform.transform(
            point = mouseDrag.clientPosition,
        )

        val initialWorldPosition = worldPointerPosition.sample()

        val worldDelta = worldPointerPosition.map {
            it - initialWorldPosition
        }

        apply(
            worldDelta,
            mouseDrag.released,
        )
    }
}
