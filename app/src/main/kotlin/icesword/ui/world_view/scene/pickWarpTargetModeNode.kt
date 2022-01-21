package icesword.ui.world_view.scene

import icesword.editor.modes.PickWarpTargetMode
import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.dynamic_list.staticListOf
import icesword.frp.mapNested
import icesword.geometry.DynamicTransform
import icesword.geometry.IntVec2
import icesword.html.createSvgGroupDl
import icesword.html.createSvgLine
import icesword.html.createSvgSwitch
import icesword.ui.world_view.scene.base.HybridNode
import kotlinx.css.Color
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement

fun createPickWarpTargetModeNode(
    pickWarpTargetMode: PickWarpTargetMode,
): HybridNode = object : HybridNode() {
    override fun buildOverlayElement(context: OverlayBuildContext): SVGElement = context.run {
        createSvgSwitch(
            child = pickWarpTargetMode.pickingState.mapNested { pickingStateNow ->
                buildCross(
                    svg = svg,
                    transform = DynamicTransform.translate(
                        viewTransform.transform(pickingStateNow.pickerWorldPosition)
                    ),
                    tillDetach = tillDetach,
                )
            },
            tillDetach = tillDetach,
        )
    }
}

private fun buildCross(
    svg: SVGSVGElement,
    transform: DynamicTransform,
    tillDetach: Till,
): SVGElement {
    val a = 32
    val color = Cell.constant(Color.darkBlue)

    return createSvgGroupDl(
        svg = svg,
        transform = transform,
        children = staticListOf(
            createSvgLine(
                pointA = Cell.constant(IntVec2(0, -a)),
                pointB = Cell.constant(IntVec2(0, a)),
                stroke = color,
                tillDetach = tillDetach,
            ),
            createSvgLine(
                pointA = Cell.constant(IntVec2(-a, 0)),
                pointB = Cell.constant(IntVec2(a, 0)),
                stroke = color,
                tillDetach = tillDetach,
            ),
        ),
        tillDetach = tillDetach,
    )
}
