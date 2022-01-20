package icesword.ui.world_view.scene

import icesword.editor.entities.CrumblingPeg
import icesword.editor.Editor
import icesword.editor.entities.CrateStack
import icesword.ui.CanvasNode
import icesword.ui.world_view.EntityNode
import icesword.ui.world_view.EntityNodeB
import icesword.ui.world_view.scene.base.HybridNode
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

fun createCrumblingPegNode(
    crumblingPeg: CrumblingPeg,
): EntityNodeB = object : EntityNodeB {
    override fun build(context: EntityNodeB.BuildContext): EntityNode = context.run {
        EntityNode(
            hybridNode = CrumblingPegNode(
                editor = editor,
                crumblingPeg = crumblingPeg,
            ),
        )
    }
}
