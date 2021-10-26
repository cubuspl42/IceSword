package icesword.scene

import icesword.editor.Editor
import icesword.editor.SelectMode
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.reactTill
import icesword.frp.reactTillNext
import icesword.geometry.IntVec2
import icesword.html.MouseButton
import icesword.html.calculateRelativePosition
import icesword.html.onMouseDrag
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGRectElement

fun createBackFoilOverlayElement(
    editor: Editor,
    viewport: HTMLElement,
    tillDetach: Till,
): SVGElement {
    val foil = document.createElementNS("http://www.w3.org/2000/svg", "rect") as SVGRectElement

    foil.apply {
        setAttributeNS(null, "x", "0%")
        setAttributeNS(null, "y", "0%")
        setAttributeNS(null, "width", "100%")
        setAttributeNS(null, "height", "100%")
        setAttributeNS(null, "fill-opacity", "0")
    }

    editor.editorMode.reactTillNext(tillDetach) { mode, tillNext ->
        when (mode) {
            is SelectMode -> setupSelectModeController(
                editor = editor,
                viewport = viewport,
                element = foil,
                selectMode = mode,
                tillDetach = tillNext,
            )
        }
    }

    return foil
}

private fun setupSelectModeController(
    editor: Editor,
    viewport: HTMLElement,
    element: Element,
    selectMode: SelectMode,
    tillDetach: Till,
) {
    val world = editor.world

    fun calculateWorldPosition(clientPosition: IntVec2): IntVec2 {
        val viewportPosition =
            element.calculateRelativePosition(clientPosition)

        val worldPosition: IntVec2 =
            world.transformToWorld(cameraPoint = viewportPosition).sample()

        return worldPosition
    }

    val button = MouseButton.Primary

    element.onMouseDrag(
        button = button,
        outer = viewport,
        till = tillDetach
    )
        .reactTill(tillDetach) { mouseDrag ->
            (selectMode.state.sample() as? SelectMode.IdleMode)?.selectArea(
                anchorWorldCoord = calculateWorldPosition(
                    clientPosition = mouseDrag.position.sample()
                ),
                targetWorldCoord = mouseDrag.position.map(::calculateWorldPosition),
                confirm = mouseDrag.onEnd,
                abort = Stream.never(), // FIXME?
            )
        }
}
