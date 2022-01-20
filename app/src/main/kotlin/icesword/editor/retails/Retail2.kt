package icesword.editor.retails

import icesword.ImageSetId
import icesword.editor.MetaTile
import icesword.editor.TileGenerator
import icesword.editor.TileGeneratorContext
import icesword.editor.entities.ElevatorPrototype
import icesword.editor.entities.TogglePegPrototype
import icesword.editor.entities.elastic.ElasticLinearPattern
import icesword.editor.entities.elastic.ElasticLinearPatternOrientation
import icesword.editor.entities.elastic.LinearMetaTilePattern
import icesword.editor.entities.encode
import icesword.editor.retails.Retail2.MetaTiles
import icesword.editor.retails.Retail2.MetaTiles.SinglePile
import icesword.editor.retails.Retail2.MetaTiles.Tower
import icesword.wwd.Wwd

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

private val towerTileGenerator = TileGenerator.forwardAll(
    Tower.CannonLeft.topLeft,
    Tower.CannonLeft.topRight,
    Tower.CannonLeft.centerLeft,
    Tower.CannonLeft.centerRight,
    Tower.CannonLeft.bottomLeft,
    Tower.CannonLeft.bottomRight,

    Tower.CannonRight.topLeft,
    Tower.CannonRight.topRight,
    Tower.CannonRight.centerLeft,
    Tower.CannonRight.centerRight,
    Tower.CannonRight.bottomLeft,
    Tower.CannonRight.bottomRight,

    Tower.window,
)

private val retailTileGenerator = object : TileGenerator {
    override fun buildTile(context: TileGeneratorContext): Int? = context.run {
        val metaTiles = MetaTiles
        val doublePile = MetaTiles.DoublePile

        // What's the difference between 38/39?

        when {
            // Platform / goo

            containsAll(metaTiles.platformTop, MetaTiles.Goo.left) -> 313
            containsAll(metaTiles.platformTop, MetaTiles.Goo.center) -> 314
            containsAll(metaTiles.platformTop, MetaTiles.Goo.right) -> 315

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

            // Platform / single pile

            containsAll(metaTiles.platformTop, SinglePile.topInner) -> 3
            containsAll(metaTiles.platformBottom, SinglePile.center) -> 9

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

        val death = MetaTile(107)

        object DoublePile {
            val top = MetaTile(113)

            val core = MetaTile(48)

            val bottom = MetaTile(52)
        }

        object SinglePile {
            val topOuter = MetaTile(1)

            val topInner = MetaTile(18)

            val center = MetaTile(14)

            val bottom = MetaTile(52)

            val bottomInner = MetaTile(20)

            val bottomOuter = MetaTile(23)
        }

        object Tower {
            val core = MetaTile(77, z = MetaTileZOder.TowerCore.ordinal)

            val window = MetaTile(85)

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

            object CannonLeft {
                val topLeft = MetaTile(83)
                val topRight = MetaTile(84)
                val centerLeft = MetaTile(88)
                val centerRight = MetaTile(89)
                val bottomLeft = MetaTile(92)
                val bottomRight = MetaTile(93)
            }

            object CannonRight {
                val topLeft = MetaTile(86)
                val topRight = MetaTile(87)
                val centerLeft = MetaTile(90)
                val centerRight = MetaTile(91)
                val bottomLeft = MetaTile(94)
                val bottomRight = MetaTile(95)
            }
        }

        object Goo {
            val left = MetaTile(313)

            val center = MetaTile(314)

            val right = MetaTile(315)
        }
    }

    override val tileGenerator: TileGenerator = TileGenerator.chained(
        towerTileGenerator,
        retailTileGenerator,
    )

    val togglePegPrototype = TogglePegPrototype(
        imageSetId = ImageSetId(fullyQualifiedId = "LEVEL2_IMAGES_PEGSLIDER"),
        shortImageSetId = "LEVEL_PEGSLIDER",
    )

    override val elevatorPrototype = ElevatorPrototype(
        elevatorImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL2_IMAGES_ELEVATOR",
        ),
        wwdObjectPrototype = Wwd.Object_.empty().copy(
            logic = encode("Elevator"),
            imageSet = encode("LEVEL_ELEVATOR"),
        ),
    )
}
