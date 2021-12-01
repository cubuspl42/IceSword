package icesword.editor

import icesword.ImageSetId
import icesword.editor.JeweledTreasureColor.*
import icesword.editor.JeweledTreasureKind.Chalice
import icesword.editor.JeweledTreasureKind.Cross
import icesword.editor.JeweledTreasureKind.Crown
import icesword.editor.JeweledTreasureKind.Gecko
import icesword.editor.JeweledTreasureKind.Ring
import icesword.editor.JeweledTreasureKind.Scepter
import icesword.editor.JeweledTreasureKind.Skull
import icesword.editor.WapObjectPrototype.CoinPrototype

enum class JeweledTreasureKind(
    private val treasureImageId: String,
) {
    Ring("RINGS"),
    Cross("CROSSES"),
    Scepter("SCEPTERS"),
    Gecko("GECKOS"),
    Chalice("CHALICES"),
    Crown("CROWNS"),
    Skull("JEWELEDSKULL");

    fun buildImageSetId(color: JeweledTreasureColor): ImageSetId = ImageSetId(
        fullyQualifiedId = "GAME_IMAGES_TREASURE_${treasureImageId}_${color.colorId}",
    )
}

enum class JeweledTreasureColor {
    Red,
    Green,
    Blue,
    Purple;

    val colorId: String
        get() = this.name.uppercase()
}

enum class PickupKind {
    TreasureGoldbars,
    TreasureRingsRed,
    TreasureRingsGreen,
    TreasureRingsBlue,
    TreasureRingsPurple,
    TreasureNecklace,
    TreasureCrossesRed,
    TreasureCrossesGreen,
    TreasureCrossesBlue,
    TreasureCrossesPurple,
    TreasureSceptersRed,
    TreasureSceptersGreen,
    TreasureSceptersBlue,
    TreasureSceptersPurple,
    TreasureGeckosRed,
    TreasureGeckosGreen,
    TreasureGeckosBlue,
    TreasureGeckosPurple,
    AmmoDeathbag,
    AmmoShot,
    AmmoShotbag,
    PowerupCatnip1,
    PowerupCatnip2,
    HealthBreadwater,
    Health25,
    Health10,
    Health15,
    AmmoMagic5,
    AmmoMagic10,
    AmmoMagic25,
    Mappiece,
    Warp,
    TreasureCoins,
    AmmoDynamite,
    CurseAmmo,
    CurseMagic,
    CurseHealth,
    CurseDeath,
    CurseTreasure,
    CurseFreeze,
    TreasureChalicesRed,
    TreasureChalicesGreen,
    TreasureChalicesBlue,
    TreasureChalicesPurple,
    TreasureCrownsRed,
    TreasureCrownsGreen,
    TreasureCrownsBlue,
    TreasureCrownsPurple,
    TreasureSkullRed,
    TreasureSkullGreen,
    TreasureSkullBlue,
    TreasureSkullPurple,
    PowerupInvisibility,
    PowerupInvincibility,
    PowerupLife,
    PowerupFireSword,
    PowerupLightningSword,
    PowerupFrostSword,
    BossWarp,
    Level2Gem;

    val code: Int
        get() = ordinal + 1

    val imageSetId: ImageSetId? by lazy {
        when (this) {
            TreasureGoldbars -> ImageSetId("GAME_IMAGES_TREASURE_GOLDBARS")

            TreasureRingsRed -> Ring.buildImageSetId(Red)
            TreasureRingsGreen -> Ring.buildImageSetId(Green)
            TreasureRingsBlue -> Ring.buildImageSetId(Blue)
            TreasureRingsPurple -> Ring.buildImageSetId(Purple)

            TreasureNecklace -> null

            TreasureCrossesRed -> Cross.buildImageSetId(Red)
            TreasureCrossesGreen -> Cross.buildImageSetId(Green)
            TreasureCrossesBlue -> Cross.buildImageSetId(Blue)
            TreasureCrossesPurple -> Cross.buildImageSetId(Purple)

            TreasureSceptersRed -> Scepter.buildImageSetId(Red)
            TreasureSceptersGreen -> Scepter.buildImageSetId(Green)
            TreasureSceptersBlue -> Scepter.buildImageSetId(Blue)
            TreasureSceptersPurple -> Scepter.buildImageSetId(Purple)

            TreasureGeckosRed -> Gecko.buildImageSetId(Red)
            TreasureGeckosGreen -> Gecko.buildImageSetId(Green)
            TreasureGeckosBlue -> Gecko.buildImageSetId(Blue)
            TreasureGeckosPurple -> Gecko.buildImageSetId(Purple)

            AmmoDeathbag -> null
            AmmoShot -> null
            AmmoShotbag -> null
            PowerupCatnip1 -> null
            PowerupCatnip2 -> null
            HealthBreadwater -> null
            Health25 -> null
            Health10 -> null
            Health15 -> null
            AmmoMagic5 -> null
            AmmoMagic10 -> null
            AmmoMagic25 -> null
            Mappiece -> null
            Warp -> null
            TreasureCoins -> CoinPrototype.imageSetId
            AmmoDynamite -> null
            CurseAmmo -> null
            CurseMagic -> null
            CurseHealth -> null
            CurseDeath -> null
            CurseTreasure -> null
            CurseFreeze -> null

            TreasureChalicesRed -> Chalice.buildImageSetId(Red)
            TreasureChalicesGreen -> Chalice.buildImageSetId(Green)
            TreasureChalicesBlue -> Chalice.buildImageSetId(Blue)
            TreasureChalicesPurple -> Chalice.buildImageSetId(Purple)

            TreasureCrownsRed -> Crown.buildImageSetId(Red)
            TreasureCrownsGreen -> Crown.buildImageSetId(Green)
            TreasureCrownsBlue -> Crown.buildImageSetId(Blue)
            TreasureCrownsPurple -> Crown.buildImageSetId(Purple)

            TreasureSkullRed -> Skull.buildImageSetId(Red)
            TreasureSkullGreen -> Skull.buildImageSetId(Green)
            TreasureSkullBlue -> Skull.buildImageSetId(Blue)
            TreasureSkullPurple -> Skull.buildImageSetId(Purple)

            PowerupInvisibility -> null
            PowerupInvincibility -> null
            PowerupLife -> null
            PowerupFireSword -> null
            PowerupLightningSword -> null
            PowerupFrostSword -> null
            BossWarp -> null
            Level2Gem -> null
        }
    }
}
