package icesword.ui.scene

import icesword.editor.entities.CrumblingPeg
import icesword.editor.Editor
import icesword.ui.CanvasNode
import org.w3c.dom.svg.SVGElement

class CrumblingPegNode(
    private val editor: Editor,
    private val crumblingPeg: CrumblingPeg,
) : HybridNode() {
    override fun buildCanvasNode(
        context: CanvasNodeBuildContext,
    ): CanvasNode = WapSpriteNode(
        editorTextureBank = editor.editorTextureBank,
        textureBank = context.textureBank,
        wapSprite = crumblingPeg.wapSprite,
    )

    override fun buildOverlayElement(
        context: OverlayBuildContext,
    ): SVGElement = context.run {
        val boundingBox = crumblingPeg.wapSprite.boundingBox

        createEntityFrameElement(
            editor = editor,
            svg = svg,
            outer = viewport,
            entity = crumblingPeg,
            viewBoundingBox = viewTransform.transform(boundingBox),
            tillDetach = tillDetach,
        )
    }
}
