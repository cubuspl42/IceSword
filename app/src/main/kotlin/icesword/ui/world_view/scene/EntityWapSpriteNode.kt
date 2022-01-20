package icesword.ui.world_view.scene

import icesword.editor.DynamicWapSprite
import icesword.editor.Editor
import icesword.editor.entities.Entity
import icesword.editor.entities.TogglePeg
import icesword.editor.entities.WapObject
import icesword.editor.entities.Warp
import icesword.ui.CanvasNode
import icesword.ui.world_view.EntityNode
import icesword.ui.world_view.EntityNodeB
import icesword.ui.world_view.scene.base.HybridNode
import org.w3c.dom.svg.SVGElement

class EntityWapSpriteNode(
    private val editor: Editor,
    private val entity: Entity,
    private val wapSprite: DynamicWapSprite,
) : HybridNode() {
    override fun buildCanvasNode(
        context: CanvasNodeBuildContext,
    ): CanvasNode = WapSpriteNode(
        editorTextureBank = editor.editorTextureBank,
        textureBank = context.textureBank,
        wapSprite = wapSprite,
    )

    override fun buildOverlayElement(
        context: OverlayBuildContext,
    ): SVGElement = context.run {
        val boundingBox = wapSprite.boundingBox

        createEntityFrameElement(
            editor = editor,
            svg = svg,
            outer = viewport,
            entity = entity,
            viewBoundingBox = viewTransform.transform(boundingBox),
            tillDetach = tillDetach,
        )
    }
}

private fun createEntityWapSpriteNode(
    entity: Entity,
    wapSprite: DynamicWapSprite,
): EntityNodeB = object : EntityNodeB {
    override fun build(context: EntityNodeB.BuildContext): EntityNode = context.run {
        EntityNode(
            hybridNode = EntityWapSpriteNode(
                editor = editor,
                entity = entity,
                wapSprite = wapSprite,
            ),
        )
    }
}

fun createWapObjectNode(
    wapObject: WapObject,
): EntityNodeB = createEntityWapSpriteNode(
    entity = wapObject,
    wapSprite = wapObject.sprite,
)

fun createWarpNode(
    warp: Warp,
): EntityNodeB = createEntityWapSpriteNode(
    entity = warp,
    wapSprite = warp.wapSprite,
)
