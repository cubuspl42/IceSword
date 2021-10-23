package icesword.scene

import html.createSvgRect
import icesword.editor.SelectMode
import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import icesword.geometry.IntVec2
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement

fun createAreaSelectionOverlayElement(
    svg: SVGSVGElement,
    viewTransform: Cell<IntVec2>,
    areaSelectingMode: SelectMode.AreaSelectingMode,
    tillDetach: Till,
): SVGElement {
    val rect = areaSelectingMode.selectionArea

    val translate = Cell.map2(
        viewTransform,
        rect.map { it.position },
    ) { vt, ep -> vt + ep }

    return createSvgRect(
        svg = svg,
        translate = translate,
        size = rect.map { it.size },
        tillDetach = tillDetach,
    ).apply {
        setAttributeNS(null, "fill", "orange")
        setAttributeNS(null, "fill-opacity", "0.3")

        setAttributeNS(null, "stroke", "orange")
        setAttributeNS(null, "stroke-width", "2px")
        setAttributeNS(null, "stroke-opacity", "0.5")
    }
}
