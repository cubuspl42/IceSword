package icesword.editor.elastic.prototype

import icesword.editor.ElasticMetaTilesGenerator
import icesword.editor.MetaTile
import icesword.editor.elastic.ElasticLinearPattern
import icesword.editor.elastic.ElasticLinearPatternOrientation
import icesword.editor.elastic.ElasticRectangularFragment
import icesword.editor.elastic.ElasticRectangularPattern
import icesword.editor.elastic.LinearMetaTilePattern
import icesword.editor.retails.LadderElasticGenerator
import icesword.editor.retails.Retail
import icesword.editor.retails.Retail6
import icesword.editor.retails.Retail6.MetaTiles.HorizontalRoof
import icesword.editor.retails.Retail6.MetaTiles.House
import icesword.geometry.IntSize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Retail6Fence")
object Retail6FencePrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(4, 2)

    private val generator = ElasticLinearPattern(
        startingPattern = LinearMetaTilePattern(
            metaTiles = listOf(
                MetaTile.None,
                Retail6.MetaTiles.Fence.bottomLeft,
            ),
            width = 2,
        ),
        repeatingPattern = LinearMetaTilePattern(
            metaTiles = listOf(
                Retail6.MetaTiles.Fence.top,
                Retail6.MetaTiles.Fence.bottomCenter,
            ),
            width = 2,
        ),
        endingPattern = LinearMetaTilePattern(
            metaTiles = listOf(
                MetaTile.None,
                Retail6.MetaTiles.Fence.bottomRightInner,
                MetaTile.None,
                Retail6.MetaTiles.Fence.bottomRightOuter,
            ),
            width = 2,
        ),
        orientation = ElasticLinearPatternOrientation.Horizontal,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticMetaTilesGenerator = generator
}

@Serializable
@SerialName("Retail6HorizontalRoof")
object Retail6HorizontalRoofPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(4, 2)

    private val generator = ElasticLinearPattern(
        startingPattern = LinearMetaTilePattern(
            metaTiles = listOf(
                HorizontalRoof.topLeft,
                HorizontalRoof.bottomLeft,
            ),
            width = 2,
        ),
        repeatingPattern = LinearMetaTilePattern(
            metaTiles = listOf(
                HorizontalRoof.topCenter,
                HorizontalRoof.bottomCenter,
            ),
            width = 2,
        ),
        endingPattern = LinearMetaTilePattern(
            metaTiles = listOf(
                HorizontalRoof.topRightInner,
                HorizontalRoof.bottomRightInner,
                HorizontalRoof.topRightOuter,
                HorizontalRoof.bottomRightOuter,
            ),
            width = 2,
        ),
        orientation = ElasticLinearPatternOrientation.Horizontal,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticMetaTilesGenerator = generator
}

@Serializable
@SerialName("Retail6Ladder")
object Retail6LadderPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(1, 3)

    private val generator = LadderElasticGenerator.build(
        ladder = Retail6.MetaTiles.Ladder,
    )

    override fun buildGenerator(retail: Retail): ElasticMetaTilesGenerator = generator
}

@Serializable
@SerialName("Retail6WhiteHouse")
object Retail6WhiteHousePrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(5, 4)

    private val generator = ElasticRectangularPattern(
        topLeft = ElasticRectangularFragment(
            metaTiles = listOf(
                House.topLeft, House.topCenter,
            ),
            width = 2,
            height = 1,
        ),
        topCenter = ElasticRectangularFragment.ofSingle(
            House.topCenter,
        ),
        topRight = ElasticRectangularFragment.ofSingle(
            House.topRight,
        ),
        centerLeft = ElasticRectangularFragment(
            metaTiles = listOf(
                House.leftOuter, House.leftInner,
            ),
            width = 2,
            height = 1,
        ),
        center = ElasticRectangularFragment.ofSingle(
            House.center,
        ),
        centerRight = ElasticRectangularFragment.ofSingle(
            House.right,
        ),
        bottomLeft = ElasticRectangularFragment(
            metaTiles = listOf(
                House.bottomLeftOuter, House.bottomLeftInner,
            ),
            width = 2,
            height = 1,
        ),
        bottomCenter = ElasticRectangularFragment.ofSingle(
            House.bottomCenter,
        ),
        bottomRight = ElasticRectangularFragment.ofSingle(
            House.bottomRight,
        ),
        leftStaticWidth = 2,
        centerHorizontalRepeatingWidth = 1,
        rightStaticWidth = 1,
        topStaticHeight = 1,
        centerVerticalRepeatingHeight = 1,
        bottomStaticHeight = 1,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail) = generator
}