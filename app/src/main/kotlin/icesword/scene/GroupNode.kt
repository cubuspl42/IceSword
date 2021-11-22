package icesword.scene

import TextureBank
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.mapNotNull
import icesword.frp.dynamic_list.toDynamicSet
import icesword.frp.map
import icesword.html.createSvgGroup
import org.w3c.dom.svg.SVGElement

open class GroupNode(
    private val children: DynamicList<HybridNode>,
) : HybridNode {
    override fun buildCanvasNode(
        textureBank: TextureBank,
    ): CanvasNode = GroupCanvasNode(
        children = children.mapNotNull {
            it.buildCanvasNode(textureBank = textureBank)
        },
    )

    override fun buildOverlayElement(
        context: HybridNode.OverlayBuildContext,
    ): SVGElement = context.run {
        createSvgGroup(
            svg = svg,
            children = children.toDynamicSet().map {
                it.buildOverlayElement(context)
            },
            tillDetach = tillDetach,
        )
    }
}

