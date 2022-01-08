package icesword.ui.scene

import icesword.editor.DynamicWapSprite
import icesword.editor.Editor
import icesword.editor.entities.Entity
import icesword.ui.CanvasNode
import org.w3c.dom.svg.SVGElement

class EntityWapSpriteNode(
    private val editor: Editor,
    private val entity: Entity,
    private val wapSprite: DynamicWapSprite,
) : HybridNode() {
    override fun buildCanvasNode(
        context: CanvasNodeBuildContext,
    ): CanvasNode = WapSpriteNode(
        editorTextureBank = editor.editorTextureBank,
        textureBank = context.textureBank,
        wapSprite = wapSprite,
    )

    override fun buildOverlayElement(
        context: OverlayBuildContext,
    ): SVGElement = context.run {
        val boundingBox = wapSprite.boundingBox

        createEntityFrameElement(
            editor = editor,
            svg = svg,
            outer = viewport,
            entity = entity,
            viewBoundingBox = viewTransform.transform(boundingBox),
            tillDetach = tillDetach,
        )
    }
}
