package icesword.editor.entities.elastic.prototype

import icesword.editor.entities.ElasticGenerator
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
import icesword.editor.retails.Retail6.MetaTiles
import icesword.editor.retails.Retail6.MetaTiles.BackWall
import icesword.editor.retails.Retail6.MetaTiles.BrownHouse
import icesword.editor.retails.Retail6.MetaTiles.Fence
import icesword.editor.retails.Retail6.MetaTiles.HorizontalRoof
import icesword.editor.retails.Retail6.MetaTiles.House
import icesword.editor.retails.Retail6.MetaTiles.Pavement
import icesword.editor.retails.Retail6.MetaTiles.TunnelBricksFloor
import icesword.editor.retails.Retail6.MetaTiles.TunnelPlateFloor
import icesword.editor.retails.Retail6.MetaTiles.TunnelTube
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
                HorizontalRoof.supportLeft,
            ),
            width = 2,
        ),
        repeatingPattern = LinearMetaTilePattern(
            metaTiles = listOf(
                HorizontalRoof.topCenter,
                HorizontalRoof.shadow,
            ),
            width = 2,
        ),
        endingPattern = LinearMetaTilePattern(
            metaTiles = listOf(
                HorizontalRoof.topRightInner,
                HorizontalRoof.supportRightInner,
                HorizontalRoof.topRightOuter,
                HorizontalRoof.supportRightOuter,
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
        ladder = MetaTiles.Ladder,
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
            house.core,
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

@Serializable
@SerialName("Retail6TunnelTube")
object Retail6TunnelTubePrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(3, 2)

    private val generator = ElasticGenerator.fromVerticalPattern(
        top = ElasticRectangularFragment(
            metaTiles = listOf(
                TunnelTube.topLeft, TunnelTube.topCenter, TunnelTube.topRight, TunnelPlateFloor.right,
            ),
            width = 4,
            height = 1,
        ),
        center = ElasticRectangularFragment(
            metaTiles = listOf(
                TunnelTube.left, TunnelTube.center, TunnelTube.right,
            ),
            width = 3,
            height = 1,
        ),
        staticWidth = 4,
    )

    override fun buildGenerator(retail: Retail) = generator
}

@Serializable
@SerialName("Retail6TunnelTubeCover")
object Retail6TunnelTubeCoverPrototype : ElasticPrototype() {
    val tubeCover = WapObjectPropsData(
        x = 96,
        y = 53,
        z = 8000,
        i = -1,
        logic = "DoNothingNormal",
        imageSet = "LEVEL_HORIZONTALTUBEALL",
    )

    override val defaultSize: IntSize = IntSize(3, 2)

    private val generator = ElasticGenerator.fromVerticalPattern(
        center = ElasticRectangularFragment(
            metaTiles = List(3) { MetaTile.None },
            width = 3,
            height = 1,
            wapObject = tubeCover,
        ),
        staticWidth = 3,
    )

    override fun buildGenerator(retail: Retail) = generator
}

@Serializable
@SerialName("Retail6TunnelTubeCoverGap")
object Retail6TunnelTubeCoverGapPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(3, 2)

    private val generator = ElasticGenerator.fromVerticalPattern(
        center = ElasticRectangularFragment(
            metaTiles = List(6) { MetaTile.None },
            width = 3,
            height = 2,
            wapObject = Retail6TunnelTubeCoverPrototype.tubeCover,
        ),
        staticWidth = 3,
    )

    override fun buildGenerator(retail: Retail) = generator
}

@Serializable
@SerialName("Retail6TunnelBricksFloor")
object Retail6TunnelBricksFloorPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(2, 1)

    private val generator = ElasticGenerator.fromHorizontalPattern(
        center = ElasticRectangularFragment.ofSingle(
            TunnelBricksFloor.core,
        ),
        right = ElasticRectangularFragment.ofSingle(
            TunnelBricksFloor.right,
        ),
        staticHeight = 1,
    )

    override fun buildGenerator(retail: Retail) = generator
}

