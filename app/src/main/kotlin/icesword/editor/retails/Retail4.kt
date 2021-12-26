package icesword.editor.retails

import icesword.editor.ChainedTileGenerator
import icesword.editor.ForwardTileGenerator
import icesword.editor.MetaTile
import icesword.editor.TileGenerator
import icesword.editor.TileGeneratorContext
import icesword.editor.elastic.ElasticLinearPattern
import icesword.editor.elastic.ElasticLinearPatternOrientation
import icesword.editor.elastic.LinearMetaTilePattern
import icesword.editor.retails.Retail4.MetaTiles.NaturalPlatform
import icesword.editor.retails.Retail4.MetaTiles.Tree

val retail4TreeLog = ElasticLinearPattern(
    startingPattern = LinearMetaTilePattern(
        metaTiles = emptyList(),
        width = 0,
    ),
    repeatingPattern = LinearMetaTilePattern(
        metaTiles = listOf(Retail4.MetaTiles.TreeLog.center),
        width = 1,
    ),
    endingPattern = LinearMetaTilePattern(
        metaTiles = emptyList(),
        width = 0,
    ),
    orientation = ElasticLinearPatternOrientation.Vertical,
)

private val naturalPlatformTileGenerator = ChainedTileGenerator(
    listOf(
        ForwardTileGenerator(NaturalPlatform.center),
        ForwardTileGenerator(NaturalPlatform.leftInner),
        ForwardTileGenerator(NaturalPlatform.rightInner),
    )
)

private val retailTileGenerator = object : TileGenerator {
    override fun buildTile(context: TileGeneratorContext): Int? = context.run {
        when {
            // Natural platform / tree
            containsAll(NaturalPlatform.topCenter, Tree.trunk) -> 185

            else -> null
        }
    }
}

object Retail4 : Retail(naturalIndex = 4) {
    object MetaTiles {
        object TreeLog {
            val center = MetaTile(184)
        }

        object WoodenPlatform {
            val left = MetaTile(155)

            val center = MetaTile(159)

            val right = MetaTile(160)
        }

        object Ladder : LadderPattern {
            override val top = MetaTile(181)

            override val center = MetaTile(182)

            override val bottom = MetaTile(183)
        }

        object NaturalPlatform {
            val topLeftOuterWide = MetaTile(101)

            val topLeftOuterNarrow = MetaTile(133)

            val topLeftInnerNarrow = MetaTile(601)

            val topCenter = MetaTile(104)

            val topRightInnerNarrow = MetaTile(137)

            val topRightOuterWide = MetaTile(600)

            val topRightOuterNarrow = MetaTile(138)

            val leftOuter = MetaTile(139)

            val leftInner = MetaTile(108)

            val center = MetaTile(227)

            val rightInner = MetaTile(111)

            val rightOuter = MetaTile(144)

            val bottomLeft = MetaTile(123)

            val bottomCenter = MetaTile(141)

            val bottomRight = MetaTile(128)
        }

        object Tree {
            val trunk = MetaTile(184)
        }

        object Goo {
            val left = MetaTile(228)

            val center = MetaTile(229)

            val right = MetaTile(230)
        }

        val death = MetaTile(175)
    }

    override val tileGenerator = ChainedTileGenerator(
        listOf(
            naturalPlatformTileGenerator,
            retailTileGenerator,
        )
    )
}
