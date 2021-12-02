package icesword.scene

import TextureBank
import icesword.RezIndex
import icesword.editor.Editor
import icesword.editor.Enemy
import icesword.editor.PickupKind
import icesword.editor.Rope
import icesword.frp.Cell
import icesword.frp.DynamicSet
import icesword.frp.Till
import icesword.frp.dynamic_list.map
import icesword.frp.dynamic_list.mapIndexed
import icesword.frp.map
import icesword.geometry.DynamicTransform
import icesword.geometry.IntVec2
import icesword.html.DynamicStyleDeclaration
import icesword.html.HTMLWidget
import icesword.html.createGridDl
import icesword.html.createSvgForeignObject
import icesword.html.createSvgGroup
import icesword.html.createTableContainer
import icesword.html.createWrapper
import icesword.html.resolve
import kotlinx.css.Align
import kotlinx.css.px
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement

class RopeNode(
    private val editor: Editor,
    private val rope: Rope,
) : HybridNode {
    override fun buildCanvasNode(
        textureBank: TextureBank,
    ): CanvasNode = WapSpriteNode(
        textureBank = textureBank,
        wapSprite = rope.wapSprite,
    )

    override fun buildOverlayElement(
        context: HybridNode.OverlayBuildContext,
    ): SVGElement = context.run {
        val boundingBox = rope.wapSprite.boundingBox

        createEntityFrameElement(
            editor = editor,
            svg = svg,
            outer = viewport,
            entity = rope,
            boundingBox = viewTransform.transform(boundingBox),
            tillDetach = tillDetach,
        )
    }
}