@Serializable
@SerialName("Retail6TunnelPlateFloor")
object Retail6TunnelPlateFloorPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(2, 1)

    private val generator = ElasticGenerator.fromHorizontalPattern(
        center = ElasticRectangularFragment.ofSingle(
            TunnelPlateFloor.core,
        ),
        right = ElasticRectangularFragment.ofSingle(
            TunnelPlateFloor.right,
        ),
        staticHeight = 1,
    )

    override fun buildGenerator(retail: Retail) = generator
}

@Serializable
@SerialName("Retail6Goo")
object Retail6GooPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(4, 3)

    private val gooCoverupObject = WapObjectPropsData(
        y = 160,
        z = 8000,
        i = -1,
        logic = "GooCoverup",
    )

    private val generator = ElasticGenerator.fromHorizontalPattern(
        left = ElasticRectangularFragment(
            metaTiles = listOf(
                MetaTile(138),
                MetaTile(148),
                MetaTiles.death,
            ),
            width = 1,
            height = 3,
            wapObject = gooCoverupObject.copy(
                x = 17,
                imageSet = "LEVEL_GOOCOVERLEFT",
            ),
        ),
        center = ElasticRectangularFragment(
            metaTiles = listOf(
                MetaTile(163),
                MetaTile(149),
                MetaTiles.death,
            ),
            width = 1,
            height = 3,
            wapObject = gooCoverupObject.copy(
                x = 33,
                imageSet = "LEVEL_GOOCOVERCENTER",
            ),
        ),
        right = ElasticRectangularFragment(
            metaTiles = listOf(
                MetaTile(140), TunnelPlateFloor.right,
                MetaTile(150), MetaTile.None,
                MetaTiles.death, MetaTile.None,
            ),
            width = 2,
            height = 3,
            wapObject = gooCoverupObject.copy(
                x = 65,
                imageSet = "LEVEL_GOOCOVERRIGHT",
            ),
        ),
        staticHeight = 3,
    )

    override fun buildGenerator(retail: Retail) = generator
}

@Serializable
@SerialName("Retail6Sewer")
object Retail6SewerPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(4, 2)

    private val gooCoverupObject = WapObjectPropsData(
        y = 64,
        z = 8000,
        i = -1,
        logic = "GooCoverup",
    )

    private val generator = ElasticGenerator.fromHorizontalPattern(
        left = ElasticRectangularFragment(
            metaTiles = listOf(
                MetaTile(197),
                MetaTiles.death,
            ),
            width = 1,
            height = 2,
            wapObject = gooCoverupObject.copy(
                x = 16,
                imageSet = "LEVEL_SEWERCOVERUP1",
            ),
        ),
        center = ElasticRectangularFragment(
            metaTiles = listOf(
                MetaTile(198),
                MetaTiles.death,
            ),
            width = 1,
            height = 2,
            wapObject = gooCoverupObject.copy(
                x = 32,
                imageSet = "LEVEL_SEWERCOVERUP2",
            ),
        ),
        right = ElasticRectangularFragment(
            metaTiles = listOf(
                MetaTile(199),
                MetaTiles.death,
            ),
            width = 1,
            height = 2,
            wapObject = gooCoverupObject.copy(
                x = 48,
                imageSet = "LEVEL_SEWERCOVERUP3",
            ),
        ),
        staticHeight = 2,
    )

    override fun buildGenerator(retail: Retail) = generator
}

@Serializable
@SerialName("Retail6Plate")
object Retail6PlatePrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(2, 2)

    private val generator = ElasticRectangularPattern(
        center = ElasticRectangularFragment.ofSingle(
            BackWall.plate,
        ),
        centerHorizontalRepeatingWidth = 1,
        centerVerticalRepeatingHeight = 1,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail) = generator
}
