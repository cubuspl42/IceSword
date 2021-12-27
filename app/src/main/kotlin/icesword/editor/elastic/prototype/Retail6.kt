package icesword.editor.elastic.prototype

import icesword.editor.ElasticGenerator
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
import icesword.editor.retails.Retail6.MetaTiles.BrownHouse
import icesword.editor.retails.Retail6.MetaTiles.Fence
import icesword.editor.retails.Retail6.MetaTiles.HorizontalRoof
import icesword.editor.retails.Retail6.MetaTiles.House
import icesword.editor.retails.Retail6.MetaTiles.Pavement
import icesword.editor.retails.Retail6.MetaTiles.WhiteHouse
import icesword.geometry.IntSize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Retail6Pavement")
object Retail6PavementPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(4, 1)

    private val generator = ElasticRectangularPattern(
        topLeft = ElasticRectangularFragment.ofSingle(
            Pavement.left,
        ),
        topCenter = ElasticRectangularFragment.ofSingle(
            Pavement.center,
        ),
        topRight = ElasticRectangularFragment(
            metaTiles = listOf(
                Pavement.rightInner, Pavement.rightOuter,
            ),
            width = 2,
            height = 1,
        ),
        leftStaticWidth = 1,
        centerHorizontalRepeatingWidth = 1,
        rightStaticWidth = 2,
        topStaticHeight = 1,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail) = generator
}

@Serializable
@SerialName("Retail6Fence")
object Retail6FencePrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(2, 2)

    private val generator = ElasticGenerator.fromHorizontalPattern(
        center = ElasticRectangularFragment(
            metaTiles = listOf(
                Fence.top,
                Fence.bottom
            ),
            width = 1,
            height = 2,
        ),
        staticHeight = 2,
    )

    override fun buildGenerator(retail: Retail) = generator
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
sealed class Retail6HousePrototype : ElasticPrototype() {
    final override val defaultSize: IntSize = IntSize(5, 4)

    abstract val house: House

    override fun buildGenerator(retail: Retail) = ElasticRectangularPattern(
        topLeft = ElasticRectangularFragment(
            metaTiles = listOf(
                house.topLeft, house.topCenter,
            ),
            width = 2,
            height = 1,
        ),
        topCenter = ElasticRectangularFragment.ofSingle(
            house.topCenter,
        ),
        topRight = ElasticRectangularFragment.ofSingle(
            house.topRight,
        ),
        centerLeft = ElasticRectangularFragment(
            metaTiles = listOf(
                house.leftOuter, house.leftInner,
            ),
            width = 2,
            height = 1,
        ),
        center = ElasticRectangularFragment.ofSingle(
            house.center,
        ),
        centerRight = ElasticRectangularFragment.ofSingle(
            house.right,
        ),
        bottomLeft = ElasticRectangularFragment(
            metaTiles = listOf(
                house.bottomLeftOuter, house.bottomLeftInner,
            ),
            width = 2,
            height = 1,
        ),
        bottomCenter = ElasticRectangularFragment.ofSingle(
            house.bottomCenter,
        ),
        bottomRight = ElasticRectangularFragment.ofSingle(
            house.bottomRight,
        ),
        leftStaticWidth = 2,
        centerHorizontalRepeatingWidth = 1,
        rightStaticWidth = 1,
        topStaticHeight = 1,
        centerVerticalRepeatingHeight = 1,
        bottomStaticHeight = 1,
    ).toElasticGenerator()
}

@Serializable
@SerialName("Retail6WhiteHouse")
object Retail6WhiteHousePrototype : Retail6HousePrototype() {
    override val house: House = WhiteHouse
}

@Serializable
@SerialName("Retail6BrownHouse")
object Retail6BrownHousePrototype : Retail6HousePrototype() {
    override val house: House = BrownHouse
}
