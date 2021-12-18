package icesword.editor.retails

import icesword.editor.MetaTile
import icesword.editor.elastic.ElasticStructurePattern
import icesword.editor.elastic.ElasticStructurePatternOrientation
import icesword.editor.elastic.RectangularMetaTilePattern

val retail4TreeLog = ElasticStructurePattern(
    startingPattern = RectangularMetaTilePattern(
        metaTiles = emptyList(),
        width = 0,
    ),
    repeatingPattern = RectangularMetaTilePattern(
        metaTiles = listOf(Retail4.MetaTiles.TreeLog.center),
        width = 1,
    ),
    endingPattern = RectangularMetaTilePattern(
        metaTiles = emptyList(),
        width = 0,
    ),
    orientation = ElasticStructurePatternOrientation.Vertical,
)


object Retail4 : Retail(naturalIndex = 4) {
    object MetaTiles {
        object TreeLog {
            val center = MetaTile(184)
        }
    }
}
