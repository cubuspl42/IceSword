package icesword.scene

import TextureBank
import icesword.editor.Editor
import icesword.editor.Enemy
import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import icesword.geometry.DynamicTransform
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.geometry.Transform
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement

class EnemyNode(
    private val editor: Editor,
    private val enemy: Enemy,
) : HybridNode {
    override fun buildCanvasNode(
        textureBank: TextureBank,
    ): Node = WapSpriteNode(
        textureBank = textureBank,
        wapSprite = enemy.wapSprite,
    )

    override fun buildOverlayElement(
        context: HybridNode.OverlayBuildContext,
    ): SVGElement {
        val entityFrameTranslate =
            context.viewTransform.transform(point = enemy.position)

        return createEntityFrameElement(
            editor = editor,
            svg = context.svg,
            outer = context.viewport,
            entity = enemy,
            translate = entityFrameTranslate,
            size = Cell.constant(IntSize(64, 64)),
            tillDetach = context.tillDetach,
        )
    }
}
