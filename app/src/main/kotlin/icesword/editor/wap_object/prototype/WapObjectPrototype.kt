package icesword.editor.wap_object.prototype

import icesword.ImageSetId
import icesword.editor.entities.encode
import icesword.wwd.Wwd
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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

    @Serializable
    object HealthPotion1 : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_HEALTH_POTION1",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("HealthPowerup"),
            imageSet = encode("GAME_HEALTH_POTION1"),
        )
    }

    @Serializable
    object HealthPotion2 : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_HEALTH_POTION2",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("HealthPowerup"),
            imageSet = encode("GAME_HEALTH_POTION2"),
        )
    }

    @Serializable
    object HealthPotion3 : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_HEALTH_POTION3",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("HealthPowerup"),
            imageSet = encode("GAME_HEALTH_POTION3"),
        )
    }

    @Serializable
    object AmmoShot : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_AMMO_SHOT",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("AmmoPowerup"),
            imageSet = encode("GAME_AMMO_SHOT"),
        )
    }

    @Serializable
    object AmmoShotbag : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_AMMO_SHOTBAG",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("AmmoPowerup"),
            imageSet = encode("GAME_AMMO_SHOTBAG"),
        )
    }

    @Serializable
    object AmmoDeathbag : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_AMMO_DEATHBAG",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("AmmoPowerup"),
            imageSet = encode("GAME_AMMO_DEATHBAG"),
        )
    }

    @Serializable
    object Dynamite : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_DYNAMITE",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("MagicPowerup"),
            imageSet = encode("GAME_DYNAMITE"),
        )
    }

    @Serializable
    object MagicGlow : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_MAGIC_GLOW",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("SpecialPowerup"),
            imageSet = encode("GAME_MAGIC_GLOW"),
        )
    }

    @Serializable
    object MagicStarGlow : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_MAGIC_STARGLOW",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("SpecialPowerup"),
            imageSet = encode("GAME_MAGIC_STARGLOW"),
        )
    }

    @Serializable
    object MagicClaw : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_MAGICCLAW",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("SpecialPowerup"),
            imageSet = encode("GAME_MAGICCLAW"),
        )
    }

    @Serializable
    object ExtraLife : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_POWERUPS_EXTRALIFE",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("SpecialPowerup"),
            imageSet = encode("GAME_POWERUPS_EXTRALIFE"),
        )
    }

    @Serializable
    object Invulnerable : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_POWERUPS_INVULNERABLE",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("SpecialPowerup"),
            imageSet = encode("GAME_POWERUPS_INVULNERABLE"),
        )
    }

    @Serializable
    object Ghost : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_POWERUPS_GHOST",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("SpecialPowerup"),
            imageSet = encode("GAME_POWERUPS_GHOST"),
        )
    }

    @Serializable
    object Catnip1 : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_CATNIPS_NIP1",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("HealthPowerup"),
            imageSet = encode("GAME_CATNIPS_NIP1"),
        )
    }

    @Serializable
    object Catnip2 : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_CATNIPS_NIP2",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("HealthPowerup"),
            imageSet = encode("GAME_CATNIPS_NIP2"),
        )
    }

    @Serializable
    object FireSword : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_POWERUPS_FIRESWORD",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("SpecialPowerup"),
            imageSet = encode("GAME_POWERUPS_FIRESWORD"),
        )
    }

    @Serializable
    object LightningSword : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_POWERUPS_LIGHTNINGSWORD",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("SpecialPowerup"),
            imageSet = encode("GAME_POWERUPS_LIGHTNINGSWORD"),
        )
    }

    @Serializable
    object IceSword : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_POWERUPS_ICESWORD",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("SpecialPowerup"),
            imageSet = encode("GAME_POWERUPS_ICESWORD"),
        )
    }

    @Serializable
    object Checkpoint : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_CHECKPOINTFLAG",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("Checkpoint"),
            imageSet = encode("GAME_CHECKPOINTFLAG"),
        )
    }

    @Serializable
    object FirstSuperCheckpoint : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_SUPERCHECKPOINT",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("FirstSuperCheckpoint"),
            imageSet = encode("GAME_SUPERCHECKPOINT"),
        )
    }

    @Serializable
    object MapPiece : WapObjectPrototype() {
        @Transient
        override val imageSetId: ImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_MAPPIECE",
        )

        @Transient
        override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
            logic = encode("EndOfLevelPowerup"),
            imageSet = encode("GAME_MAPPIECE"),
        )
    }
}