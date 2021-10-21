@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.ImageSetId
import icesword.RezIndex
import icesword.TILE_SIZE
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.map
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.DataStreamObj
import icesword.wwd.Wwd
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.khronos.webgl.Uint8Array


class Rope(
    rezIndex: RezIndex,
    initialPosition: IntVec2,
) :
    Entity() {

    companion object {
        const val radius: Int = 16

         val imageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL3_IMAGES_ROPE",
        )

        fun load(
            rezIndex: RezIndex,
            data: RopeData,
        ): Rope =
            Rope(
                rezIndex = rezIndex,
                initialPosition = data.position,
            )
    }

    private val _imageMetadata = rezIndex.getImageMetadata(
        imageSetId = imageSetId,
        i = -1,
    )!!

    private val _position = MutCell(initialPosition)

    override val position: Cell<IntVec2>
        get() = _position

    val boundingBox: Cell<IntRect> =
        position.map {
            val offset = _imageMetadata.offset
            val size = _imageMetadata.size
            val topLeft = it + offset - (size / 2).toVec2()

            IntRect(
                position = topLeft,
                size = size,
            )
        }

    override fun isSelectableAt(worldPoint: IntVec2): Boolean {
        val rect = boundingBox.sample()
        return rect.contains(worldPoint)
    }

    // TODO: Deduplicate!
    override val tileOffset: Cell<IntVec2> =
        position.map { it.divRound(TILE_SIZE) }

    override fun setPosition(newPosition: IntVec2) {
        _position.set(newPosition)
    }

    fun export(): Wwd.Object_ {
        fun encode(text: String): DataStreamObj.ByteString {
            val encodedText: ByteArray = text.encodeToByteArray()
            return DataStreamObj.ByteString(
                Uint8Array(encodedText.toTypedArray())
            )
        }

        val position = position.sample()

        return Wwd.Object_.empty().copy(
            logic = encode("AniRope"),
            imageSet = encode("LEVEL_ROPE"),
            x = position.x,
            y = position.y,
            speedX = 1750,
        )
    }

    fun toData(): RopeData =
        RopeData(
            position = position.sample(),
        )

    override fun toString(): String =
        "Rope()"
}


@Serializable
data class RopeData(
    val position: IntVec2,
)
