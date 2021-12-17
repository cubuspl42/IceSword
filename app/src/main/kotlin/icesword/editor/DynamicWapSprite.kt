@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.ImageMetadata
import icesword.ImageSetId
import icesword.RezIndex
import icesword.editor.retails.Retail
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
) {
    companion object {
        fun fromImageMetadata(
            imageMetadata: ImageMetadata,
            position: IntVec2,
        ): WapSprite {
            val boundingBox: IntRect = calculateWapSpriteBounds(
                imageMetadata = imageMetadata,
                position = position
            )

            return WapSprite(
                boundingBox = boundingBox,
                imageMetadata = imageMetadata,
            )
        }

        fun placeholder(position: IntVec2): WapSprite {
            val size = IntSize(32, 32)
            return WapSprite(
                boundingBox = size.toRect(position - size.div(2).toVec2()),
                imageMetadata = null,
            )
        }
    }
}

class DynamicWapSprite(
    val wapSprite: Cell<WapSprite>,
) {
    companion object {
//        fun fromWapObjectProps(
//            rezIndex: RezIndex,
//            retail: Retail,
//            props: Cell<WapObjectPropsData?>,
//        ): DynamicWapSprite = fromImageSetDynamic(
//            rezIndex = rezIndex,
//            imageSetId = props.map { propsData ->
//                propsData?.let {
//                    expandImageSetId(
//                        retail = retail,
//                        shortImageSetId = it.imageSet,
//                    )
//                } ?: ImageSetId.empty
//            },
//            position = props.map {
//                IntVec2(it.x, it.y)
//            }
//        )

        fun fromImageSet(
            rezIndex: RezIndex,
            imageSetId: ImageSetId,
            position: Cell<IntVec2>,
            i: Int = -1,
        ): DynamicWapSprite = fromImageSetDynamic(
                rezIndex = rezIndex,
                imageSetId = Cell.constant(imageSetId),
                position = position,
                i = i
            )

        fun fromImageSetDynamic(
            rezIndex: RezIndex,
            imageSetId: Cell<ImageSetId>,
            position: Cell<IntVec2>,
            i: Int,
        ): DynamicWapSprite =
            imageSetId.map { imageSetId ->
                rezIndex.getImageMetadata(
                    imageSetId = imageSetId,
                    i = i,
                )?.let { imageMetadata ->
                    fromImageMetadata(
                        imageMetadata = imageMetadata,
                        position = position,
                    )
                } ?: placeholder(
                    position = position,
                )
            }.switch()


        fun fromImageMetadata(
            imageMetadata: ImageMetadata,
            position: Cell<IntVec2>,
        ): DynamicWapSprite = DynamicWapSprite(
            wapSprite = position.map {
                WapSprite.fromImageMetadata(
                    imageMetadata = imageMetadata,
                    position = it,
                )
            },
        )

        private fun placeholder(
            position: Cell<IntVec2>,
        ): DynamicWapSprite = DynamicWapSprite(
            wapSprite = position.map {
                WapSprite.placeholder(position = it)
            },
        )
    }

    val boundingBox: Cell<IntRect> = wapSprite.map { it.boundingBox }

    val imageMetadata: Cell<ImageMetadata?> = wapSprite.map { it.imageMetadata }

    val center = boundingBox.map { it.center }

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
