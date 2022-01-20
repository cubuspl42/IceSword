package icesword.ui.world_view.scene

import icesword.editor.entities.CrateStack
import icesword.editor.Editor
import icesword.editor.entities.Rope
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.diff
import icesword.frp.map
import icesword.ui.CanvasNode
import icesword.ui.world_view.EntityNode
import icesword.ui.world_view.EntityNodeB
import icesword.ui.world_view.scene.base.HybridNode
import org.w3c.dom.svg.SVGElement

class CrateStackNode(
    private val editor: Editor,
    private val crateStack: CrateStack,
) : HybridNode() {
    override fun buildCanvasNode(
        context: CanvasNodeBuildContext,
    ): CanvasNode = GroupCanvasNode(
        children = DynamicList.diff(
            crateStack.outputStack.map { outputStack ->
                outputStack.crates.map { crate ->
                    WapSpriteNode(
                        editorTextureBank = editor.editorTextureBank,
                        textureBank = context.textureBank,
                        wapSprite = crate.wapSprite,
                    )
                }
            },
        ),
    )

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
            hybridNode = CrateStackNode(
                editor = editor,
                crateStack = crateStack,
            ),
        )
    }
}
