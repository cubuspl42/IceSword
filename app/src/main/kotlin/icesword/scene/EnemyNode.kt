package icesword.scene

import TextureBank
import icesword.RezIndex
import icesword.editor.Editor
import icesword.editor.Enemy
import icesword.editor.PickupKind
import icesword.frp.Cell
import icesword.frp.DynamicSet
import icesword.frp.Till
import icesword.frp.dynamic_list.map
import icesword.frp.dynamic_list.mapIndexed
import icesword.frp.map
import icesword.geometry.DynamicTransform
import icesword.geometry.IntVec2
import icesword.html.DynamicStyleDeclaration
import icesword.html.HTMLWidget
import icesword.html.createGridDl
import icesword.html.createSvgForeignObject
import icesword.html.createSvgGroup
import icesword.html.createTableContainer
import icesword.html.createWrapper
import icesword.html.resolve
import kotlinx.css.Align
import kotlinx.css.px
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement

class EnemyNode(
    private val rezIndex: RezIndex,
    private val textureBank: TextureBank,
    private val editor: Editor,
    private val enemy: Enemy,
) : HybridNode {
    override fun buildCanvasNode(
        textureBank: TextureBank,
    ): CanvasNode = WapSpriteNode(
        textureBank = textureBank,
        wapSprite = enemy.wapSprite,
    )

    override fun buildOverlayElement(
        context: HybridNode.OverlayBuildContext,
    ): SVGElement = context.run {
        val boundingBox = enemy.wapSprite.boundingBox

        val movementRangeOverlay =
            createHorizontalMovementRangeOverlay(
                svg = svg,
                viewport = viewport,
                viewTransform = viewTransform,
                entityMovementRange = enemy,
                tillDetach = tillDetach,
            )

        val frame = createEntityFrameElement(
            editor = editor,
            svg = svg,
            outer = viewport,
            entity = enemy,
            boundingBox = viewTransform.transform(boundingBox),
            tillDetach = tillDetach,
        )

        val pickupsPreviewPosition = enemy.wapSprite.boundingBox.map {
            it.topRight + IntVec2(4, 0)
        }

        fun createPickupImage(pickupKind: PickupKind): HTMLWidget {
            val imageSetId = pickupKind.imageSetId

            val imageMetadata = rezIndex.getImageMetadata(
                imageSetId = imageSetId,
                i = -1,
            )!!

            val texture = textureBank.getImageTexture(imageMetadata)!!

            return HTMLWidget.of(
                createWrapper(
                    child = Cell.constant(texture.createImage()),
                    tillDetach = Till.never,
                ).apply {
                    style.apply {
                        display = "inline-block"
                    }
                },
            )
        }

        val pickupsPreview = createSvgForeignObject(
            svg = svg,
            transform = viewTransform * DynamicTransform.translate(pickupsPreviewPosition),
            width = 128,
            height = 128,
            child = createGridDl(
                style = DynamicStyleDeclaration(
                    alignSelf = Cell.constant(Align.center),
                    justifyItems = Cell.constant(Align.center),
                    alignItems = Cell.constant(Align.center),
                ),
                gap = 8.px,
                children = enemy.pickups.map { pickupKind ->
                    createPickupImage(
                        pickupKind = pickupKind,
                    )
                },
            ).build(tillDetach).resolve() as HTMLElement,
            tillDetach = tillDetach,
        )

        createSvgGroup(
            svg = svg,
            children = DynamicSet.of(setOf(
                movementRangeOverlay,
                frame,
                pickupsPreview,
            )),
            tillDetach = tillDetach,
        )
    }
}
