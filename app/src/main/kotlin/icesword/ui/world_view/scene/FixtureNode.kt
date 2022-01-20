package icesword.ui.world_view.scene

import icesword.editor.Editor
import icesword.editor.entities.Fixture
import icesword.editor.entities.FixtureProduct
import icesword.frp.dynamic_list.map
import icesword.frp.dynamic_list.staticListOf
import icesword.ui.CanvasNode
import icesword.ui.world_view.scene.base.HybridNode
import org.w3c.dom.svg.SVGElement

class FixtureNode(
    editor: Editor,
    fixture: Fixture,
) : GroupNode(
    children = staticListOf(
        FixtureProductNode(
            fixtureProduct = fixture.product,
            alpha = 1.0,
        ),
        FixtureOverlayNode(
            editor = editor,
            fixture = fixture,
        ),
    )
)

class FixtureProductNode(
    private val fixtureProduct: FixtureProduct,
    private val alpha: Double,
) : HybridNode() {
    override fun buildCanvasNode(
        context: CanvasNodeBuildContext,
    ): CanvasNode = GroupCanvasNode(
        children = fixtureProduct.wapObjectSprites.map {
            WapSpriteNode(
                editorTextureBank = context.editorTextureBank,
                textureBank = context.textureBank,
                wapSprite = it,
                alpha = alpha,
            )
        }
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
