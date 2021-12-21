package icesword.editor.retails

import icesword.editor.MetaTile
import icesword.editor.elastic.ElasticLinearPattern
import icesword.editor.elastic.ElasticLinearPatternOrientation
import icesword.editor.elastic.LinearMetaTilePattern

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
    }
}
