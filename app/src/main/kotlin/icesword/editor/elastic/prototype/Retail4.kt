package icesword.editor.elastic.prototype

import icesword.editor.ElasticGenerator
import icesword.editor.ElasticGeneratorOutput
import icesword.editor.ElasticMetaTilesGenerator
import icesword.editor.MetaTile
import icesword.editor.WapObjectPropsData
import icesword.editor.elastic.ElasticLinearPattern
import icesword.editor.elastic.ElasticLinearPatternOrientation
import icesword.editor.elastic.ElasticRectangularFragment
import icesword.editor.elastic.ElasticRectangularPattern
import icesword.editor.elastic.LinearMetaTilePattern
import icesword.editor.retails.LadderElasticGenerator
import icesword.editor.retails.Retail
import icesword.editor.retails.Retail2
import icesword.editor.retails.Retail4
import icesword.editor.retails.Retail4.MetaTiles
import icesword.editor.retails.Retail4.MetaTiles.Goo
import icesword.editor.retails.Retail4.MetaTiles.NaturalPlatform
import icesword.editor.retails.Retail4.MetaTiles.Tree
import icesword.editor.retails.retail4TreeLog
import icesword.geometry.IntSize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Retail4TreeLog")
object Retail4TreeLogPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(1, 4)

    private val generator = retail4TreeLog.toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticMetaTilesGenerator = generator
}

@Serializable
@SerialName("Retail4WoodenPlatform")
object Retail4WoodenPlatformPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(3, 1)

    private val generator = ElasticLinearPattern(
        startingPattern = LinearMetaTilePattern(
            metaTiles = listOf(
                MetaTiles.WoodenPlatform.left,
            ),
            width = 1,
        ),
        repeatingPattern = LinearMetaTilePattern(
            metaTiles = listOf(
                MetaTiles.WoodenPlatform.center,
            ),
            width = 1,
        ),
        endingPattern = LinearMetaTilePattern(
            metaTiles = listOf(
                MetaTiles.WoodenPlatform.right,
            ),
            width = 1,
        ),
        orientation = ElasticLinearPatternOrientation.Horizontal,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticMetaTilesGenerator = generator
}

@Serializable
@SerialName("Retail4Ladder")
object Retail4LadderPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(1, 3)

    private val generator = LadderElasticGenerator.build(
        ladder = MetaTiles.Ladder,
    )

    override fun buildGenerator(retail: Retail): ElasticMetaTilesGenerator = generator
}

private class NaturalPlatformGenerator(
    topLeftOuter: MetaTile,
    topLeftInner: MetaTile,
    topRightInner: MetaTile,
    topRightOuter: MetaTile,
) : ElasticGenerator {
    // There's a subtle color difference between 107 and 139
    // There's a subtle color difference between 112 and 144 and 152
    private val shortGenerator = ElasticRectangularPattern(
        topLeft = ElasticRectangularFragment(
            metaTiles = listOf(
                topLeftOuter, topLeftInner,
                NaturalPlatform.leftOuter, NaturalPlatform.bottomCenter,
            ),
            width = 2,
            height = 2,
        ),
        topCenter = ElasticRectangularFragment(
            metaTiles = listOf(
                NaturalPlatform.topCenter,
                NaturalPlatform.bottomCenter,
            ),
            width = 1,
            height = 2,
        ),
        topRight = ElasticRectangularFragment(
            metaTiles = listOf(
                topRightInner, topRightOuter,
                NaturalPlatform.bottomCenter, NaturalPlatform.rightOuter,
            ),
            width = 2,
            height = 2,
        ),
        leftStaticWidth = 2,
        centerHorizontalRepeatingWidth = 1,
        rightStaticWidth = 2,
        topStaticHeight = 2,
    ).toElasticGenerator()

    private val tallGenerator = ElasticRectangularPattern(
        topLeft = ElasticRectangularFragment(
            metaTiles = listOf(
                topLeftOuter, topLeftInner,
                NaturalPlatform.leftOuter, NaturalPlatform.leftInner,
                MetaTile.None, NaturalPlatform.bottomLeft,
            ),
            width = 2,
            height = 3,
        ),
        topCenter = ElasticRectangularFragment(
            metaTiles = listOf(
                NaturalPlatform.topCenter,
                NaturalPlatform.center,
                NaturalPlatform.bottomCenter,
            ),
            width = 1,
            height = 3,
        ),
        topRight = ElasticRectangularFragment(
            metaTiles = listOf(
                topRightInner, topRightOuter,
                NaturalPlatform.rightInner, NaturalPlatform.rightOuter,
                NaturalPlatform.bottomRight, MetaTile.None,
            ),
            width = 2,
            height = 3,
        ),
        leftStaticWidth = 2,
        centerHorizontalRepeatingWidth = 1,
        rightStaticWidth = 2,
        topStaticHeight = 3,
    ).toElasticGenerator()

    override fun buildOutput(size: IntSize): ElasticGeneratorOutput {
        val effectiveGenerator = when {
            size.height < 3 -> shortGenerator
            else -> tallGenerator
        }

        return effectiveGenerator.buildOutput(size = size)
    }
}

