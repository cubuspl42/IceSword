package icesword.editor.retails

import icesword.editor.MetaTile
import icesword.editor.TileGenerator
import icesword.editor.TileGeneratorContext
import icesword.editor.elastic.ElasticLinearPattern
import icesword.editor.elastic.ElasticLinearPatternOrientation
import icesword.editor.elastic.LinearMetaTilePattern

val retail2PlatformPattern = ElasticLinearPattern(
    startingPattern = LinearMetaTilePattern(
        metaTiles = listOf(
            Retail2.MetaTiles.platformTopLeft, Retail2.MetaTiles.platformBottomLeft,
        ),
        width = 2,
    ),
    repeatingPattern = LinearMetaTilePattern(
        metaTiles = listOf(
            Retail2.MetaTiles.platformTop, Retail2.MetaTiles.platformBottom,
        ),
        width = 2,
    ),
    endingPattern = LinearMetaTilePattern(
        metaTiles = listOf(
            Retail2.MetaTiles.platformTopRight, Retail2.MetaTiles.platformBottomRight,
        ),
        width = 2,
    ),
    orientation = ElasticLinearPatternOrientation.Horizontal,
)

val doublePilePattern = run {
    val doublePile = Retail2.MetaTiles.DoublePile

    ElasticLinearPattern(
        startingPattern = LinearMetaTilePattern(
            metaTiles = listOf(doublePile.top),
            width = 1,
        ),
        repeatingPattern = LinearMetaTilePattern(
            metaTiles = listOf(doublePile.core),
            width = 1,
        ),
        endingPattern = LinearMetaTilePattern(
            metaTiles = listOf(doublePile.bottom),
            width = 1,
        ),
        orientation = ElasticLinearPatternOrientation.Vertical,
    )
}

val retail2TowerTop = ElasticLinearPattern(
    startingPattern = LinearMetaTilePattern(
        metaTiles = listOf(
            MetaTile(68), MetaTile(70), MetaTile(75),
            MetaTile(68), MetaTile(71), MetaTile(76),
        ),
        width = 3,
    ),
    repeatingPattern = LinearMetaTilePattern(
        metaTiles = listOf(
            MetaTile(68), MetaTile(72), MetaTile(77),
        ),
        width = 3,
    ),
    endingPattern = LinearMetaTilePattern(
        metaTiles = listOf(
            MetaTile(68), MetaTile(73), MetaTile(78),
            MetaTile(68), MetaTile(74), MetaTile(79),
        ),
        width = 3,
    ),
    orientation = ElasticLinearPatternOrientation.Horizontal,
)

private val retailTileGenerator = object : TileGenerator {
    override fun buildTile(context: TileGeneratorContext): Int? = context.run {
        val metaTiles = Retail2.MetaTiles
        val doublePile = Retail2.MetaTiles.DoublePile

        // What's the difference between 38/39?

        when {
            // Platform overlapping with double pile

            containsAll(metaTiles.platformTopLeft, doublePile.top) -> 28
            containsAll(metaTiles.platformTopLeft, doublePile.core) -> 46
            containsAll(metaTiles.platformTop, doublePile.top) -> 35
            containsAll(metaTiles.platformTop, doublePile.core) -> 32
            containsAll(metaTiles.platformTopRight, doublePile.top) -> 310 // Best one can do
            containsAll(metaTiles.platformTopRight, doublePile.core) -> 310

            containsAll(metaTiles.platformBottomLeft, doublePile.core) -> 53
            containsAll(metaTiles.platformBottomLeft, doublePile.bottom) -> 37
            containsAll(metaTiles.platformBottom, doublePile.core) -> 38
            containsAll(metaTiles.platformBottom, doublePile.bottom) -> 38 // Best one can do
            containsAll(metaTiles.platformBottomRight, doublePile.core) -> 311
            containsAll(metaTiles.platformBottomRight, doublePile.bottom) -> 312

            else -> null
        }
    }
}

object Retail2 : Retail(naturalIndex = 2) {
    object MetaTiles {
        val platformTopLeft = MetaTile(28)

        val platformTop = MetaTile(29)

        val platformTopRight = MetaTile(310)

        val platformBottomLeft = MetaTile(37)

        val platformBottom = MetaTile(39)

        val platformBottomRight = MetaTile(312)

        object DoublePile {
            val top = MetaTile(113)

            val core = MetaTile(48)

            val bottom = MetaTile(52)
        }
    }

    override val tileGenerator: TileGenerator = retailTileGenerator
}
