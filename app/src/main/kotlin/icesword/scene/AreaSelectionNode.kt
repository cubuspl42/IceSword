package icesword.scene

import icesword.editor.SelectMode
import icesword.frp.Till
import icesword.geometry.DynamicTransform
import icesword.html.createSvgRectR
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement

fun createAreaSelectionOverlayElement(
    svg: SVGSVGElement,
    viewTransform: DynamicTransform,
    areaSelectingMode: SelectMode<*>.AreaSelectingMode,
    tillDetach: Till,
): SVGElement {
    val rect = areaSelectingMode.selectionArea

    val viewRect = viewTransform.transform(rect)

    return createSvgRectR(
        svg = svg,
        rect = viewRect,
        tillDetach = tillDetach,
    ).apply {
        setAttributeNS(null, "fill", "orange")
        setAttributeNS(null, "fill-opacity", "0.3")

        setAttributeNS(null, "stroke", "orange")
        setAttributeNS(null, "stroke-width", "2px")
        setAttributeNS(null, "stroke-opacity", "0.5")
    }
}
