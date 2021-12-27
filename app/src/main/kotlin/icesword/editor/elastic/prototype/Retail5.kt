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
import icesword.editor.retails.Retail5
import icesword.editor.retails.Retail5.MetaTiles.Bridge
import icesword.editor.retails.Retail5.MetaTiles.House
import icesword.editor.retails.Retail5.MetaTiles.Spikes
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Retail5MetalPlatform")
object Retail5MetalPlatformPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(5, 2)

    private val generator = ElasticLinearPattern(
        startingPattern = LinearMetaTilePattern(
            metaTiles = listOf(
                Retail5.MetaTiles.MetalPlatform.topLeftOuter,
                MetaTile.None,
                Retail5.MetaTiles.MetalPlatform.topLeftInner,
                Retail5.MetaTiles.MetalPlatform.bottomLeft,
            ),
            width = 2,
        ),
        repeatingPattern = LinearMetaTilePattern(
            metaTiles = listOf(
                Retail5.MetaTiles.MetalPlatform.topCenter,
                Retail5.MetaTiles.MetalPlatform.bottomCenter,
            ),
            width = 2,
        ),
        endingPattern = LinearMetaTilePattern(
            metaTiles = listOf(
                Retail5.MetaTiles.MetalPlatform.topRightInner,
                Retail5.MetaTiles.MetalPlatform.bottomCenter,
                Retail5.MetaTiles.MetalPlatform.topRightOuter,
                Retail5.MetaTiles.MetalPlatform.bottomRight,
            ),
            width = 2,
        ),
        orientation = ElasticLinearPatternOrientation.Horizontal,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticMetaTilesGenerator = generator
}

@Serializable
@SerialName("Retail5Ladder")
object Retail5LadderPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(1, 3)

    private val generator = LadderElasticGenerator.build(
        ladder = Retail5.MetaTiles.Ladder,
    )

    override fun buildGenerator(retail: Retail): ElasticMetaTilesGenerator = generator
}

@Serializable
@SerialName("Retail5HorizontalRoof")
object Retail5HorizontalRoofPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(4, 2)

    private val generator = ElasticRectangularPattern(
        topLeft = ElasticRectangularFragment(
            metaTiles = listOf(
                House.HorizontalRoof.left,
                House.shadow,
            ),
            width = 1,
            height = 2,
        ),
        topCenter = ElasticRectangularFragment(
            metaTiles = listOf(
                House.HorizontalRoof.center,
                House.shadow,
            ),
            width = 1,
            height = 2,
        ),
        topRight = ElasticRectangularFragment(
            metaTiles = listOf(
                House.HorizontalRoof.rightInner, House.HorizontalRoof.rightOuter,
                House.shadow, MetaTile.None,
            ),
            width = 2,
            height = 2,
        ),
        leftStaticWidth = 1,
        centerHorizontalRepeatingWidth = 1,
        rightStaticWidth = 2,
        topStaticHeight = 2,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail) = generator
}

@Serializable
@SerialName("Retail5Spikes")
object Retail5SpikesPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(5, 2)

    private val generator = ElasticRectangularPattern(
        topCenter = ElasticRectangularFragment(
            metaTiles = listOf(
                Spikes.top,
                Spikes.bottom,
            ),
            width = 1,
            height = 2,
        ),
        topRight = ElasticRectangularFragment(
            metaTiles = listOf(
                MetaTile.None,
                Spikes.bottom,
            ),
            width = 1,
            height = 2,
        ),
        centerHorizontalRepeatingWidth = 1,
        rightStaticWidth = 1,
        topStaticHeight = 2,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail) = generator
}

@Serializable
@SerialName("Retail5BridgeLeft")
object Retail5BridgeLeftPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(3, 1)

    private val generator = ElasticRectangularPattern(
        topLeft = ElasticRectangularFragment.ofSingle(
            Bridge.left,
        ),
        topCenter = ElasticRectangularFragment.ofSingle(
            Bridge.core,
        ),
        topRight = ElasticRectangularFragment(
            metaTiles = listOf(Bridge.core),
            wapObject = WapObjectPropsData(
                x = 70,
                y = 32,
                i = -1,
                logic = "AniCycle",
                imageSet = "LEVEL_WALKENDCAP",
            ),
            width = 1,
            height = 1,
        ),
        leftStaticWidth = 1,
        centerHorizontalRepeatingWidth = 1,
        rightStaticWidth = 1,
        topStaticHeight = 1,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}

