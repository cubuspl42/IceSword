@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.ImageMetadata
import icesword.ImageSetId
import icesword.RezIndex
import icesword.frp.Cell
import icesword.frp.map
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import kotlinx.serialization.UseSerializers

class WapSprite(
    rezIndex: RezIndex,
    imageSetId: ImageSetId,
    position: Cell<IntVec2>,
) {
    val imageMetadata = rezIndex.getImageMetadata(
        imageSetId = imageSetId,
        i = -1,
    )!!

    val boundingBox: Cell<IntRect> =
        position.map {
            calculateWapSpriteBounds(
                imageMetadata = imageMetadata,
                position = it
            )
        }

    override fun toString(): String = "WapSprite()"
}

fun calculateWapSpriteBounds(
    imageMetadata: ImageMetadata,
    position: IntVec2,
): IntRect {
    val offset = imageMetadata.offset
    val size = imageMetadata.size
    val topLeft = position + offset - (size / 2).toVec2()

    return IntRect(
        position = topLeft,
        size = size,
    )
}
