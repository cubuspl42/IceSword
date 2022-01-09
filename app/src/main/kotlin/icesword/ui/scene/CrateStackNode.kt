package icesword.ui.scene

import icesword.editor.entities.CrateStack
import icesword.editor.Editor
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.map
import icesword.ui.CanvasNode
import icesword.ui.scene.base.HybridNode
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