@Serializable
@SerialName("Retail4NarrowNaturalPlatform")
object Retail4NarrowNaturalPlatformPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(5, 3)

    private val generator = NaturalPlatformGenerator(
        topLeftOuter = NaturalPlatform.topLeftOuterNarrow,
        topLeftInner = NaturalPlatform.topLeftInnerNarrow,
        topRightInner = NaturalPlatform.topRightInnerNarrow,
        topRightOuter = NaturalPlatform.topRightOuterNarrow,
    )

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}

@Serializable
@SerialName("Retail4WideNaturalPlatform")
object Retail4WideNaturalPlatformPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(5, 3)

    private val generator = NaturalPlatformGenerator(
        topLeftOuter = NaturalPlatform.topLeftOuterWide,
        topLeftInner = NaturalPlatform.topCenter,
        topRightInner = NaturalPlatform.topCenter,
        topRightOuter = NaturalPlatform.topRightOuterWide,
    )

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}

@Serializable
@SerialName("Retail4Tree")
object Retail4TreePrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(1, 3)

    private val generator = ElasticRectangularPattern(
        topLeft = ElasticRectangularFragment(
            metaTiles = listOf(Tree.trunk),
            width = 1,
            height = 1,
            wapObject = WapObjectPropsData(
                x = 33,
                y = -24,
                z = 1000,
                i = -1,
                logic = "DoNothingNormal",
                imageSet = "LEVEL_BUSH",
            ),
        ),
        centerLeft = ElasticRectangularFragment.ofSingle(
            Tree.trunk,
        ),
        leftStaticWidth = 1,
        topStaticHeight = 1,
        centerVerticalRepeatingHeight = 1,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}

@Serializable
@SerialName("Retail4Goo")
object Retail4GooPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(5, 2)

    private val gooCoverupObject = WapObjectPropsData(
        x = 32,
        y = 82,
        z = 8000,
        i = -1,
        logic = "GooCoverup",
    )

    private val generator = ElasticRectangularPattern(
        topLeft = ElasticRectangularFragment(
            metaTiles = listOf(
                Goo.left,
                MetaTiles.death,
            ),
            wapObject = gooCoverupObject.copy(
                imageSet = "LEVEL_GOOLEFT",
            ),
            width = 1,
            height = 2,
        ),
        topCenter = ElasticRectangularFragment(
            metaTiles = listOf(
                Goo.center,
                MetaTiles.death,
            ),
            wapObject = gooCoverupObject.copy(
                imageSet = "LEVEL_GOOMIDDLE",
            ),
            width = 1,
            height = 2,
        ),
        topRight = ElasticRectangularFragment(
            metaTiles = listOf(
                Goo.right,
                MetaTiles.death,
            ),
            wapObject = gooCoverupObject.copy(
                imageSet = "LEVEL_GOORIGHT",
            ),
            width = 1,
            height = 2,
        ),
        leftStaticWidth = 1,
        centerHorizontalRepeatingWidth = 1,
        rightStaticWidth = 1,
        topStaticHeight = 2,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}
