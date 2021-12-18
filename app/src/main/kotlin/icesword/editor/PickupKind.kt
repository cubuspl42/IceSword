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
import icesword.editor.wap_object.prototype.WapObjectPrototype.CoinPrototype

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

    val imageSetId: ImageSetId by lazy {
        // FIXME
        val defaultImageSetId = ImageSetId(
            fullyQualifiedId = "GAME_IMAGES_MONOLITH",
        )

        when (this) {
            TreasureGoldbars -> ImageSetId(
                fullyQualifiedId = "GAME_IMAGES_TREASURE_GOLDBARS",
            )

            TreasureRingsRed -> Ring.buildImageSetId(Red)
            TreasureRingsGreen -> Ring.buildImageSetId(Green)
            TreasureRingsBlue -> Ring.buildImageSetId(Blue)
            TreasureRingsPurple -> Ring.buildImageSetId(Purple)

            TreasureNecklace -> defaultImageSetId

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

            AmmoDeathbag -> defaultImageSetId
            AmmoShot -> defaultImageSetId
            AmmoShotbag -> defaultImageSetId
            PowerupCatnip1 -> defaultImageSetId
            PowerupCatnip2 -> defaultImageSetId
            HealthBreadwater -> defaultImageSetId
            Health25 -> defaultImageSetId
            Health10 -> defaultImageSetId
            Health15 -> defaultImageSetId
            AmmoMagic5 -> defaultImageSetId
            AmmoMagic10 -> defaultImageSetId
            AmmoMagic25 -> defaultImageSetId
            Mappiece -> defaultImageSetId
            Warp -> defaultImageSetId
            TreasureCoins -> CoinPrototype.imageSetId
            AmmoDynamite -> defaultImageSetId
            CurseAmmo -> defaultImageSetId
            CurseMagic -> defaultImageSetId
            CurseHealth -> defaultImageSetId
            CurseDeath -> defaultImageSetId
            CurseTreasure -> defaultImageSetId
            CurseFreeze -> defaultImageSetId

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

            PowerupInvisibility -> defaultImageSetId
            PowerupInvincibility -> defaultImageSetId
            PowerupLife -> defaultImageSetId
            PowerupFireSword -> defaultImageSetId
            PowerupLightningSword -> defaultImageSetId
            PowerupFrostSword -> defaultImageSetId
            BossWarp -> defaultImageSetId
            Level2Gem -> defaultImageSetId
        }
    }
}
