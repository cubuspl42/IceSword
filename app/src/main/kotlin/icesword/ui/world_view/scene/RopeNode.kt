package icesword.ui.world_view.scene

import icesword.editor.Editor
import icesword.editor.entities.Rope
import icesword.ui.CanvasNode
import icesword.ui.world_view.scene.base.HybridNode
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
            viewBoundingBox = viewTransform.transform(boundingBox),
            tillDetach = tillDetach,
        )
    }
}
