@file:UseSerializers(
    IntVec2Serializer::class,
    WwdObjectSerializer::class,
)

package icesword.editor.entities

import icesword.ImageSetId
import icesword.RezIndex
import icesword.editor.DynamicWapSprite
import icesword.editor.IntVec2Serializer
import icesword.editor.WwdObjectSerializer
import icesword.editor.retails.Retail
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.DataStreamObj
import icesword.wwd.Wwd
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.khronos.webgl.Uint8Array

fun encode(text: String): DataStreamObj.ByteString {
    val encodedText: ByteArray = text.encodeToByteArray()
    return DataStreamObj.ByteString(
        Uint8Array(encodedText.toTypedArray())
    )
}

interface WapObjectExportable {
    fun exportWapObjects(): List<Wwd.Object_> =
        listOf(exportWapObject())

    fun exportWapObject(): Wwd.Object_ {
        throw NotImplementedError()
    }
}

class WapObject(
    rezIndex: RezIndex,
    retail: Retail,
    initialProps: WapObjectPropsData,
    initialPosition: IntVec2,
) : Entity(), WapObjectExportable {
    companion object {
        fun load(
            rezIndex: RezIndex,
            retail: Retail,
            data: WapObjectData,
        ): WapObject =
            WapObject(
                rezIndex = rezIndex,
                retail = retail,
                initialProps = data.wwdObject,
                initialPosition = data.position,
            )
    }

    override val entityPosition = EntityPixelPosition(
        initialPosition = initialPosition,
    )

    val props = WapObjectProps(initialProps = initialProps)

    val sprite = DynamicWapSprite.fromImageSet(
        rezIndex = rezIndex,
        imageSetId = expandImageSetId(
            retail = retail,
            shortImageSetId = initialProps.imageSet,
        ),
        position = position,
    )

    override fun isSelectableIn(area: IntRect): Boolean {
        val hitBox = sprite.boundingBox.sample()
        return hitBox.overlaps(area)
    }

    override fun exportWapObject(): Wwd.Object_ {
        val wwdObject = props.toData().toWwdObject()
        val position = position.sample()

        return wwdObject.copy(
            x = position.x,
            y = position.y,
        )
    }

    fun toData(): WapObjectData =
        WapObjectData(
            wwdObject = props.toData(),
            position = position.sample(),
        )

    override fun toString(): String =
        "WapObject()"
}

@Serializable
data class WapObjectData(
    val wwdObject: WapObjectPropsData,
    val position: IntVec2,
)

fun expandImageSetId(
    retail: Retail,
    shortImageSetId: String,
): ImageSetId = ImageSetId(
    fullyQualifiedId = when {
        shortImageSetId == "" -> ""
        shortImageSetId.startsWith("GAME_") -> shortImageSetId.replace("GAME_", "GAME_IMAGES_")
        shortImageSetId.startsWith("LEVEL_") -> shortImageSetId.replace("LEVEL_",
            "LEVEL${retail.naturalIndex}_IMAGES_")
        else -> throw UnsupportedOperationException("Cannot expand short imageset ID: $shortImageSetId")
    }
)
