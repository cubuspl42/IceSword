package icesword.editor.retails

import icesword.editor.MetaTile
import icesword.editor.elastic.ElasticStructurePattern
import icesword.editor.elastic.ElasticStructurePatternOrientation
import icesword.editor.elastic.RectangularMetaTilePattern

val retail2PlatformPattern = ElasticStructurePattern(
    startingPattern = RectangularMetaTilePattern(
        metaTiles = listOf(
            Retail2.MetaTiles.platformTopLeft, Retail2.MetaTiles.platformBottomLeft,
        ),
        width = 2,
    ),
    repeatingPattern = RectangularMetaTilePattern(
        metaTiles = listOf(
            Retail2.MetaTiles.platformTop, Retail2.MetaTiles.platformBottom,
        ),
        width = 2,
    ),
    endingPattern = RectangularMetaTilePattern(
        metaTiles = listOf(
            Retail2.MetaTiles.platformTopRight, Retail2.MetaTiles.platformBottomRight,
        ),
        width = 2,
    ),
    orientation = ElasticStructurePatternOrientation.Horizontal,
)

object Retail2 : Retail(naturalIndex = 2) {
    object MetaTiles {
        val platformTopLeft = MetaTile(46)

        val platformTop = MetaTile(29)

        val platformTopRight = MetaTile(310)

        val platformBottomLeft = MetaTile(37)

        val platformBottom = MetaTile(39)

        val platformBottomRight = MetaTile(312)
    }
}
