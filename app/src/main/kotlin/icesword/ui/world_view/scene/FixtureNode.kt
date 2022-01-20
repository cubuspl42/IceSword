package icesword.ui.world_view.scene

import icesword.EditorTextureBank
import icesword.RezTextureBank
import icesword.editor.Editor
import icesword.editor.entities.ElasticProduct
import icesword.editor.entities.Fixture
import icesword.editor.entities.FixtureProduct
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.map
import icesword.frp.dynamic_list.staticListOf
import icesword.ui.CanvasNode
import icesword.ui.world_view.EntityNode
import icesword.ui.world_view.EntityNodeB
import icesword.ui.world_view.WapNode
import icesword.ui.world_view.scene.base.HybridNode
import org.w3c.dom.svg.SVGElement

fun createFixtureNode(
    fixture: Fixture,
): EntityNodeB = object : EntityNodeB {
    override fun build(context: EntityNodeB.BuildContext): EntityNode = context.run {
        EntityNode(
            wapNodes = createFixtureProductWapNodes(
                editorTextureBank = editorTextureBank,
                textureBank = textureBank,
                fixtureProduct = fixture.product,
            ),
            overlayNode = FixtureOverlayNode(
                editor = editor,
                fixture = fixture,
            ),
        )
    }
}

class FixtureProductNode(
    private val fixtureProduct: FixtureProduct,
    private val alpha: Double,
) : HybridNode() {
    override fun buildCanvasNode(
        context: CanvasNodeBuildContext,
    ): CanvasNode = GroupCanvasNode(
        children = fixtureProduct.wapObjectSprites.map { wapSprite ->
            WapSpriteNode(
                editorTextureBank = context.editorTextureBank,
                textureBank = context.textureBank,
                wapSprite = wapSprite,
                alpha = alpha,
            )
        }
    )
}

fun createFixtureProductWapNodes(
    editorTextureBank: EditorTextureBank,
    textureBank: RezTextureBank,
    fixtureProduct: FixtureProduct,
    alpha: Double = 1.0,
): DynamicList<WapNode> = fixtureProduct.wapObjectSprites.map { wapSprite ->
    WapNode.fromWapSprite(
        editorTextureBank = editorTextureBank,
        textureBank = textureBank,
        wapSprite = wapSprite,
        alpha = alpha,
    )
}

class FixtureOverlayNode(
    private val editor: Editor,
    private val fixture: Fixture,
) : HybridNode() {
    override fun buildOverlayElement(
        context: OverlayBuildContext,
    ): SVGElement = context.run {
        val viewBoundingBox = viewTransform.transform(
            fixture.product.boundingBox,
        )

        createEntityFrameElement(
            editor = editor,
            svg = svg,
            outer = viewport,
            entity = fixture,
            viewBoundingBox = viewBoundingBox,
            tillDetach = tillDetach,
        )
    }
}
