@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.ImageMetadata
import icesword.ImageSetId
import icesword.RezIndex
import icesword.frp.Cell
import icesword.frp.map
import icesword.frp.switchMap
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import kotlinx.serialization.UseSerializers

data class WapSprite(
    val boundingBox: IntRect,
    val imageMetadata: ImageMetadata?,
    val zOrder: Int = 0,
) {
    companion object {
        fun fromImageMetadata(
            imageMetadata: ImageMetadata,
            position: IntVec2,
            z: Int = 0,
        ): WapSprite {
            val boundingBox: IntRect = calculateWapSpriteBounds(
                imageMetadata = imageMetadata,
                position = position
            )

            return WapSprite(
                boundingBox = boundingBox,
                imageMetadata = imageMetadata,
                zOrder = z,
            )
        }

        fun placeholder(
            position: IntVec2,
            z: Int = 0,
        ): WapSprite {
            val size = IntSize(32, 32)
            return WapSprite(
                boundingBox = size.toRect(position - size.div(2).toVec2()),
                imageMetadata = null,
                zOrder = z,
            )
        }
    }
}

class DynamicWapSprite(
    val wapSprite: Cell<WapSprite>,
) {
    companion object {
        fun fromImageSet(
            rezIndex: RezIndex,
            imageSetId: ImageSetId,
            position: Cell<IntVec2>,
            i: Int = -1,
            z: Cell<Int> = Cell.constant(0),
        ): DynamicWapSprite = fromImageSetDynamic(
            rezIndex = rezIndex,
            imageSetId = Cell.constant(imageSetId),
            position = position,
            i = i,
            z = z,
        )

        private fun fromImageSetDynamic(
            rezIndex: RezIndex,
            imageSetId: Cell<ImageSetId>,
            position: Cell<IntVec2>,
            i: Int,
            z: Cell<Int> = Cell.constant(0),
        ): DynamicWapSprite =
            imageSetId.map { imageSetIdNow ->
                rezIndex.getImageMetadata(
                    imageSetId = imageSetIdNow,
                    i = i,
                )?.let { imageMetadata ->
                    fromImageMetadata(
                        imageMetadata = imageMetadata,
                        position = position,
                        z = z,
                    )
                } ?: placeholder(
                    position = position,
                    z = z,
                )
            }.switch()


        fun fromImageMetadata(
            imageMetadata: ImageMetadata,
            position: Cell<IntVec2>,
            z: Cell<Int> = Cell.constant(0),
        ): DynamicWapSprite = DynamicWapSprite(
            wapSprite = Cell.map2(position, z) { positionNow, zNow ->
                WapSprite.fromImageMetadata(
                    imageMetadata = imageMetadata,
                    position = positionNow,
                    z = zNow,
                )
            }
        )

        private fun placeholder(
            position: Cell<IntVec2>,
            z: Cell<Int> = Cell.constant(0),
        ): DynamicWapSprite = DynamicWapSprite(
            wapSprite = Cell.map2(position, z) { positionNow, zNow ->
                WapSprite.placeholder(
                    position = positionNow,
                    z = zNow,
                )
            }
        )
    }

    val boundingBox: Cell<IntRect> = wapSprite.map { it.boundingBox }

    val imageMetadata: Cell<ImageMetadata?> = wapSprite.map { it.imageMetadata }

    val zOrder = wapSprite.map { it.zOrder }

    val center = boundingBox.map { it.center }

    fun isSelectableIn(area: IntRect): Boolean {
        val hitBox = boundingBox.sample()
        return hitBox.overlaps(area)
    }

    override fun toString(): String = "DynamicWapSprite()"
}

fun Cell<DynamicWapSprite>.switch() = DynamicWapSprite(
    wapSprite = this.switchMap { it.wapSprite }
)

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
