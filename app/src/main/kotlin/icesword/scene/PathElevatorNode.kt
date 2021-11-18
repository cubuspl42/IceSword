package icesword.scene

import TextureBank
import icesword.editor.Editor
import icesword.editor.PathElevator
import icesword.frp.DynamicSet
import icesword.html.createSvgGroup
import org.w3c.dom.svg.SVGElement

class PathElevatorNode(
    private val editor: Editor,
    private val pathElevator: PathElevator,
) : HybridNode {
    override fun buildCanvasNode(
        textureBank: TextureBank,
    ): Node = WapSpriteNode(
        textureBank = textureBank,
        wapSprite = pathElevator.wapSprite,
    )

    override fun buildOverlayElement(
        context: HybridNode.OverlayBuildContext,
    ): SVGElement = context.run {
        val boundingBox = pathElevator.wapSprite.boundingBox

        createEntityFrameElement(
            editor = editor,
            svg = svg,
            outer = viewport,
            entity = pathElevator,
            boundingBox = viewTransform.transform(boundingBox),
            tillDetach = tillDetach,
        )
    }
}
