package icesword.ui.world_view.scene

import icesword.editor.entities.CrateStack
import icesword.editor.Editor
import icesword.frp.dynamic_list.map
import icesword.ui.world_view.WapNode
import icesword.ui.world_view.EntityNode
import icesword.ui.world_view.EntityNodeB
import icesword.ui.world_view.scene.base.HybridNode
import org.w3c.dom.svg.SVGElement

class CrateStackNode(
    private val editor: Editor,
    private val crateStack: CrateStack,
) : HybridNode() {
    override fun buildOverlayElement(
        context: HybridNode.OverlayBuildContext,
    ): SVGElement = context.run {
        createEntityFrameElement(
            editor = editor,
            svg = svg,
            outer = viewport,
            entity = crateStack,
            viewBoundingBox = viewTransform.transform(crateStack.boundingBox),
            tillDetach = tillDetach,
        )
    }
}

fun createCrateStackNode(crateStack: CrateStack): EntityNodeB = object : EntityNodeB {
    override fun build(context: EntityNodeB.BuildContext): EntityNode = context.run {
        EntityNode(
            wapNodes = crateStack.outputCrates.map {
                WapNode.fromWapSprite(
                    editorTextureBank = editor.editorTextureBank,
                    textureBank = context.textureBank,
                    wapSprite = it.wapSprite,
                )
            },
            overlayNode = CrateStackNode(
                editor = editor,
                crateStack = crateStack,
            ),
        )
    }
}
