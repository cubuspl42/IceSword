package icesword.editor.retails

import icesword.editor.KnotPrototype
import icesword.editor.MetaTile
import icesword.editor.TileGenerator
import icesword.editor.TileGeneratorContext
import icesword.editor.knot_mesh.KnotStructurePattern
import icesword.editor.knot_mesh.MetaTilePattern1x1
import icesword.editor.knot_mesh.StructureConcavePattern
import icesword.editor.knot_mesh.StructureConvexPattern
import icesword.editor.retails.Retail6.MetaTiles.Bricks
import icesword.editor.retails.Retail6.MetaTiles.BrownHouse
import icesword.editor.retails.Retail6.MetaTiles.Fence
import icesword.editor.retails.Retail6.MetaTiles.HorizontalRoof
import icesword.editor.retails.Retail6.MetaTiles.House
import icesword.editor.retails.Retail6.MetaTiles.Pavement
import icesword.editor.retails.Retail6.MetaTiles.WhiteHouse

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

interface HouseTiles {
    companion object {
        const val topCenter: Int = 33

        const val topRight: Int = 36
    }

    val bottomLeftOuterWall: Int

    val bottomLeftInner: Int

    val bottomCenter: Int

    val bottomRight: Int

    val bottomLeftOuterRooftop: Int

    val bottomRightRooftop: Int

    val leftFence: Int

    val bottomLeftFence: Int

    val rightFence: Int

    val bottomRightFence: Int

    val bottomLeftOuterPavement: Int

    val bottomLeftInnerPavement: Int

    val bottomCenterPavement: Int

    val bottomRightPavement: Int
}

private val whiteHouseTiles = object : HouseTiles {
    override val bottomLeftOuterWall: Int = 43

    override val bottomLeftInner: Int = 60

    override val bottomCenter: Int = 61

    override val bottomRight: Int = 62

    override val bottomLeftOuterRooftop: Int = 186

    override val bottomRightRooftop: Int = 188

    override val leftFence: Int = 24

    override val bottomLeftFence: Int = 29

    override val rightFence: Int = 169

    override val bottomRightFence: Int = 171

    override val bottomLeftOuterPavement: Int = 64

    override val bottomLeftInnerPavement: Int = 66

    override val bottomCenterPavement: Int = 67

    override val bottomRightPavement: Int = 68
}

private val brownHouseTiles = object : HouseTiles {
    override val bottomLeftOuterWall: Int = 84

    override val bottomLeftInner: Int = 85

    override val bottomCenter: Int = 86

    override val bottomRight: Int = 78

    override val bottomLeftOuterRooftop: Int = 185

    override val bottomRightRooftop: Int = 187

    override val leftFence: Int = 23

    override val bottomLeftFence: Int = 28

    override val rightFence: Int = 170

    override val bottomRightFence: Int = 172

    override val bottomLeftOuterPavement: Int = 91

    override val bottomLeftInnerPavement: Int = 94

    override val bottomCenterPavement: Int = 95

    override val bottomRightPavement: Int = 98
}

class HouseTileGenerator(
    private val house: House,
    private val houseTiles: HouseTiles,
) : TileGenerator {
    override fun buildTile(context: TileGeneratorContext): Int? = context.run {
        when {
            // House / house

            containsAll(house.topLeft, house.bottomLeftOuter) -> houseTiles.bottomLeftOuterWall
            containsAll(house.topCenter, house.bottomLeftInner) -> houseTiles.bottomLeftInner
            containsAll(house.topCenter, house.bottomCenter) -> houseTiles.bottomCenter
            containsAll(house.topRight, house.bottomRight) -> houseTiles.bottomRight

            containsAll(house.topCenter, house.bottomLeftOuter) -> houseTiles.bottomLeftOuterRooftop
            containsAll(house.topCenter, house.bottomRight) -> houseTiles.bottomRightRooftop

            // House / fence

            containsAll(house.leftOuter, Fence.top) -> houseTiles.leftFence
            containsAll(house.bottomLeftOuter, Fence.bottom) -> houseTiles.bottomLeftFence

            containsAll(house.right, Fence.top) -> houseTiles.rightFence
            containsAll(house.bottomRight, Fence.bottom) -> houseTiles.bottomRightFence

            // House / pavement

            containsAll(house.bottomLeftOuter, Pavement.center) -> houseTiles.bottomLeftOuterPavement
            containsAll(house.bottomLeftInner, Pavement.center) -> houseTiles.bottomLeftInnerPavement
            containsAll(house.bottomCenter, Pavement.center) -> houseTiles.bottomCenterPavement
            containsAll(house.bottomRight, Pavement.center) -> houseTiles.bottomRightPavement

            else -> null
        }
    }
}

