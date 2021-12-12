@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.ImageMetadata
import icesword.ImageSetId
import icesword.RezIndex
import icesword.frp.Cell
import icesword.frp.map
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import kotlinx.serialization.UseSerializers

class WapSprite(
    val size: IntSize,
    val boundingBox: Cell<IntRect>,
    val imageMetadata: ImageMetadata?,
) {
    companion object {
        fun fromImageSet(
            rezIndex: RezIndex,
            imageSetId: ImageSetId,
            position: Cell<IntVec2>,
        ): WapSprite = rezIndex.getImageMetadata(
            imageSetId = imageSetId,
            i = -1,
        )?.let { imageMetadata ->
            fromImageMetadata(
                imageMetadata = imageMetadata,
                position = position,
            )
        } ?: placeholder(
            position = position,
        )

        fun fromImageMetadata(
            imageMetadata: ImageMetadata,
            position: Cell<IntVec2>,
        ): WapSprite {
            val size: IntSize = imageMetadata.size

            val boundingBox: Cell<IntRect> =
                position.map {
                    calculateWapSpriteBounds(
                        imageMetadata = imageMetadata,
                        position = it
                    )
                }

            return WapSprite(
                size = size,
                boundingBox = boundingBox,
                imageMetadata = imageMetadata,
            )
        }

        private fun placeholder(
            position: Cell<IntVec2>,
        ): WapSprite {
            val size = IntSize(32, 32)
            return WapSprite(
                size = size,
                boundingBox = position.map {
                    size.toRect(it - size.div(2).toVec2())
                },
                imageMetadata = null,
            )
        }
    }


    val center = boundingBox.map { it.center }

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
