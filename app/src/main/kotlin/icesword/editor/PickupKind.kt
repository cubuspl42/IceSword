package icesword.editor

import icesword.ImageSetId
import icesword.editor.WapObjectPrototype.ChaliceTreasurePrototype
import icesword.editor.WapObjectPrototype.CoinPrototype
import icesword.editor.WapObjectPrototype.CrossTreasurePrototype
import icesword.editor.WapObjectPrototype.CrownTreasurePrototype
import icesword.editor.WapObjectPrototype.GeckoTreasurePrototype
import icesword.editor.WapObjectPrototype.RingTreasurePrototype
import icesword.editor.WapObjectPrototype.ScepterTreasurePrototype
import icesword.editor.WapObjectPrototype.SkullTreasurePrototype

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
            TreasureGoldbars -> null
            TreasureRingsRed -> null
            TreasureRingsGreen -> RingTreasurePrototype.imageSetId
            TreasureRingsBlue -> null
            TreasureRingsPurple -> null
            TreasureNecklace -> null
            TreasureCrossesRed -> null
            TreasureCrossesGreen -> CrossTreasurePrototype.imageSetId
            TreasureCrossesBlue -> null
            TreasureCrossesPurple -> null
            TreasureSceptersRed -> null
            TreasureSceptersGreen -> ScepterTreasurePrototype.imageSetId
            TreasureSceptersBlue -> null
            TreasureSceptersPurple -> null
            TreasureGeckosRed -> null
            TreasureGeckosGreen -> GeckoTreasurePrototype.imageSetId
            TreasureGeckosBlue -> null
            TreasureGeckosPurple -> null
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
            TreasureChalicesRed -> null
            TreasureChalicesGreen -> ChaliceTreasurePrototype.imageSetId
            TreasureChalicesBlue -> null
            TreasureChalicesPurple -> null
            TreasureCrownsRed -> null
            TreasureCrownsGreen -> CrownTreasurePrototype.imageSetId
            TreasureCrownsBlue -> null
            TreasureCrownsPurple -> null
            TreasureSkullRed -> null
            TreasureSkullGreen -> SkullTreasurePrototype.imageSetId
            TreasureSkullBlue -> null
            TreasureSkullPurple -> null
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
