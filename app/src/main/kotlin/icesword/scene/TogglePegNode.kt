package icesword.scene

import icesword.RezTextureBank
import icesword.editor.Editor
import icesword.editor.TogglePeg
import org.w3c.dom.svg.SVGElement

class TogglePegNode(
    private val editor: Editor,
    private val togglePeg: TogglePeg,
) : HybridNode() {
    override fun buildCanvasNode(
        textureBank: RezTextureBank,
    ): CanvasNode = WapSpriteNode(
        editorTextureBank = editor.editorTextureBank,
        textureBank = textureBank,
        wapSprite = togglePeg.wapSprite,
    )

    override fun buildOverlayElement(
        context: OverlayBuildContext,
    ): SVGElement = context.run {
        val boundingBox = togglePeg.wapSprite.boundingBox

        createEntityFrameElement(
            editor = editor,
            svg = svg,
            outer = viewport,
            entity = togglePeg,
            boundingBox = viewTransform.transform(boundingBox),
            tillDetach = tillDetach,
        )
    }
}
