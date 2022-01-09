package icesword.ui.scene.base

import icesword.EditorTextureBank
import icesword.RezTextureBank
import icesword.frp.Cell
import icesword.frp.DynamicSet
import icesword.frp.Till
import icesword.frp.dynamic_list.DynamicList
import icesword.geometry.DynamicTransform
import icesword.html.createSvgGroup
import icesword.ui.CanvasNode
import icesword.ui.scene.GroupNode
import icesword.ui.scene.NoopCanvasNode
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement

abstract class HybridNode {
    companion object {
        fun ofSingle(node: Cell<HybridNode?>): HybridNode = GroupNode(
            children = DynamicList.ofSingle(node)
        )
    }

    data class CanvasNodeBuildContext(
        val editorTextureBank: EditorTextureBank,
        val textureBank: RezTextureBank,
    )

    data class OverlayBuildContext(
        val svg: SVGSVGElement,
        val viewport: HTMLElement,
        val viewTransform: DynamicTransform,
        val tillDetach: Till,
    )

    open fun buildCanvasNode(
        context: CanvasNodeBuildContext,
    ): CanvasNode = NoopCanvasNode()

    open fun buildOverlayElement(
        context: OverlayBuildContext,
    ): SVGElement = createSvgGroup(
        svg = context.svg,
        children = DynamicSet.empty(),
        tillDetach = context.tillDetach
    )

    override fun equals(other: Any?): Boolean {
        println("HybridNode.equals")
//        throw UnsupportedOperationException()
        return false
    }

    override fun hashCode(): Int {
        println("HybridNode.hashCode")
        //        throw UnsupportedOperationException()
        return 0
    }
}

fun hybridCanvasNode(build: (HybridNode.CanvasNodeBuildContext) -> CanvasNode): HybridNode = object : HybridNode() {
    override fun buildCanvasNode(context: CanvasNodeBuildContext): CanvasNode =
        build(context)
}

fun hybridOverlayNode(build: (svg: SVGSVGElement) -> SVGElement): HybridNode = object : HybridNode() {
    override fun buildOverlayElement(context: OverlayBuildContext): SVGElement =
        build(context.svg)
}
