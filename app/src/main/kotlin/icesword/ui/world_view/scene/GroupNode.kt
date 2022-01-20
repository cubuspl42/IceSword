package icesword.ui.world_view.scene

import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.mapNotNull
import icesword.frp.dynamic_list.mapTillRemoved
import icesword.html.createSvgGroupDl
import icesword.ui.CanvasNode
import icesword.ui.world_view.scene.base.HybridNode
import org.w3c.dom.svg.SVGElement

open class GroupNode(
    private val children: DynamicList<HybridNode>,
) : HybridNode() {
    override fun buildCanvasNode(
        context: CanvasNodeBuildContext,
    ): CanvasNode = GroupCanvasNode(
        // Should these children be pinned? (mapTillRemoved or something)
        children = children.mapNotNull {
            it.buildCanvasNode(context = context)
        },
    )

    override fun buildOverlayElement(
        context: OverlayBuildContext,
    ): SVGElement = context.run {
        createSvgGroupDl(
            svg = svg,
            children = children.mapTillRemoved(tillDetach) { it, tillRemoved ->
                it.buildOverlayElement(context.copy(tillDetach = tillRemoved))
            },
            tillDetach = tillDetach,
        )
    }
}
