package icesword.scene

import icesword.RezTextureBank
import icesword.editor.CrateStack
import icesword.editor.Editor
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.map
import org.w3c.dom.svg.SVGElement

class CrateStackNode(
    private val editor: Editor,
    private val crateStack: CrateStack,
) : HybridNode() {
    override fun buildCanvasNode(
        textureBank: RezTextureBank,
    ): CanvasNode = GroupCanvasNode(
        children = DynamicList.diff(
            crateStack.outputStack.map { outputStack ->
                outputStack.crates.map { crate ->
                    WapSpriteNode(
                        editorTextureBank = editor.editorTextureBank,
                        textureBank = textureBank,
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
            boundingBox = viewTransform.transform(crateStack.boundingBox),
            tillDetach = tillDetach,
        )
    }
}
