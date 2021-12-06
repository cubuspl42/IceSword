package icesword.scene

import TextureBank
import icesword.editor.CrateStack
import icesword.editor.Editor
import org.w3c.dom.svg.SVGElement

class CrateStackNode(
    private val editor: Editor,
    private val crateStack: CrateStack,
) : HybridNode {
    override fun buildCanvasNode(
        textureBank: TextureBank,
    ): CanvasNode = WapSpriteNode(
        textureBank = textureBank,
        wapSprite = crateStack.wapSprite,
    )

    override fun buildOverlayElement(
        context: HybridNode.OverlayBuildContext,
    ): SVGElement = context.run {
        val boundingBox = crateStack.wapSprite.boundingBox

        createEntityFrameElement(
            editor = editor,
            svg = svg,
            outer = viewport,
            entity = crateStack,
            boundingBox = viewTransform.transform(boundingBox),
            tillDetach = tillDetach,
        )
    }
}
