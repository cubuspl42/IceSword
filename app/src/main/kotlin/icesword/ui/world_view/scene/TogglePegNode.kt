package icesword.ui.world_view.scene

import icesword.editor.Editor
import icesword.editor.entities.CrumblingPeg
import icesword.editor.entities.TogglePeg
import icesword.ui.CanvasNode
import icesword.ui.world_view.EntityNode
import icesword.ui.world_view.EntityNodeB
import icesword.ui.world_view.scene.base.HybridNode
import org.w3c.dom.svg.SVGElement

class TogglePegNode(
    private val editor: Editor,
    private val togglePeg: TogglePeg,
) : HybridNode() {
    override fun buildCanvasNode(
        context: CanvasNodeBuildContext,
    ): CanvasNode = WapSpriteNode(
        editorTextureBank = editor.editorTextureBank,
        textureBank = context.textureBank,
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
            viewBoundingBox = viewTransform.transform(boundingBox),
            tillDetach = tillDetach,
        )
    }
}

fun createTogglePegNode(
    togglePeg: TogglePeg,
): EntityNodeB = object : EntityNodeB {
    override fun build(context: EntityNodeB.BuildContext): EntityNode = context.run {
        EntityNode(
            hybridNode = TogglePegNode(
                editor = editor,
                togglePeg = togglePeg,
            ),
        )
    }
}
