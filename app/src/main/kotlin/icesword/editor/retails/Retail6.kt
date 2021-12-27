package icesword.editor.retails

import icesword.editor.ChainedTileGenerator
import icesword.editor.ForwardTileGenerator
import icesword.editor.KnotPrototype
import icesword.editor.MetaTile
import icesword.editor.TileGenerator
import icesword.editor.TileGeneratorContext
import icesword.editor.knot_mesh.KnotStructurePattern
import icesword.editor.knot_mesh.MetaTilePattern1x1
import icesword.editor.knot_mesh.StructureConcavePattern
import icesword.editor.knot_mesh.StructureConvexPattern
import icesword.editor.retails.Retail6.MetaTiles.Bricks
import icesword.editor.retails.Retail6.MetaTiles.Fence
import icesword.editor.retails.Retail6.MetaTiles.HorizontalRoof
import icesword.editor.retails.Retail6.MetaTiles.House
import icesword.editor.retails.Retail6.MetaTiles.Pavement

private val bricksPattern = object : KnotStructurePattern(
    convexPattern = StructureConvexPattern(
        topLeft = MetaTilePattern1x1(Bricks.topLeft),
        top = MetaTilePattern1x1(Bricks.topCenter),
        topRight = MetaTilePattern1x1(Bricks.topRight),
        left = MetaTilePattern1x1(Bricks.left),
        right = MetaTilePattern1x1(Bricks.center),
        bottomLeft = MetaTilePattern1x1(Bricks.bottomLeft),
        bottom = MetaTilePattern1x1(Bricks.center),
        bottomRight = MetaTilePattern1x1(Bricks.center),
    ),
    concavePattern = StructureConcavePattern(
        topLeft = MetaTilePattern1x1(Bricks.center),
        topRight = MetaTilePattern1x1(Bricks.center),
        bottomLeft = MetaTilePattern1x1(Bricks.center),
        bottomRight = MetaTilePattern1x1(Bricks.concaveBottomRight),
    ),
    fill = MetaTilePattern1x1(Bricks.center),
) {
    override fun test(knotPrototype: KnotPrototype): Boolean =
        knotPrototype is KnotPrototype.Retail6BricksPrototype
}

private val horizontalRoofTileGenerator = TileGenerator.forwardAll(
    HorizontalRoof.topLeft,
    HorizontalRoof.topCenter,
    HorizontalRoof.topRightInner,
    HorizontalRoof.topRightOuter,
    HorizontalRoof.bottomLeft,
    HorizontalRoof.bottomCenter,
    HorizontalRoof.bottomRightInner,
    HorizontalRoof.bottomRightOuter,
)

private val retailTileGenerator = object : TileGenerator {
    override fun buildTile(context: TileGeneratorContext): Int? = context.run {
        when {
            // Bricks / pavement

            containsAll(Bricks.center, Pavement.left) -> 110
            containsAll(Bricks.left, Pavement.rightOuter) -> 203

            // House / house

            containsAll(House.topLeft, House.bottomLeftOuter) -> 43
            containsAll(House.topCenter, House.bottomLeftInner) -> 60
            containsAll(House.topCenter, House.bottomCenter) -> 61
            containsAll(House.topRight, House.bottomRight) -> 62

            containsAll(House.topCenter, House.bottomLeftOuter) -> 186
            containsAll(House.topCenter, House.bottomRight) -> 188

            // House  / fence

            containsAll(House.leftOuter, Fence.top) -> 24
            containsAll(House.bottomLeftOuter, Fence.bottom) -> 29

            containsAll(House.right, Fence.top) -> 169
            containsAll(House.bottomRight, Fence.bottom) -> 171

            // House / pavement

            containsAll(House.bottomLeftOuter, Pavement.center) -> 64
            containsAll(House.bottomLeftInner, Pavement.center) -> 66
            containsAll(House.bottomCenter, Pavement.center) -> 67
            containsAll(House.bottomRight, Pavement.center) -> 68

            // Pavement / fence

            containsAll(Pavement.center, Fence.bottom) -> 27

            else -> null
        }
    }
}

object Retail6 : Retail(naturalIndex = 6) {
    object MetaTiles {
        object Fence {
            val top = MetaTile(22)

            val bottom = MetaTile(27)
        }

        object Pavement {
            val left = MetaTile(89)

            val center = MetaTile(101)

            val rightInner = MetaTile(99)

            val rightOuter = MetaTile(100)
        }

        object HorizontalRoof {
            val topLeft = MetaTile(38)

            val topCenter = MetaTile(39)

            val topRightInner = MetaTile(40)

            val topRightOuter = MetaTile(41)

            val bottomLeft = MetaTile(44)

            val bottomCenter = MetaTile(42)

            val bottomRightInner = MetaTile(46)

            val bottomRightOuter = MetaTile(47)
        }

        object Ladder : LadderPattern {
            override val top = MetaTile(16)

            override val center = MetaTile(18)

            override val bottom = MetaTile(20)
        }

        object Bricks {
            val topLeft = MetaTile(102)

            val topCenter = MetaTile(111)

            val topRight = MetaTile(108)

            val left = MetaTile(109)

            val center = MetaTile(110)

            val bottomLeft = MetaTile(116)

            val concaveTopRight = MetaTile(200)

            val concaveBottomRight = MetaTile(115)
        }

        object House {
            val topLeft = MetaTile(32)

            val topCenter = MetaTile(33)

            val topRight = MetaTile(36)

            val leftOuter = MetaTile(37)

            val leftInner = MetaTile(55)

            val center = MetaTile(49)

            val right = MetaTile(57)

            val bottomLeftOuter = MetaTile(43)

            val bottomLeftInner = MetaTile(60)

            val bottomCenter = MetaTile(61)

            val bottomRight = MetaTile(62)
        }
    }

    override val knotStructurePatterns: List<KnotStructurePattern> =
        listOf(bricksPattern)

    override val tileGenerator: TileGenerator = TileGenerator.chained(
        horizontalRoofTileGenerator,
        retailTileGenerator,
    )
}
