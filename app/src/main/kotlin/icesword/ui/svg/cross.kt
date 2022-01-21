package icesword.ui.svg

import icesword.frp.Cell.Companion.constant
import icesword.frp.Till
import icesword.frp.dynamic_list.staticListOf
import icesword.geometry.DynamicTransform
import icesword.geometry.IntVec2
import icesword.html.createSvgGroupDl
import icesword.html.createSvgLine
import kotlinx.css.Color
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement

fun createSvgCross(
    svg: SVGSVGElement,
    transform: DynamicTransform,
    color: Color,
    tillDetach: Till,
): SVGElement {
    val a = 32

    return createSvgGroupDl(
        svg = svg,
        transform = transform,
        children = staticListOf(
            createSvgLine(
                pointA = constant(IntVec2(0, -a)),
                pointB = constant(IntVec2(0, a)),
                stroke = constant(color),
                tillDetach = tillDetach,
            ),
            createSvgLine(
                pointA = constant(IntVec2(-a, 0)),
                pointB = constant(IntVec2(a, 0)),
                stroke = constant(color),
                tillDetach = tillDetach,
            ),
        ),
        tillDetach = tillDetach,
    )
}
