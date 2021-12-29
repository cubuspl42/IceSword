package icesword.scene

import icesword.RezTextureBank
import icesword.editor.CrumblingPeg
import icesword.editor.Editor
import icesword.editor.Rope
import org.w3c.dom.svg.SVGElement

class CrumblingPegNode(
    private val editor: Editor,
    private val crumblingPeg: CrumblingPeg,
) : HybridNode() {
    override fun buildCanvasNode(
        textureBank: RezTextureBank,
    ): CanvasNode = WapSpriteNode(
        editorTextureBank = editor.editorTextureBank,
        textureBank = textureBank,
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
            boundingBox = viewTransform.transform(boundingBox),
            tillDetach = tillDetach,
        )
    }
}
