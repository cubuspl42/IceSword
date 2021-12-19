package icesword.editor.elastic.prototype

import icesword.editor.ElasticGenerator
import icesword.editor.MetaTile
import icesword.editor.elastic.ElasticStructurePattern
import icesword.editor.elastic.ElasticStructurePatternOrientation
import icesword.editor.elastic.RectangularMetaTilePattern
import icesword.editor.retails.LadderElasticGenerator
import icesword.editor.retails.Retail
import icesword.editor.retails.Retail5
import icesword.geometry.IntSize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Retail5MetalPlatform")
object Retail5MetalPlatformPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(5, 2)

    private val generator = ElasticStructurePattern(
        startingPattern = RectangularMetaTilePattern(
            metaTiles = listOf(
                Retail5.MetaTiles.MetalPlatform.topLeftOuter,
                MetaTile.None,
                Retail5.MetaTiles.MetalPlatform.topLeftInner,
                Retail5.MetaTiles.MetalPlatform.bottomLeft,
            ),
            width = 2,
        ),
        repeatingPattern = RectangularMetaTilePattern(
            metaTiles = listOf(
                Retail5.MetaTiles.MetalPlatform.topCenter,
                Retail5.MetaTiles.MetalPlatform.bottomCenter,
            ),
            width = 2,
        ),
        endingPattern = RectangularMetaTilePattern(
            metaTiles = listOf(
                Retail5.MetaTiles.MetalPlatform.topRightInner,
                Retail5.MetaTiles.MetalPlatform.bottomCenter,
                Retail5.MetaTiles.MetalPlatform.topRightOuter,
                Retail5.MetaTiles.MetalPlatform.bottomRight,
            ),
            width = 2,
        ),
        orientation = ElasticStructurePatternOrientation.Horizontal,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}

@Serializable
@SerialName("Retail5Ladder")
object Retail5LadderPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(1, 3)

    private val generator = LadderElasticGenerator.build(
        ladder = Retail5.MetaTiles.Ladder,
    )

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}

@Serializable
@SerialName("Retail5HorizontalRoof")
object Retail5HorizontalRoofPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(3, 1)

    private val generator = ElasticStructurePattern(
        startingPattern = RectangularMetaTilePattern(
            metaTiles = listOf(
                MetaTile(267),
            ),
            width = 1,
        ),
        repeatingPattern = RectangularMetaTilePattern(
            metaTiles = listOf(
                MetaTile(268),
            ),
            width = 1,
        ),
        endingPattern = RectangularMetaTilePattern(
            metaTiles = listOf(
                MetaTile(269), MetaTile(270),
            ),
            width = 1,
        ),
        orientation = ElasticStructurePatternOrientation.Horizontal,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}
