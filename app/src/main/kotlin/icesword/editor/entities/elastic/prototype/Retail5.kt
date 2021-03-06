package icesword.editor.entities.elastic.prototype

import icesword.editor.entities.ElasticGenerator
import icesword.editor.entities.ElasticGeneratorOutput
import icesword.editor.entities.ElasticMetaTilesGenerator
import icesword.editor.MetaTile
import icesword.editor.entities.WapObjectPropsData
import icesword.editor.entities.elastic.ElasticLinearPattern
import icesword.editor.entities.elastic.ElasticLinearPatternOrientation
import icesword.editor.entities.elastic.ElasticRectangularFragment
import icesword.editor.entities.elastic.ElasticRectangularPattern
import icesword.editor.entities.elastic.LinearMetaTilePattern
import icesword.editor.retails.LadderElasticGenerator
import icesword.editor.retails.Retail
import icesword.editor.retails.Retail5
import icesword.editor.retails.Retail5.MetaTiles.Arch
import icesword.editor.retails.Retail5.MetaTiles.Bridge
import icesword.editor.retails.Retail5.MetaTiles.House
import icesword.editor.retails.Retail5.MetaTiles.Spikes
import icesword.editor.retails.Retail5.MetaTiles.Wall
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

@Serializable
@SerialName("Retail5ArchSpan")
object Retail5ArchSpanPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(5, 1)

    private val generator = ElasticRectangularPattern(
        topLeft = ElasticRectangularFragment(
            metaTiles = listOf(
                Wall.Dirty.topLeft, Arch.Span.left,
            ),
            width = 2,
            height = 1,
        ),
        topCenter = ElasticRectangularFragment.ofSingle(
            Arch.Span.center,
        ),
        topRight = ElasticRectangularFragment(
            metaTiles = listOf(
                Arch.Span.right, Wall.Dirty.topRight,
            ),
            width = 2,
            height = 1,
        ),
        leftStaticWidth = 2,
        centerHorizontalRepeatingWidth = 1,
        rightStaticWidth = 2,
        topStaticHeight = 1
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail) = generator
}

@Serializable
@SerialName("Retail5ArchLeg")
object Retail5ArchLegPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(3, 3)

    private val generator = ElasticRectangularPattern(
        centerLeft = ElasticRectangularFragment(
            metaTiles = listOf(
                Arch.Leg.left, Arch.Leg.core, Arch.Leg.right,
            ),
            width = 3,
            height = 1,
        ),
        leftStaticWidth = 3,
        centerVerticalRepeatingHeight = 1,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail) = generator
}

@Serializable
@SerialName("Retail5DirtyWall")
object Retail5DirtyWallPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(3, 1)

    private val generator = ElasticRectangularPattern(
        topLeft = ElasticRectangularFragment.ofSingle(
            Wall.Dirty.topLeft,
        ),
        topCenter = ElasticRectangularFragment.ofSingle(
            Wall.Dirty.topCenter,
        ),
        topRight = ElasticRectangularFragment.ofSingle(
            Wall.Dirty.topRight,
        ),
        leftStaticWidth = 1,
        centerHorizontalRepeatingWidth = 1,
        rightStaticWidth = 1,
        topStaticHeight = 1,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail) = generator
}

@Serializable
@SerialName("Retail5CleanWall")
object Retail5CleanWallPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(3, 2)

    private val generator = ElasticRectangularPattern(
        topLeft = ElasticRectangularFragment.ofSingle(
            Wall.Clean.topLeft,
        ),
        topCenter = ElasticRectangularFragment.ofSingle(
            Wall.Clean.topCenter,
        ),
        topRight = ElasticRectangularFragment.ofSingle(
            Wall.Clean.topRight,
        ),
        centerLeft = ElasticRectangularFragment.ofSingle(
            Wall.Clean.left,
        ),
        center = ElasticRectangularFragment.ofSingle(
            Wall.Clean.core,
        ),
        centerRight = ElasticRectangularFragment.ofSingle(
            Wall.Clean.core,
        ),
        leftStaticWidth = 1,
        centerHorizontalRepeatingWidth = 1,
        rightStaticWidth = 1,
        topStaticHeight = 1,
        centerVerticalRepeatingHeight = 1,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail) = generator
}

@Serializable
sealed class PostPrototype : ElasticPrototype() {
    companion object {
        const val postBottomImageSet = "LEVEL_POSTBOTTOM"
    }

    abstract val x: Int

    abstract val topPostY: Int

    abstract val topPostImageSet: String

    abstract val bottomPostY1: Int

    abstract val bottomPostY2: Int?

    final override val defaultSize: IntSize = IntSize(1, 3)

    override fun buildGenerator(retail: Retail): ElasticGenerator {
        val post = WapObjectPropsData(
            x = x,
            z = 1000,
            i = -1,
            logic = "BehindCandy",
        )

        return ElasticRectangularPattern(
            topLeft = ElasticRectangularFragment(
                metaTiles = listOf(MetaTile.None),
                wapObject = post.copy(
                    y = topPostY,
                    imageSet = topPostImageSet,
                ),
                width = 1,
                height = 1,
            ),
            centerLeft = ElasticRectangularFragment(
                metaTiles = listOf(MetaTile.None),
                wapObject = post.copy(
                    y = 28,
                    imageSet = postBottomImageSet,
                ),
                width = 1,
                height = 1,
            ),
            bottomLeft = ElasticRectangularFragment(
                metaTiles = listOf(MetaTile.None),
                wapObject = post.copy(
                    y = bottomPostY1,
                    imageSet = postBottomImageSet,
                ),
                extraWapObject = bottomPostY2?.let { y ->
                    post.copy(
                        y = y,
                        imageSet = postBottomImageSet,
                    )
                },
                width = 1,
                height = 1,
            ),
            leftStaticWidth = 1,
            topStaticHeight = 1,
            centerVerticalRepeatingHeight = 1,
            bottomStaticHeight = 1,
        ).toElasticGenerator()
    }
}

@Serializable
@SerialName("Retail5Post1")
object Retail5Post1Prototype : PostPrototype() {
    override val x: Int = 65

    override val topPostY: Int = 27

    override val topPostImageSet: String = "LEVEL_POSTTOP1"

    override val bottomPostY1: Int = 28

    override val bottomPostY2: Int = 32
}

@Serializable
@SerialName("Retail5Post1Padded")
object Retail5Post1PaddedPrototype : PostPrototype() {
    override val x: Int = 65

    override val topPostY: Int = 27

    override val topPostImageSet: String = "LEVEL_POSTTOP1"

    override val bottomPostY1: Int = 4

    override val bottomPostY2: Int? = null
}

@Serializable
@SerialName("Retail5Post2")
object Retail5Post2Prototype : PostPrototype() {
    override val x: Int = 46

    override val topPostY: Int = 39

    override val topPostImageSet: String = "LEVEL_POSTTOP2"

    override val bottomPostY1: Int = 28

    override val bottomPostY2: Int = 32
}

@Serializable
@SerialName("Retail5Post2Padded")
object Retail5Post2PaddedPrototype : PostPrototype() {
    override val x: Int = 46

    override val topPostY: Int = 39

    override val topPostImageSet: String = "LEVEL_POSTTOP2"

    override val bottomPostY1: Int = 4

    override val bottomPostY2: Int? = null
}
