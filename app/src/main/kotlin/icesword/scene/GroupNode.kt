package icesword.scene

import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.mapNotNull
import icesword.frp.dynamic_list.toDynamicSet
import icesword.frp.mapTillRemoved
import icesword.html.createSvgGroup
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
        context: HybridNode.OverlayBuildContext,
    ): SVGElement = context.run {
        createSvgGroup(
            svg = svg,
            children = children.toDynamicSet().mapTillRemoved(tillDetach) { it, tillRemoved ->
                it.buildOverlayElement(context.copy(tillDetach = tillRemoved))
            },
            tillDetach = tillDetach,
        )
    }
}

