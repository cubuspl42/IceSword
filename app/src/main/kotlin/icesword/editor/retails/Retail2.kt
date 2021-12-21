package icesword.editor.retails

import icesword.editor.MetaTile
import icesword.editor.TileGenerator
import icesword.editor.TileGeneratorContext
import icesword.editor.elastic.ElasticLinearPattern
import icesword.editor.elastic.ElasticLinearPatternOrientation
import icesword.editor.elastic.LinearMetaTilePattern
import icesword.editor.retails.Retail2.MetaTiles
import icesword.editor.retails.Retail2.MetaTiles.Tower

val retail2PlatformPattern = ElasticLinearPattern(
    startingPattern = LinearMetaTilePattern(
        metaTiles = listOf(
            MetaTiles.platformTopLeft, MetaTiles.platformBottomLeft,
        ),
        width = 2,
    ),
    repeatingPattern = LinearMetaTilePattern(
        metaTiles = listOf(
            MetaTiles.platformTop, MetaTiles.platformBottom,
        ),
        width = 2,
    ),
    endingPattern = LinearMetaTilePattern(
        metaTiles = listOf(
            MetaTiles.platformTopRight, MetaTiles.platformBottomRight,
        ),
        width = 2,
    ),
    orientation = ElasticLinearPatternOrientation.Horizontal,
)

val doublePilePattern = run {
    val doublePile = MetaTiles.DoublePile

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
        val metaTiles = MetaTiles
        val doublePile = MetaTiles.DoublePile

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

            // Tower / tower

            containsAll(Tower.Platform.topCenter, Tower.Column.left) -> 508

            else -> null
        }
    }
}

object Retail2 : Retail(naturalIndex = 2) {
    enum class MetaTileZOder {
        Tower,
        TowerCore,
        TowerSide,
    }

    object MetaTiles {
        val platformTopLeft = MetaTile(28)

        val platformTop = MetaTile(29)

        val platformTopRight = MetaTile(310)

        val platformBottomLeft = MetaTile(37)

        val platformBottom = MetaTile(39)

        val platformBottomRight = MetaTile(312)

        val battlement = MetaTile(67)

        object DoublePile {
            val top = MetaTile(113)

            val core = MetaTile(48)

            val bottom = MetaTile(52)
        }

        object Tower {
            val core = MetaTile(77, z = MetaTileZOder.TowerCore.ordinal)

            object Platform {
                val topLeftOuter = MetaTile(70, z = MetaTileZOder.Tower.ordinal)
                val topLeftInner = MetaTile(71, z = MetaTileZOder.Tower.ordinal)
                val topCenter = MetaTile(72, z = MetaTileZOder.Tower.ordinal)
                val topRightInner = MetaTile(73, z = MetaTileZOder.Tower.ordinal)
                val topRightOuter = MetaTile(74, z = MetaTileZOder.Tower.ordinal)
                val bottomLeftOuter = MetaTile(75, z = MetaTileZOder.Tower.ordinal)
                val bottomLeftInner = MetaTile(76, z = MetaTileZOder.Tower.ordinal)
                val bottomRightInner = MetaTile(78, z = MetaTileZOder.Tower.ordinal)
                val bottomRightOuter = MetaTile(79, z = MetaTileZOder.Tower.ordinal)
            }

            object Column {
                val topLeft = MetaTile(80, z = MetaTileZOder.TowerSide.ordinal)
                val topCenter = MetaTile(81, z = MetaTileZOder.Tower.ordinal)
                val topRight = MetaTile(82, z = MetaTileZOder.TowerSide.ordinal)
                val left = MetaTile(96, z = MetaTileZOder.TowerSide.ordinal)
                val right = MetaTile(97, z = MetaTileZOder.TowerSide.ordinal)
            }
        }
    }

    override val tileGenerator: TileGenerator = retailTileGenerator
}
