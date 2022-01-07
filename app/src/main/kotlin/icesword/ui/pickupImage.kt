package icesword.ui

import icesword.RezIndex
import icesword.editor.PickupKind
import icesword.frp.Cell.Companion.constant
import icesword.frp.Till
import icesword.html.HTMLWidget
import icesword.html.createWrapper
import org.w3c.dom.Image

fun createPickupImage(
    rezIndex: RezIndex,
    pickupKind: PickupKind,
): HTMLWidget {
    val imageSetId = pickupKind.imageSetId

    val imageMetadata = rezIndex.getImageMetadata(
        imageSetId = imageSetId,
        i = -1,
    )!!

    val imagePath = "images/CLAW/${imageMetadata.pidPath.path.removeSuffix(".PID")}.png"

    return HTMLWidget.of(
        createWrapper(
            child = constant(Image(
                width = imageMetadata.size.width,
                height = imageMetadata.size.height,
            ).apply {
                src = imagePath

                style.apply {
                    setProperty("pointer-events", "none")
                }
            }),
            tillDetach = Till.never,
        ).apply {
            style.apply {
                display = "block"
            }
        },
    )
}
