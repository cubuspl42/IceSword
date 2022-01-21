package icesword.ui.world_view.scene

import icesword.editor.modes.PickWarpTargetMode
import icesword.frp.mapNested
import icesword.geometry.DynamicTransform
import icesword.html.createSvgSwitch
import icesword.ui.svg.createSvgCross
import icesword.ui.world_view.scene.base.HybridNode
import kotlinx.css.Color
import org.w3c.dom.svg.SVGElement

fun createPickWarpTargetModeNode(
    pickWarpTargetMode: PickWarpTargetMode,
): HybridNode = object : HybridNode() {
    override fun buildOverlayElement(context: OverlayBuildContext): SVGElement = context.run {
        createSvgSwitch(
            child = pickWarpTargetMode.pickingState.mapNested { pickingStateNow ->
                createSvgCross(
                    svg = svg,
                    transform = DynamicTransform.translate(
                        viewTransform.transform(pickingStateNow.pickerWorldPosition)
                    ),
                    color = Color.darkBlue,
                    tillDetach = tillDetach,
                )
            },
            tillDetach = tillDetach,
        )
    }
}
