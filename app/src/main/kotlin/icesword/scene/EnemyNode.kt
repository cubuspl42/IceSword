package icesword.scene

import TextureBank
import icesword.editor.Editor
import icesword.editor.Enemy
import org.w3c.dom.svg.SVGElement

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
        val boundingBox = enemy.wapSprite.boundingBox

        return createEntityFrameElement(
            editor = editor,
            svg = context.svg,
            outer = context.viewport,
            entity = enemy,
            boundingBox = context.viewTransform.transform(boundingBox),
            tillDetach = context.tillDetach,
        )
    }
}
