package icesword.editor.retails

import icesword.ImageSetId
import icesword.editor.entities.CrumblingPegPrototype
import icesword.editor.entities.KnotPrototype
import icesword.editor.MetaTile
import icesword.editor.TileGenerator
import icesword.editor.TileGeneratorContext
import icesword.editor.entities.knot_mesh.KnotStructurePattern
import icesword.editor.entities.knot_mesh.MetaTilePattern1x1
import icesword.editor.entities.knot_mesh.StructureConcavePattern
import icesword.editor.entities.knot_mesh.StructureConvexPattern
import icesword.editor.retails.Retail6.MetaTiles.BackWall
import icesword.editor.retails.Retail6.MetaTiles.Bricks
import icesword.editor.retails.Retail6.MetaTiles.BrownHouse
import icesword.editor.retails.Retail6.MetaTiles.Fence
import icesword.editor.retails.Retail6.MetaTiles.HorizontalRoof
import icesword.editor.retails.Retail6.MetaTiles.House
import icesword.editor.retails.Retail6.MetaTiles.Ladder
import icesword.editor.retails.Retail6.MetaTiles.Pavement
import icesword.editor.retails.Retail6.MetaTiles.ShutterWindow
import icesword.editor.retails.Retail6.MetaTiles.TunnelBricksFloor
import icesword.editor.retails.Retail6.MetaTiles.TunnelPlateFloor
import icesword.editor.retails.Retail6.MetaTiles.TunnelTube
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
)

interface HouseShutterWindowTiles {
    val coreLeftShutter: Int

    val leftInnerLeftShutter: Int

    val coreCore: Int

    val coreRightShutter: Int

    val rightRightShutter: Int
}

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

    val supportLeft: Int

    val support: Int

    val shadow: Int

    val supportRightInner: Int

    val shutterWindow: HouseShutterWindowTiles
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

    override val supportLeft: Int = 44

    override val support: Int = 45

    override val shadow: Int = 42

    override val supportRightInner: Int = 46

    override val shutterWindow: HouseShutterWindowTiles = object : HouseShutterWindowTiles {
        override val coreLeftShutter: Int = 56

        override val leftInnerLeftShutter: Int = 50

        override val coreCore: Int = 51

        override val coreRightShutter: Int = 53

        override val rightRightShutter: Int = 52
    }
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

    override val supportLeft: Int = 75

    override val support: Int = 77

    override val shadow: Int = 76

    override val supportRightInner: Int = 73

    override val shutterWindow: HouseShutterWindowTiles = object : HouseShutterWindowTiles {
        override val coreLeftShutter: Int = 71

        override val leftInnerLeftShutter: Int = 80

        override val coreCore: Int = 81

        override val coreRightShutter: Int = 82

        override val rightRightShutter: Int = 83
    }
}

val bricksTileGenerator = TileGenerator.chained(
    object : TileGenerator {
        override fun buildTile(context: TileGeneratorContext): Int? = context.run {
            when {
                // Bricks / pavement

                containsAll(Bricks.center, Pavement.left) -> 110
                containsAll(Bricks.left, Pavement.rightOuter) -> 203

                // Bricks / plate

                containsAll(Bricks.topLeft, BackWall.plate) -> 106
                containsAll(Bricks.topRight, BackWall.plate) -> 105
                containsAll(Bricks.left, BackWall.plate) -> 114
                containsAll(Bricks.bottomLeft, BackWall.plate) -> 116

                else -> null
            }
        }
    },
    TileGenerator.forwardAll(
        Bricks.topCenter,
        Bricks.center,
        Bricks.concaveBottomRight,
    )
)

fun buildHouseTileGenerator(
    house: House,
    houseTiles: HouseTiles,
): TileGenerator = TileGenerator.chained(
    object : TileGenerator {
        override fun buildTile(context: TileGeneratorContext): Int? = context.run {
            when {
                // House / shutter window

                containsAll(house.core, ShutterWindow.leftShutter) -> houseTiles.shutterWindow.coreLeftShutter
                containsAll(house.leftInner, ShutterWindow.leftShutter) -> houseTiles.shutterWindow.leftInnerLeftShutter

                containsAll(house.core, ShutterWindow.core) -> houseTiles.shutterWindow.coreCore

                containsAll(house.core, ShutterWindow.rightShutter) -> houseTiles.shutterWindow.coreRightShutter
                containsAll(house.right, ShutterWindow.rightShutter) -> houseTiles.shutterWindow.rightRightShutter

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

                // House / horizontal roof

                containsAll(house.leftInner, HorizontalRoof.supportLeft) -> houseTiles.supportLeft
                containsAll(house.core, HorizontalRoof.shadow) -> houseTiles.shadow
                containsAll(house.right, HorizontalRoof.supportRightInner) -> houseTiles.supportRightInner

                else -> null
            }
        }
    },
    TileGenerator.forwardAll(
        house.topLeft,
        house.topCenter,
        house.topRight,
        house.leftOuter,
        house.leftInner,
        house.core,
        house.right,
        house.bottomLeftOuter,
        house.bottomLeftInner,
        house.bottomCenter,
        house.bottomRight,
    ),
)

private val whiteHouseTileGenerator = buildHouseTileGenerator(
    house = WhiteHouse,
    houseTiles = whiteHouseTiles,
)