@Serializable
@SerialName("Retail5BridgeRight")
object Retail5BridgeRightPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(3, 1)

    private val generator = ElasticRectangularPattern(
        topLeft = ElasticRectangularFragment.ofSingle(
            Bridge.brokenEdge,
        ),
        topCenter = ElasticRectangularFragment.ofSingle(
            Bridge.core,
        ),
        topRight = ElasticRectangularFragment.ofSingle(
            Bridge.right,
        ),
        leftStaticWidth = 1,
        centerHorizontalRepeatingWidth = 1,
        rightStaticWidth = 1,
        topStaticHeight = 1,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}

@Serializable
@SerialName("Retail5BreakPlank")
object Retail5BreakPlankPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(3, 1)

    override fun buildGenerator(retail: Retail): ElasticGenerator = object : ElasticGenerator {
        override fun buildOutput(size: IntSize) = ElasticGeneratorOutput(
            localMetaTiles = emptyMap(),
            localWapObjects = listOf(
                WapObjectPropsData(
                    x = 40,
                    y = 88,
                    i = -1,
                    logic = "BreakPlank",
                    imageSet = "LEVEL_BREAKPLANK",
                    width = size.width,
                ),
            )
        )
    }
}

@Serializable
@SerialName("Retail5House")
object Retail5HousePrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(7, 5)

    private fun buildRoofMetaTiles(a: Int, w: Int): Map<IntVec2, MetaTile> {
        val roofTop = listOf(
            IntVec2(a - 1, 0) to MetaTile(255),
            IntVec2(a, 0) to MetaTile(256),
            IntVec2(a + 1, 0) to MetaTile(257),
        ).toMap()

        val roofCenter = (0 until a - 2).flatMap {
            val i = it + 1
            listOf(
                IntVec2(a - 2 - it, i) to MetaTile(255),
                IntVec2(a - 1 - it, i) to MetaTile(401),
                IntVec2(a + 1 + it, i) to MetaTile(258),
                IntVec2(a + 2 + it, i) to MetaTile(257),
            ) + ((a - it)..(a + it)).map { j ->
                IntVec2(j, i) to House.core
            }
        }.toMap()

        val roofBottom = (listOf(
            IntVec2(0, a - 1) to MetaTile(259),
            IntVec2(1, a - 1) to MetaTile(260),
            IntVec2(w - 2, a - 1) to MetaTile(262),
            IntVec2(w - 1, a - 1) to MetaTile(263),
        ) + (2..(w - 3)).map { j ->
            IntVec2(j, a - 1) to House.core
        }).toMap()

        return roofTop + roofCenter + roofBottom
    }

    private val blockGenerator = ElasticRectangularPattern(
        centerLeft = ElasticRectangularFragment(
            metaTiles = listOf(
                House.Block.leftOuter, House.Block.leftInner,
            ),
            width = 2,
            height = 1,
        ),
        center = ElasticRectangularFragment.ofSingle(
            House.core,
        ),
        centerRight = ElasticRectangularFragment.ofSingle(
            House.Block.right,
        ),
        leftStaticWidth = 2,
        centerHorizontalRepeatingWidth = 1,
        rightStaticWidth = 1,
    ).toElasticGenerator()

    private val generator = object : ElasticGenerator {
        override fun buildOutput(size: IntSize): ElasticGeneratorOutput {
            val a: Int = size.width / 2
            val w = 2 * a + 1

            val roofMetaTiles = buildRoofMetaTiles(a = a, w = w)

            val blockWidth = (w - 1).coerceAtLeast(0)
            val blockHeight = (size.height - a).coerceAtLeast(0)

            val blockMetaTiles = blockGenerator
                .buildOutput(IntSize(width = blockWidth, height = blockHeight))
                .localMetaTiles
                .mapKeys { (coord, _) -> coord + IntVec2(0, a) }

            val metaTiles = roofMetaTiles + blockMetaTiles

            return ElasticGeneratorOutput(
                localMetaTiles = metaTiles,
                localWapObjects = emptyList(),
            )
        }
    }

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}
