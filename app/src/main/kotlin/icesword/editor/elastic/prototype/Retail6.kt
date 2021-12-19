package icesword.editor.elastic.prototype

import icesword.editor.ElasticGenerator
import icesword.editor.MetaTile
import icesword.editor.elastic.ElasticStructurePattern
import icesword.editor.elastic.ElasticStructurePatternOrientation
import icesword.editor.elastic.RectangularMetaTilePattern
import icesword.editor.retails.LadderElasticGenerator
import icesword.editor.retails.Retail
import icesword.editor.retails.Retail6
import icesword.geometry.IntSize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Retail6Fence")
object Retail6FencePrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(4, 2)

    private val generator = ElasticStructurePattern(
        startingPattern = RectangularMetaTilePattern(
            metaTiles = listOf(
                MetaTile.None,
                Retail6.MetaTiles.Fence.bottomLeft,
            ),
            width = 2,
        ),
        repeatingPattern = RectangularMetaTilePattern(
            metaTiles = listOf(
                Retail6.MetaTiles.Fence.top,
                Retail6.MetaTiles.Fence.bottomCenter,
            ),
            width = 2,
        ),
        endingPattern = RectangularMetaTilePattern(
            metaTiles = listOf(
                MetaTile.None,
                Retail6.MetaTiles.Fence.bottomRightInner,
                MetaTile.None,
                Retail6.MetaTiles.Fence.bottomRightOuter,
            ),
            width = 2,
        ),
        orientation = ElasticStructurePatternOrientation.Horizontal,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}

@Serializable
@SerialName("Retail6HorizontalRoof")
object Retail6HorizontalRoofPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(4, 2)

    private val generator = ElasticStructurePattern(
        startingPattern = RectangularMetaTilePattern(
            metaTiles = listOf(
                Retail6.MetaTiles.HorizontalRoof.topLeft,
                Retail6.MetaTiles.HorizontalRoof.bottomLeft,
            ),
            width = 2,
        ),
        repeatingPattern = RectangularMetaTilePattern(
            metaTiles = listOf(
                Retail6.MetaTiles.HorizontalRoof.topCenter,
                Retail6.MetaTiles.HorizontalRoof.bottomCenter,
            ),
            width = 2,
        ),
        endingPattern = RectangularMetaTilePattern(
            metaTiles = listOf(
                Retail6.MetaTiles.HorizontalRoof.topRightInner,
                Retail6.MetaTiles.HorizontalRoof.bottomRightInner,
                Retail6.MetaTiles.HorizontalRoof.topRightOuter,
                Retail6.MetaTiles.HorizontalRoof.bottomRightOuter,
            ),
            width = 2,
        ),
        orientation = ElasticStructurePatternOrientation.Horizontal,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}
