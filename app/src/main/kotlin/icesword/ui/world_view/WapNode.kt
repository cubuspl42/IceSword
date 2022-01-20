package icesword.ui.world_view

import icesword.EditorTextureBank
import icesword.RezTextureBank
import icesword.editor.DynamicWapSprite
import icesword.frp.Cell
import icesword.ui.CanvasNode
import icesword.ui.world_view.scene.WapSpriteNode

data class WapNode(
    val zOrder: Cell<Int>? = null,
    val canvasNode: CanvasNode,
) {
    companion object {
        fun fromWapSprite(
            editorTextureBank: EditorTextureBank,
            textureBank: RezTextureBank,
            wapSprite: DynamicWapSprite,
            alpha: Double = 1.0,
        ): WapNode = WapNode(
            zOrder = wapSprite.zOrder,
            canvasNode = WapSpriteNode(
                editorTextureBank = editorTextureBank,
                textureBank = textureBank,
                wapSprite = wapSprite,
                alpha = alpha,
            ),
        )
    }

    val effectiveZOrder: Cell<Int>
        get() = zOrder ?: Cell.constant(0)
}


//
//data class EntityNode(
//    val wapNodes: DynamicList<WapNode>,
////    val overlayHybridNode: HybridNode? = null,
////    val viewportCanvasHybridNode: HybridNode? = null,
////    val contentOverlayHybridNode: HybridNode? = null,
//)
//
//fun CanvasNode.asWapEntityNode(): EntityNode =
//    this.asHybridNode().asWapEntityNode()
////
////fun HybridNode.asWapEntityNode(): EntityNode = EntityNode(
////    wapNodes = staticListOf(
////        WapNode(hybridNode = this)
////    )
////)
