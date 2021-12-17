@file:UseSerializers(
    IntVec2Serializer::class,
    WwdObjectSerializer::class,
)

package icesword.editor

import icesword.ImageSetId
import icesword.RezIndex
import icesword.editor.retails.Retail
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.DataStreamObj
import icesword.wwd.Wwd
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import org.khronos.webgl.Uint8Array

fun encode(text: String): DataStreamObj.ByteString {
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
    object EmptyPrototype : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty()
    }

    @Serializable
    object StackedCratesPrototype : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL3_IMAGES_CRATES",
        )

        @Transient
        override val wwdObjectPrototype = Wwd.Object_.empty()
    }

    @Serializable
    object Level1CrumblingPegPrototype : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL1_IMAGES_CRUMBLINGPEG",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("CrumblingPeg"),
            imageSet = encode("LEVEL_CRUMBLINGPEG"),
        )
    }

    @Serializable
    object Level3CrumblingPegPrototype : WapObjectPrototype() {
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
            logic = encode("GlitterlessPowerup"),
            imageSet = encode("GAME_TREASURE_COINS"),
        )
    }

    @Serializable
    sealed class TreasurePrototype : WapObjectPrototype() {
        companion object {
            // TODO: Support all colors
            private const val color = "GREEN"
        }

        abstract val treasureImageId: String

        final override val imageSetId: ImageSetId
            get() = ImageSetId(
                fullyQualifiedId = "GAME_IMAGES_TREASURE_${treasureImageId}_${color}",
            )

        final override val wwdObjectPrototype: Wwd.Object_
            get() = Wwd.Object_.empty().copy(
                logic = encode("TreasurePowerup"),
                imageSet = encode("GAME_TREASURE_${treasureImageId}_${color}"),
            )
    }

    @Serializable
    @SerialName("icesword.editor.WapObjectPrototype.CrossTreasure")
    object CrossTreasurePrototype : TreasurePrototype() {
        @Transient
        override val treasureImageId: String = "CROSSES"
    }

    @Serializable
    object ScepterTreasurePrototype : TreasurePrototype() {
        @Transient
        override val treasureImageId: String = "SCEPTERS"
    }

    @Serializable
    object CrownTreasurePrototype : TreasurePrototype() {
        @Transient
        override val treasureImageId: String = "CROWNS"
    }

    @Serializable
    object ChaliceTreasurePrototype : TreasurePrototype() {
        @Transient
        override val treasureImageId: String = "CHALICES"
    }

    @Serializable
    object RingTreasurePrototype : TreasurePrototype() {
        @Transient
        override val treasureImageId: String = "RINGS"
    }

    @Serializable
    object GeckoTreasurePrototype : TreasurePrototype() {
        @Transient
        override val treasureImageId: String = "GECKOS"
    }

    @Serializable
    object SkullTreasurePrototype : TreasurePrototype() {
        @Transient
        override val treasureImageId: String = "JEWELEDSKULL"
    }

    @Serializable
    object RobberThiefPrototype : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL3_IMAGES_ROBBERTHIEF",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("RobberThief"),
            imageSet = encode("LEVEL_ROBBERTHIEF"),
        )
    }

    @Serializable
    object CutThroatPrototype : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL3_IMAGES_CUTTHROAT",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("RobberThief"),
            imageSet = encode("LEVEL_CUTTHROAT"),
        )
    }

    @Serializable
    object OfficerPrototype : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL1_IMAGES_OFFICER",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("Officer"),
            imageSet = encode("LEVEL_OFFICER"),
        )
    }

    @Serializable
    object SoldierPrototype : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL1_IMAGES_SOLDIER",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("Soldier"),
            imageSet = encode("LEVEL_SOLDIER"),
        )
    }

    @Serializable
    object Level1RatPrototype : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL1_IMAGES_RAT",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("Rat"),
            imageSet = encode("LEVEL_RAT"),
        )
    }

    @Serializable
    object Level3RatPrototype : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL3_IMAGES_RAT",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("Rat"),
            imageSet = encode("LEVEL_RAT"),
        )
    }

    @Serializable
    object Level3ElevatorPrototype : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL3_IMAGES_ELEVATOR1",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("Elevator"),
            imageSet = encode("LEVEL_ELEVATOR1"),
        )
    }

    @Serializable
    object PathElevatorPrototype : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL3_IMAGES_ELEVATOR1",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("PathElevator"),
            imageSet = encode("LEVEL_ELEVATOR1"),
        )
    }

    // TODO: Support FLOORSPIKES2 (underground spikes)
    @Serializable
    object FloorSpikePrototype : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL3_IMAGES_FLOORSPIKES1",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("FloorSpike"),
            imageSet = encode("LEVEL_FLOORSPIKES1"),
        )
    }
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
