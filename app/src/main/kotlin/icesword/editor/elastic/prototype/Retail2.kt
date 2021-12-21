package icesword.editor.elastic.prototype

import icesword.editor.ElasticGenerator
import icesword.editor.MetaTile
import icesword.editor.elastic.ElasticRectangularFragment
import icesword.editor.elastic.ElasticRectangularPattern
import icesword.editor.retails.Retail
import icesword.editor.retails.Retail2
import icesword.editor.retails.Retail2.MetaTiles.DoublePile
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

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
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
                Retail2.MetaTiles.battlement, Retail2.MetaTiles.battlement,
                MetaTile(70), MetaTile(71),
                MetaTile(75), MetaTile(76),
                MetaTile.None, MetaTile(80),
            ),
            width = 2,
            height = 4,
        ),
        topCenter = ElasticRectangularFragment(
            metaTiles = listOf(
                Retail2.MetaTiles.battlement,
                MetaTile(72),
                MetaTile(77),
                MetaTile(81),
            ),
            width = 1,
            height = 4,
        ),
        topRight = ElasticRectangularFragment(
            metaTiles = listOf(
                Retail2.MetaTiles.battlement, Retail2.MetaTiles.battlement,
                MetaTile(73), MetaTile(74),
                MetaTile(78), MetaTile(79),
                MetaTile(82), MetaTile.None,
            ),
            width = 2,
            height = 4,
        ),
        centerLeft = ElasticRectangularFragment(
            metaTiles = listOf(
                MetaTile.None, MetaTile(96)
            ),
            width = 2,
            height = 1,
        ),
        center = ElasticRectangularFragment.ofSingle(MetaTile(77)),
        centerRight = ElasticRectangularFragment(
            metaTiles = listOf(
                MetaTile(97), MetaTile.None,
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
