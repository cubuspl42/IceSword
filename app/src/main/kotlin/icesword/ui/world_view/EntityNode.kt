package icesword.ui.world_view

import icesword.EditorTextureBank
import icesword.RezIndex
import icesword.RezTextureBank
import icesword.editor.Editor
import icesword.frp.dynamic_list.DynamicList
import icesword.geometry.DynamicTransform
import icesword.ui.world_view.scene.base.HybridNode

class EntityNode(
    val wapNodes: DynamicList<WapNode> = DynamicList.empty(),
    val overlayNode: HybridNode? = null,
    val hybridViewportCanvasNode: HybridNode? = null,
    val hybridContentOverlayNode: HybridNode? = null,
)

interface EntityNodeB {
    data class BuildContext(
        val rezIndex: RezIndex,
        val textureBank: RezTextureBank,
        val editorTextureBank: EditorTextureBank,
        val editor: Editor,
        val viewTransform: DynamicTransform,
    )

    fun build(context: BuildContext): EntityNode
}
