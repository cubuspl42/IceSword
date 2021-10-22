@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.RezIndex
import icesword.frp.Cell
import icesword.frp.map
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import kotlinx.serialization.UseSerializers

class WapObjectStem(
    rezIndex: RezIndex,
    wapObjectPrototype: WapObjectPrototype,
    position: Cell<IntVec2>,
) {
    val imageMetadata = rezIndex.getImageMetadata(
        imageSetId = wapObjectPrototype.imageSetId,
        i = -1,
    )!!

    val boundingBox: Cell<IntRect> =
        position.map {
            val offset = imageMetadata.offset
            val size = imageMetadata.size
            val topLeft = it + offset - (size / 2).toVec2()

            IntRect(
                position = topLeft,
                size = size,
            )
        }

    override fun toString(): String =
        "WapObjectStem()"
}