private val whiteHouseTileGenerator = HouseTileGenerator(
    house = WhiteHouse,
    houseTiles = whiteHouseTiles,
)

private val brownHouseTileGenerator = HouseTileGenerator(
    house = BrownHouse,
    houseTiles = brownHouseTiles,
)

private val retailTileGenerator = object : TileGenerator {
    override fun buildTile(context: TileGeneratorContext): Int? = context.run {
        when {
            // Bricks / pavement

            containsAll(Bricks.center, Pavement.left) -> 110
            containsAll(Bricks.left, Pavement.rightOuter) -> 203

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

        interface House {
            val topLeft: MetaTile

            val topCenter: MetaTile

            val topRight: MetaTile

            val leftOuter: MetaTile

            val leftInner: MetaTile

            val center: MetaTile

            val right: MetaTile

            val bottomLeftOuter: MetaTile

            val bottomLeftInner: MetaTile

            val bottomCenter: MetaTile

            val bottomRight: MetaTile
        }

        object WhiteHouse : House {
            override val topLeft: MetaTile = MetaTile(32)

            override val topCenter: MetaTile = MetaTile(HouseTiles.topCenter)

            override val topRight: MetaTile = MetaTile(HouseTiles.topRight)

            override val leftOuter: MetaTile = MetaTile(37)

            override val leftInner: MetaTile = MetaTile(55)

            override val center: MetaTile = MetaTile(49)

            override val right: MetaTile = MetaTile(57)

            override val bottomLeftOuter: MetaTile = MetaTile(whiteHouseTiles.bottomLeftOuterWall)

            override val bottomLeftInner: MetaTile = MetaTile(whiteHouseTiles.bottomLeftInner)

            override val bottomCenter: MetaTile = MetaTile(whiteHouseTiles.bottomCenter)

            override val bottomRight: MetaTile = MetaTile(whiteHouseTiles.bottomRight)
        }

        object BrownHouse : House {
            override val topLeft: MetaTile = MetaTile(69)

            override val topCenter: MetaTile = MetaTile(HouseTiles.topCenter)

            override val topRight: MetaTile = MetaTile(HouseTiles.topRight)

            override val leftOuter: MetaTile = MetaTile(74)

            override val leftInner: MetaTile = MetaTile(70)

            override val center: MetaTile = MetaTile(72)

            override val right: MetaTile = MetaTile(88)

            override val bottomLeftOuter: MetaTile = MetaTile(brownHouseTiles.bottomLeftOuterWall)

            override val bottomLeftInner: MetaTile = MetaTile(brownHouseTiles.bottomLeftInner)

            override val bottomCenter: MetaTile = MetaTile(brownHouseTiles.bottomCenter)

            override val bottomRight: MetaTile = MetaTile(brownHouseTiles.bottomRight)
        }
    }

    override val knotStructurePatterns: List<KnotStructurePattern> =
        listOf(bricksPattern)

    override val tileGenerator: TileGenerator = TileGenerator.chained(
        horizontalRoofTileGenerator,
        whiteHouseTileGenerator,
        brownHouseTileGenerator,
        retailTileGenerator,
    )
}
