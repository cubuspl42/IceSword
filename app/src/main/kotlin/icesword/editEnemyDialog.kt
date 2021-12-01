package icesword

import TextureBank
import icesword.editor.Enemy
import icesword.editor.PickupKind
import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.Till
import icesword.frp.units
import icesword.html.DynamicStyleDeclaration
import icesword.html.HTMLWidget
import icesword.html.HTMLWidgetB
import icesword.html.createButtonWb
import icesword.html.createColumnWb
import icesword.html.createGrid
import icesword.html.createTextWb
import icesword.html.createWrapper
import icesword.html.flatMap
import icesword.html.map
import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.px
import org.w3c.dom.HTMLElement


fun createEditEnemyDialog(
    rezIndex: RezIndex,
    textureBank: TextureBank,
    enemy: Enemy,
): HTMLWidgetB<Dialog> {
    fun createPickupImage(pickupKind: PickupKind): HTMLWidget? =
        pickupKind.imageSetId?.let { imageSetId ->
            val imageMetadata = rezIndex.getImageMetadata(
                imageSetId = imageSetId,
                i = -1,
            )!!

            val texture = textureBank.getImageTexture(imageMetadata)!!

            HTMLWidget.of(
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

    return createButtonWb(
        style = DynamicStyleDeclaration(
            alignSelf = constant(Align.flexEnd),
        ),
        text = "✕",
    ).flatMap { closeButton ->
        createColumnWb(
            style = DynamicStyleDeclaration(
                backgroundColor = constant(Color("#d1d1d1")),
                padding = constant("16px"),
                fontFamily = constant("sans-serif"),
            ),
            verticalGap = 8.px,
            children = listOf(
                closeButton,
                createTextWb(
                    text = constant("Enemy: ${enemy.imageSetId.fullyQualifiedId}"),
                ),
                createGrid(
                    style = DynamicStyleDeclaration(
                        alignSelf = constant(Align.center),
                        justifyItems = constant(Align.center),
                        alignItems = constant(Align.center),
                    ),
                    gap = 8.px,
                    children = PickupKind.values().mapNotNull {
                        createPickupImage(it)
                    },
                ),
                createButtonWb(
                    style = DynamicStyleDeclaration(
                        alignSelf = constant(Align.flexStart),
                    ),
                    text = "Save",
                ),
            ),
        ).map { content ->
            Dialog(
                content = content,
                onClose = closeButton.onPressed.units(),
            )
        }
    }
}
