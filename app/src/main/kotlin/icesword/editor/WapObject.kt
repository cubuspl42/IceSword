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
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import org.khronos.webgl.Uint8Array

private fun encode(text: String): DataStreamObj.ByteString {
    val encodedText: ByteArray = text.encodeToByteArray()
    return DataStreamObj.ByteString(
        Uint8Array(encodedText.toTypedArray())
    )
}

@Serializable
sealed class WapObjectPrototype {
    abstract val imageSetId: ImageSetId

    abstract val wwdObjectPrototype: Wwd.Object_

    @Serializable
    object RopePrototype : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL3_IMAGES_ROPE",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("AniRope"),
            imageSet = encode("LEVEL_ROPE"),
            speedX = 1750,
        )
    }

    @Serializable
    object CrumblingPegPrototype : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL3_IMAGES_CRUMBLINPEG1",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("CrumblingPeg"),
            imageSet = encode("LEVEL_CRUMBLINPEG1"),
        )
    }


    @Serializable
    object CoinPrototype : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_TREASURE_COINS",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("TreasurePowerup"),
            imageSet = encode("GAME_TREASURE_COINS"),
        )
    }
}

class WapObject(
    rezIndex: RezIndex,
     val wapObjectPrototype: WapObjectPrototype,
    initialPosition: IntVec2,
) :
    Entity() {

    companion object {
        fun load(
            rezIndex: RezIndex,
            data: WapObjectData,
        ): WapObject =
            WapObject(
                rezIndex = rezIndex,
                wapObjectPrototype = data.prototype,
                initialPosition = data.position,
            )
    }

    private val _imageMetadata = rezIndex.getImageMetadata(
        imageSetId = wapObjectPrototype.imageSetId,
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
        val position = position.sample()

        return wapObjectPrototype.wwdObjectPrototype.copy(
            x = position.x,
            y = position.y,
        )
    }

    fun toData(): WapObjectData =
        WapObjectData(
            prototype = wapObjectPrototype,
            position = position.sample(),
        )

    override fun toString(): String =
        "WapObject()"
}


@Serializable
data class WapObjectData(
    val prototype: WapObjectPrototype,
    val position: IntVec2,
)
