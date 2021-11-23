package icesword.scene

import TextureBank
import icesword.RezIndex
import icesword.editor.Editor
import icesword.editor.Enemy
import icesword.editor.PickupKind
import icesword.frp.Cell
import icesword.frp.DynamicSet
import icesword.frp.Till
import icesword.frp.map
import icesword.geometry.DynamicTransform
import icesword.geometry.IntVec2
import icesword.html.createSvgForeignObject
import icesword.html.createSvgGroup
import icesword.html.createTableContainer
import icesword.html.createWrapper
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

        fun createPickupImage(pickupKind: PickupKind): HTMLElement {
            val imageMetadata = rezIndex.getImageMetadata(
                imageSetId = pickupKind.imageSetId!!,
                i = -1,
            )!!

            val texture = textureBank.getImageTexture(imageMetadata)!!

            return createWrapper(
                child = Cell.constant(texture.createImage()),
                tillDetach = Till.never,
            ).apply {
                style.apply {
                    display = "inline-block"

                    width = 36.px.value
                    height = 36.px.value
                }
            }
        }

        val pickupsPreview = createSvgForeignObject(
            svg = svg,
            transform = viewTransform * DynamicTransform.translate(pickupsPreviewPosition),
            width = 128,
            height = 128,
            child = createTableContainer(
                borderSpacing = 4.px,
                rows = listOf(
                    // TODO: Display actual enemy's pickups
                    listOf(
                        createPickupImage(PickupKind.TreasureCoins),
                        createPickupImage(PickupKind.TreasureRingsGreen),
                        createPickupImage(PickupKind.TreasureCrossesGreen),
                    ),
                    listOf(
                        createPickupImage(PickupKind.TreasureSceptersGreen),
                        createPickupImage(PickupKind.TreasureGeckosGreen),
                        createPickupImage(PickupKind.TreasureChalicesGreen),
                    ),
                    listOf(
                        createPickupImage(PickupKind.TreasureCrownsGreen),
                        createPickupImage(PickupKind.TreasureSkullGreen),
                        createPickupImage(PickupKind.TreasureRingsGreen),
                    ),
                )
            ),
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
