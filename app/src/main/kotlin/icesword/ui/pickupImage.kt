package icesword.ui

import icesword.RezTextureBank
import icesword.RezIndex
import icesword.editor.PickupKind
import icesword.frp.Cell
import icesword.frp.Till
import icesword.html.HTMLWidget
import icesword.html.createWrapper

fun createPickupImage(
    rezIndex: RezIndex,
    textureBank: RezTextureBank,
    pickupKind: PickupKind,
): HTMLWidget {
    val imageSetId = pickupKind.imageSetId

    val imageMetadata = rezIndex.getImageMetadata(
        imageSetId = imageSetId,
        i = -1,
    )!!

    val texture = textureBank.getImageTexture(imageMetadata.pidPath)!!

    return HTMLWidget.of(
        createWrapper(
            child = Cell.constant(texture.createImage()),
            tillDetach = Till.never,
        ).apply {
            style.apply {
                display = "block"
            }
        },
    )
}
