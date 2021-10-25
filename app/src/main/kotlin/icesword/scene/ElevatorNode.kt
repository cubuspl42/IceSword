package icesword.scene

import icesword.editor.Editor
import icesword.editor.Elevator
import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import icesword.geometry.IntVec2
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement

fun createElevatorOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: Cell<IntVec2>,
    elevator: Elevator,
    tillDetach: Till,
): SVGElement {
    val boundingBox = elevator.wapObjectStem.boundingBox

    val translate = Cell.map2(
        viewTransform,
        boundingBox.map { it.position },
    ) { vt, ep -> vt + ep }

    val box = createEntityFrameElement(
        editor = editor,
        svg = svg,
        outer = viewport,
        entity = elevator,
        translate = translate,
        size = boundingBox.map { it.size },
        tillDetach = tillDetach,
    )

    return box
}
