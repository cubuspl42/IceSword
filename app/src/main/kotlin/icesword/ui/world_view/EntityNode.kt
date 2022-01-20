package icesword.ui.world_view

import icesword.EditorTextureBank
import icesword.RezIndex
import icesword.RezTextureBank
import icesword.editor.Editor
import icesword.geometry.DynamicTransform
import icesword.ui.world_view.scene.base.HybridNode

class EntityNode(
    val hybridNode: HybridNode,
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

fun EntityNodeB.buildHybridNode(
    rezIndex: RezIndex,
    textureBank: RezTextureBank,
    editorTextureBank: EditorTextureBank,
    editor: Editor,
    viewTransform: DynamicTransform,
): HybridNode =
    build(
        EntityNodeB.BuildContext(
            rezIndex = rezIndex,
            textureBank = textureBank,
            editorTextureBank = editorTextureBank,
            editor = editor,
            viewTransform = viewTransform,
        ),
    ).hybridNode
