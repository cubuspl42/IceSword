package icesword.editor.retails

import icesword.editor.ChainedTileGenerator
import icesword.editor.ForwardTileGenerator
import icesword.editor.KnotPrototype
import icesword.editor.MetaTile
import icesword.editor.TileGenerator
import icesword.editor.TileGeneratorContext
import icesword.editor.knot_mesh.KnotStructurePattern
import icesword.editor.knot_mesh.MetaTilePattern1x1
import icesword.editor.knot_mesh.MetaTilePattern2x1
import icesword.editor.knot_mesh.StructureConcavePattern
import icesword.editor.knot_mesh.StructureConvexPattern
import icesword.editor.retails.Retail5.MetaTiles.Bridge
import icesword.editor.retails.Retail5.MetaTiles.House
import icesword.editor.retails.Retail5.MetaTiles.Ladder
import icesword.editor.retails.Retail5.MetaTiles.Rock
import icesword.editor.retails.Retail5.MetaTiles.Spikes

private val rockPattern = object : KnotStructurePattern(
    convexPattern = StructureConvexPattern(
        topLeft = MetaTilePattern2x1(Rock.topLeftOuter, Rock.topLeftInner),
        top = MetaTilePattern1x1(Rock.topCenter),
        topRight = MetaTilePattern1x1(Rock.topRight),
        left = MetaTilePattern2x1(Rock.leftOuter, Rock.leftInner),
        right = MetaTilePattern1x1(Rock.right),
        bottomLeft = MetaTilePattern2x1(Rock.bottomLeftOuter, Rock.bottomLeftInner),
        bottom = MetaTilePattern1x1(Rock.bottomCenter),
        bottomRight = MetaTilePattern1x1(Rock.bottomRight),
    ),
    concavePattern = StructureConcavePattern(
        bottomLeft = MetaTilePattern1x1(Rock.concaveBottomLeft),
        bottomRight = MetaTilePattern2x1(Rock.concaveBottomRightOuter, Rock.concaveBottomRightInner),
    ),
    fill = MetaTilePattern1x1(Rock.center),
) {
    override fun test(knotPrototype: KnotPrototype): Boolean =
        knotPrototype is KnotPrototype.Retail5RockPrototype
}

private val houseTileGenerator = ChainedTileGenerator(
    listOf(
        ForwardTileGenerator(House.HorizontalRoof.left),
        ForwardTileGenerator(House.HorizontalRoof.center),
        ForwardTileGenerator(House.HorizontalRoof.rightInner),
        ForwardTileGenerator(House.HorizontalRoof.rightOuter),
    )
)

private val retailTileGenerator = object : TileGenerator {
    override fun buildTile(context: TileGeneratorContext): Int? = context.run {
        when {
            // Metal platform / ladder

            containsAll(Retail5.MetaTiles.MetalPlatform.topCenter, Ladder.top) -> 204
            containsAll(Retail5.MetaTiles.MetalPlatform.bottomCenter, Ladder.center) -> 221

            // Rock / ladder

            containsAll(Rock.topCenter, Ladder.bottom) -> 303

            // Rock / spikes

            containsAll(Rock.right, Spikes.top) -> 314
            containsAll(Rock.leftOuter, Spikes.top) -> 497
            containsAll(Rock.concaveBottomLeft, Spikes.bottom) -> 404
            containsAll(Rock.topCenter, Spikes.bottom) -> 405
            containsAll(Rock.concaveBottomRightOuter, Spikes.bottom) -> 406
            containsAll(Rock.concaveBottomRightInner, Spikes.bottom) -> 407

            // Rock / bridge

            containsAll(Rock.topRight, Bridge.left) -> 503
            containsAll(Rock.topLeftOuter, Bridge.core) -> 507
            containsAll(Rock.topLeftInner, Bridge.right) -> 508

            containsAll(Rock.right, Bridge.left) -> 518
            containsAll(Rock.leftOuter, Bridge.right) -> 519
            containsAll(Rock.leftOuter, Bridge.core) -> 519
            containsAll(Rock.leftInner, Bridge.right) -> 313

            // House / shadow

            containsAll(House.Block.leftInner, House.shadow) -> 400
            containsAll(House.core, House.shadow) -> 275
            containsAll(House.Block.right, House.shadow) -> 276

            else -> null
        }
    }
}

object Retail5 : Retail(naturalIndex = 5) {
    object MetaTiles {
        object Rock {
            val topLeftOuter = MetaTile(299)

            val topLeftInner = MetaTile(300)

            val topCenter = MetaTile(309)

            val topRight = MetaTile(311)

            val leftOuter = MetaTile(312)

            val leftInner = MetaTile(313)

            val center = MetaTile(307)

            val right = MetaTile(314)

            val bottomLeftOuter = MetaTile(315)

            val bottomLeftInner = MetaTile(316)

            val bottomCenter = MetaTile(535)

            val bottomRight = MetaTile(322)

            val concaveBottomLeft = MetaTile(308)

            val concaveBottomRightOuter = MetaTile(305)

            val concaveBottomRightInner = MetaTile(306)
        }

        object MetalPlatform {
            val topLeftOuter = MetaTile(201)

            val topLeftInner = MetaTile(202)

            val topCenter = MetaTile(203)

            val topRightInner = MetaTile(205)

            val topRightOuter = MetaTile(206)

            val bottomLeft = MetaTile(207)

            val bottomCenter = MetaTile(219)

            val bottomRight = MetaTile(222)
        }

        object Ladder : LadderPattern {
            override val top = MetaTile(516)

            override val center = MetaTile(231)

            override val bottom = MetaTile(498)
        }

        object Spikes {
            val top = MetaTile(402)

            val bottom = MetaTile(405)
        }

        object Bridge {
            val left = MetaTile(506)

            val core = MetaTile(500)

            val brokenEdge = MetaTile(502)

            val right = MetaTile(505)
        }

        object House {
            val core = MetaTile(266)

            val shadow = MetaTile(null)

            object HorizontalRoof {
                val left = MetaTile(267)

                val center = MetaTile(268)

                val rightInner = MetaTile(269)

                val rightOuter = MetaTile(270)
            }

            object Block {
                val leftOuter = MetaTile(264)

                val leftInner = MetaTile(265)

                val right = MetaTile(281)
            }
        }
    }

    override val knotStructurePatterns: List<KnotStructurePattern> =
        listOf(rockPattern)

    override val tileGenerator: TileGenerator = ChainedTileGenerator(
        listOf(
            houseTileGenerator,
            retailTileGenerator,
        )
    )
}
