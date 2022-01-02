package icesword.editor.entities.elastic.prototype

import icesword.editor.entities.ElasticGenerator
import icesword.editor.entities.ElasticMetaTilesGenerator
import icesword.editor.MetaTile
import icesword.editor.entities.WapObjectPropsData
import icesword.editor.entities.elastic.ElasticRectangularFragment
import icesword.editor.entities.elastic.ElasticRectangularPattern
import icesword.editor.retails.Retail
import icesword.editor.retails.Retail1
import icesword.editor.retails.retail1PlatformPattern
import icesword.editor.retails.retail1SpikesPattern
import icesword.geometry.IntSize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Level1Platform")
object Retail1PlatformPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(3, 1)

    private val generator = retail1PlatformPattern.toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticMetaTilesGenerator = generator
}

@Serializable
@SerialName("Retail1Spikes")
object Retail1SpikesPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(4, 2)

    private val generator = retail1SpikesPattern.toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticMetaTilesGenerator = generator
}

@Serializable
@SerialName("Level1Column")
object Retail1ColumnPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(1, 4)

    private val generator = ElasticRectangularPattern(
        topCenter = ElasticRectangularFragment(
            metaTiles = listOf(MetaTile(933)),
            wapObject = WapObjectPropsData(
                x = 28,
                y = 32,
                i = 2,
                logic = "DoNothing",
                imageSet = "LEVEL_ARCHESFRONT",
            ),
            width = 1,
            height = 1,
        ),
        center = ElasticRectangularFragment.ofSingle(MetaTile(934)),
        bottomCenter = ElasticRectangularFragment.ofSingle(Retail1.MetaTiles.Column.bottom),
        centerHorizontalRepeatingWidth = 1,
        topStaticHeight = 1,
        centerVerticalRepeatingHeight = 1,
        bottomStaticHeight = 1,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}
