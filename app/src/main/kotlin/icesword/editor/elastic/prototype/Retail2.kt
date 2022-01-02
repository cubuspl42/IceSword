package icesword.editor.elastic.prototype

import icesword.editor.entities.ElasticGenerator
import icesword.editor.entities.ElasticMetaTilesGenerator
import icesword.editor.MetaTile
import icesword.editor.entities.WapObjectPropsData
import icesword.editor.elastic.ElasticRectangularFragment
import icesword.editor.elastic.ElasticRectangularPattern
import icesword.editor.retails.Retail
import icesword.editor.retails.Retail2.MetaTiles
import icesword.editor.retails.Retail2.MetaTiles.DoublePile
import icesword.editor.retails.Retail2.MetaTiles.SinglePile
import icesword.editor.retails.Retail2.MetaTiles.Tower
import icesword.editor.retails.retail2PlatformPattern
import icesword.editor.retails.retail2TowerTop
import icesword.geometry.IntSize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Level2Platform")
object Retail2PlatformPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(5, 2)

    private val generator = retail2PlatformPattern.toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticMetaTilesGenerator = generator
}

@Serializable
@SerialName("Level2DoublePile")
object Retail2DoublePilePrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(1, 3)

    private val generator = ElasticRectangularPattern(
        topCenter = ElasticRectangularFragment.ofSingle(DoublePile.top),
        center = ElasticRectangularFragment.ofSingle(DoublePile.core),
        bottomCenter = ElasticRectangularFragment.ofSingle(DoublePile.bottom),
        centerHorizontalRepeatingWidth = 1,
        topStaticHeight = 1,
        centerVerticalRepeatingHeight = 1,
        bottomStaticHeight = 1,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}

@Serializable
@SerialName("Retail2SinglePile")
object Retail2SinglePilePrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(2, 4)

    private val generator = ElasticRectangularPattern(
        topCenter = ElasticRectangularFragment(
            metaTiles = listOf(
                SinglePile.topOuter,
                SinglePile.topInner,
            ),
            width = 1,
            height = 2,
        ),
        center = ElasticRectangularFragment.ofSingle(
            SinglePile.center,
        ),
        bottomCenter = ElasticRectangularFragment(
            metaTiles = listOf(
                SinglePile.bottomInner,
                SinglePile.bottomOuter,
            ),
            width = 1,
            height = 2,
        ),
        centerHorizontalRepeatingWidth = 1,
        topStaticHeight = 2,
        centerVerticalRepeatingHeight = 1,
        bottomStaticHeight = 2,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}

@Serializable
@SerialName("Retail2TowerTop")
object Retail2TowerTopPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(5, 3)

    private val generator = retail2TowerTop.toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}

@Serializable
@SerialName("Retail2Tower")
object Retail2TowerPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(5, 5)

    private val generator = ElasticRectangularPattern(
        topLeft = ElasticRectangularFragment(
            metaTiles = listOf(
                MetaTiles.battlement, MetaTiles.battlement,
                Tower.Platform.topLeftOuter, Tower.Platform.topLeftInner,
                Tower.Platform.bottomLeftOuter, Tower.Platform.bottomLeftInner,
                MetaTile.None, Tower.Column.topLeft,
            ),
            width = 2,
            height = 4,
        ),
        topCenter = ElasticRectangularFragment(
            metaTiles = listOf(
                MetaTiles.battlement,
                Tower.Platform.topCenter,
                Tower.core,
                Tower.Column.topCenter,
            ),
            width = 1,
            height = 4,
        ),
        topRight = ElasticRectangularFragment(
            metaTiles = listOf(
                MetaTiles.battlement, MetaTiles.battlement,
                Tower.Platform.topRightInner, Tower.Platform.topRightOuter,
                Tower.Platform.bottomRightInner, Tower.Platform.bottomRightOuter,
                Tower.Column.topRight, MetaTile.None,
            ),
            width = 2,
            height = 4,
        ),
        centerLeft = ElasticRectangularFragment(
            metaTiles = listOf(
                MetaTile.None, Tower.Column.left
            ),
            width = 2,
            height = 1,
        ),
        center = ElasticRectangularFragment.ofSingle(Tower.core),
        centerRight = ElasticRectangularFragment(
            metaTiles = listOf(
                Tower.Column.right, MetaTile.None,
            ),
            width = 2,
            height = 1,
        ),
        leftStaticWidth = 2,
        centerHorizontalRepeatingWidth = 1,
        rightStaticWidth = 2,
        topStaticHeight = 4,
        centerVerticalRepeatingHeight = 1,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}

@Serializable
@SerialName("Retail2Goo")
object Retail2GooPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(3, 4)

    private val gooCoverupObject = WapObjectPropsData(
        x = 32,
        y = 154,
        i = -1,
        logic = "GooCoverup",
    )

    private val generator = ElasticRectangularPattern(
        topLeft = ElasticRectangularFragment(
            metaTiles = listOf(
                MetaTiles.Goo.left,
                DoublePile.core,
                MetaTiles.death,
                SinglePile.center,
            ),
            wapObject = gooCoverupObject.copy(
                imageSet = "LEVEL_GOOLEFT",
            ),
            width = 1,
            height = 4,
        ),
        topCenter = ElasticRectangularFragment(
            metaTiles = listOf(
                MetaTiles.Goo.center,
                DoublePile.core,
                MetaTiles.death,
                SinglePile.center,
            ),
            wapObject = gooCoverupObject.copy(
                imageSet = "LEVEL_GOOCOVERUP",
            ),
            width = 1,
            height = 4,
        ),
        topRight = ElasticRectangularFragment(
            metaTiles = listOf(
                MetaTiles.Goo.right,
                DoublePile.core,
                MetaTiles.death,
                SinglePile.center,
            ),
            wapObject = gooCoverupObject.copy(
                imageSet = "LEVEL_GOORIGHT",
            ),
            width = 1,
            height = 4,
        ),
        leftStaticWidth = 1,
        centerHorizontalRepeatingWidth = 1,
        rightStaticWidth = 1,
        topStaticHeight = 4,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}