private val brownHouseTileGenerator = buildHouseTileGenerator(
    house = BrownHouse,
    houseTiles = brownHouseTiles,
)

private val tunnelTubeTileGenerator = TileGenerator.forwardAll(
    TunnelTube.topLeft,
    TunnelTube.topCenter,
    TunnelTube.topRight,
    TunnelTube.left,
    TunnelTube.center,
    TunnelTube.right,
)

private val ladderTileGenerator = TileGenerator.chained(
    object : TileGenerator {
        override fun buildTile(context: TileGeneratorContext): Int? = context.run {
            when {
                // Plate / ladder

                containsAll(BackWall.plate, Ladder.center) -> 165
                containsAll(BackWall.plate, Ladder.bottom) -> 166

                // Tunnel tube / ladder

                containsAll(TunnelTube.topCenter, Ladder.top) -> 139
                containsAll(TunnelTube.center, Ladder.center) -> 143

                else -> null
            }
        }
    },
    TileGenerator.forwardAll(
        Ladder.top,
        Ladder.center,
        Ladder.bottom,
    ),
)

private val pavementTileGenerator = TileGenerator.chained(
    object : TileGenerator {
        override fun buildTile(context: TileGeneratorContext): Int? = context.run {
            when {
                // Pavement / fence

                containsAll(Pavement.center, Fence.bottom) -> 27

                else -> null
            }
        }
    },
    TileGenerator.forwardAll(
        Pavement.left,
        Pavement.center,
        Pavement.rightInner,
        Pavement.rightOuter,
    ),
)

private val tunnelFloorTileGenerator = TileGenerator.chained(
    object : TileGenerator {
        override fun buildTile(context: TileGeneratorContext): Int? = context.run {
            when {
                // Tunnel floor / tunnel floor

                containsAll(TunnelBricksFloor.core, TunnelPlateFloor.right) -> 129
                containsAll(TunnelPlateFloor.core, TunnelBricksFloor.right) -> 134

                // Bricks / tunnel floor

                containsAll(Bricks.left, TunnelBricksFloor.right) -> 132
                containsAll(Bricks.concaveBottomRight, TunnelBricksFloor.right) -> 132
                containsAll(Bricks.left, TunnelPlateFloor.right) -> 141
                containsAll(Bricks.concaveBottomRight, TunnelPlateFloor.right) -> 141

                else -> null
            }
        }
    },
    TileGenerator.forwardAll(
        TunnelBricksFloor.core,
        TunnelBricksFloor.right,
        TunnelPlateFloor.core,
        TunnelPlateFloor.right,
    ),
)

private val retailTileGenerator = object : TileGenerator {
    override fun buildTile(context: TileGeneratorContext): Int? = null
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

            val supportLeft = MetaTile(44)

            val shadow = MetaTile(42)

            val supportRightInner = MetaTile(46)

            val supportRightOuter = MetaTile(47)
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

            val concaveBottomRight = MetaTile(115)
        }

        object ShutterWindow {
            val leftShutter = MetaTile(whiteHouseTiles.shutterWindow.coreLeftShutter)

            val core = MetaTile(whiteHouseTiles.shutterWindow.coreCore)

            val rightShutter = MetaTile(whiteHouseTiles.shutterWindow.coreRightShutter)
        }

        interface House {
            val topLeft: MetaTile

            val topCenter: MetaTile

            val topRight: MetaTile

            val leftOuter: MetaTile

            val leftInner: MetaTile

            val core: MetaTile

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

            override val core: MetaTile = MetaTile(49)

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

            override val core: MetaTile = MetaTile(72)

            override val right: MetaTile = MetaTile(88)

            override val bottomLeftOuter: MetaTile = MetaTile(brownHouseTiles.bottomLeftOuterWall)

            override val bottomLeftInner: MetaTile = MetaTile(brownHouseTiles.bottomLeftInner)

            override val bottomCenter: MetaTile = MetaTile(brownHouseTiles.bottomCenter)

            override val bottomRight: MetaTile = MetaTile(brownHouseTiles.bottomRight)
        }

        object TunnelTube {
            val topLeft = MetaTile(138)

            val topCenter = MetaTile(163)

            val topRight = MetaTile(140)

            val left = MetaTile(142)

            val center = MetaTile(147)

            val right = MetaTile(144)
        }

        object TunnelBricksFloor {
            val core = MetaTile(133)

            val right = MetaTile(133)
        }

        object TunnelPlateFloor {
            val core = MetaTile(137)

            val right = MetaTile(137)
        }

        object BackWall {
            val plate = MetaTile(128)
        }

        val death = MetaTile(145)
    }

    override val knotStructurePatterns: List<KnotStructurePattern> =
        listOf(bricksPattern)

    override val tileGenerator: TileGenerator = TileGenerator.chained(
        ladderTileGenerator,
        horizontalRoofTileGenerator,
        whiteHouseTileGenerator,
        brownHouseTileGenerator,
        tunnelTubeTileGenerator,
        tunnelFloorTileGenerator,
        bricksTileGenerator,
        pavementTileGenerator,
        retailTileGenerator,
    )

    val crumblingPegPrototype = CrumblingPegPrototype(
        imageSetId = ImageSetId(fullyQualifiedId = "LEVEL6_IMAGES_BREAKINGLEDGE"),
        shortImageSetId = "LEVEL_BREAKINGLEDGE",
    )
}
