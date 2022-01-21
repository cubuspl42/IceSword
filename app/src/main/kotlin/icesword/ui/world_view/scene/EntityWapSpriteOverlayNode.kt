package icesword.ui.world_view.scene

import icesword.editor.DynamicWapSprite
import icesword.editor.Editor
import icesword.editor.entities.Entity
import icesword.editor.entities.Rope
import icesword.editor.entities.TogglePeg
import icesword.editor.entities.WapObject
import icesword.editor.entities.Warp
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.concatWith
import icesword.frp.dynamic_list.staticListOf
import icesword.frp.map
import icesword.geometry.DynamicTransform
import icesword.ui.svg.createSvgCross
import icesword.ui.world_view.EntityNode
import icesword.ui.world_view.EntityNodeB
import icesword.ui.world_view.WapNode
import icesword.ui.world_view.scene.base.HybridNode
import kotlinx.css.Color
import org.w3c.dom.svg.SVGElement

class EntityWapSpriteOverlayNode(
    private val editor: Editor,
    private val entity: Entity,
    private val wapSprite: DynamicWapSprite,
) : HybridNode() {
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
            wapNodes = staticListOf(
                WapNode.fromWapSprite(
                    editorTextureBank = editorTextureBank,
                    textureBank = textureBank,
                    wapSprite = wapSprite,
                )
            ),
            overlayNode = EntityWapSpriteOverlayNode(
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
): EntityNodeB = object : EntityNodeB {
    override fun build(context: EntityNodeB.BuildContext): EntityNode = context.run {
        EntityNode(
            wapNodes = staticListOf(
                WapNode.fromWapSprite(
                    editorTextureBank = editorTextureBank,
                    textureBank = textureBank,
                    wapSprite = warp.wapSprite,
                )
            ),
            overlayNode = GroupNode(
                children = staticListOf(
                    EntityWapSpriteOverlayNode(
                        editor = editor,
                        entity = warp,
                        wapSprite = warp.wapSprite,
                    ),
                ).concatWith(
                    DynamicList.ofSingle(
                        editor.isEntitySelected(warp).map {
                            if (it) buildTargetCross() else null
                        },
                    ),
                ),
            ),
        )
    }

    private fun buildTargetCross() = object : HybridNode() {
        override fun buildOverlayElement(
            context: OverlayBuildContext,
        ): SVGElement = context.run {
            createSvgCross(
                svg = svg,
                transform = DynamicTransform.translate(
                    viewTransform.transform(warp.targetPosition),
                ),
                color = Color.red,
                tillDetach = tillDetach,
            )
        }
    }
}

fun createRopeNode(
    rope: Rope,
): EntityNodeB = createEntityWapSpriteNode(
    entity = rope,
    wapSprite = rope.wapSprite,
)

fun createTogglePegNode(
    togglePeg: TogglePeg,
): EntityNodeB = createEntityWapSpriteNode(
    entity = togglePeg,
    wapSprite = togglePeg.wapSprite,
)
