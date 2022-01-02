package icesword.ui.scene

import icesword.editor.Editor
import icesword.editor.modes.KnotSelectMode
import icesword.editor.SelectMode
import icesword.frp.Till
import icesword.frp.reactTill
import icesword.frp.reactTillNext
import icesword.html.MouseButton
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
            is KnotSelectMode -> setupSelectModeController(
                editor = editor,
                viewport = viewport,
                element = foil,
                selectMode = mode.selectMode,
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
    selectMode: SelectMode<*>,
    tillDetach: Till,
) {
    element.onMouseDrag(
        button = MouseButton.Primary,
        outer = viewport,
        till = tillDetach
    ).reactTill(tillDetach) { mouseDrag ->
        val worldPosition = editor.camera.transformToWorld(
            cameraPoint = mouseDrag.relativePosition,
        )

        (selectMode.state.sample() as? SelectMode.IdleMode)?.selectArea(
            anchorWorldCoord = worldPosition.sample(),
            targetWorldCoord = worldPosition,
            confirm = mouseDrag.onReleased,
        )
    }
}
