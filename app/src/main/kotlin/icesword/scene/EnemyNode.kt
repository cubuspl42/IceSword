package icesword.scene

import TextureBank
import icesword.editor.Editor
import icesword.editor.Enemy
import icesword.frp.DynamicSet
import icesword.geometry.Transform
import icesword.html.createSvgGroup
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
    ): SVGElement = context.run {
        val boundingBox = enemy.wapSprite.boundingBox

        val movementRangeOverlay =
            createHorizontalMovementRangeOverlay(
                svg = svg,
                viewport = viewport,
                viewTransform = viewTransform,
                entityMovementRange = enemy,
                tillDetach = tillDetach,
            )

        val frame = createEntityFrameElement(
            editor = editor,
            svg = svg,
            outer = viewport,
            entity = enemy,
            boundingBox = viewTransform.transform(boundingBox),
            tillDetach = tillDetach,
        )

        createSvgGroup(
            svg = svg,
            children = DynamicSet.of(setOf(
                movementRangeOverlay,
                frame,
            )),
            tillDetach = tillDetach,
        )
    }
}
