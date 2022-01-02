package icesword.scene

import icesword.editor.Editor
import icesword.editor.entities.Rope
import org.w3c.dom.svg.SVGElement

class RopeNode(
    private val editor: Editor,
    private val rope: Rope,
) : HybridNode() {
    override fun buildCanvasNode(
        context: CanvasNodeBuildContext,
    ): CanvasNode = WapSpriteNode(
        editorTextureBank = editor.editorTextureBank,
        textureBank = context.textureBank,
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
