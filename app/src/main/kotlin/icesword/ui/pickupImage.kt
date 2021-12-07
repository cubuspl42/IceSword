package icesword.ui

import TextureBank
import icesword.RezIndex
import icesword.editor.PickupKind
import icesword.frp.Cell
import icesword.frp.Till
import icesword.html.HTMLWidget
import icesword.html.createWrapper

fun createPickupImage(
    rezIndex: RezIndex,
    textureBank: TextureBank,
    pickupKind: PickupKind,
): HTMLWidget {
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
