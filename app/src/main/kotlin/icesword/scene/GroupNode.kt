package icesword.scene

import TextureBank
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.map
import icesword.frp.dynamic_list.toDynamicSet
import icesword.frp.mapNotNull
import icesword.html.createSvgGroup
import org.w3c.dom.svg.SVGElement

open class GroupNode(
    private val children: DynamicList<HybridNode>,
) : HybridNode {
    override fun buildCanvasNode(
        textureBank: TextureBank,
    ): CanvasNode = GroupCanvasNode(
        children = children.map {
            it.buildCanvasNode(textureBank = textureBank)
        },
    )

    override fun buildOverlayElement(
        context: HybridNode.OverlayBuildContext,
    ): SVGElement = context.run {
        createSvgGroup(
            svg = svg,
            children = children.toDynamicSet().mapNotNull {
                it.buildOverlayElement(context)
            },
            tillDetach = tillDetach,
        )
    }
}
