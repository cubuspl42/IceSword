package icesword.editor.elastic.prototype

import icesword.editor.ElasticGenerator
import icesword.editor.MetaTile
import icesword.editor.elastic.ElasticStructurePattern
import icesword.editor.elastic.ElasticStructurePatternOrientation
import icesword.editor.elastic.RectangularMetaTilePattern
import icesword.editor.retails.Retail
import icesword.editor.retails.Retail3
import icesword.editor.retails.Retail4
import icesword.editor.retails.retail1PlatformPattern
import icesword.editor.retails.retail3DarkSpikesPattern
import icesword.editor.retails.retail3LightSpikesPattern
import icesword.editor.retails.retail3RockLightPattern
import icesword.editor.retails.retail4TreeLog
import icesword.geometry.IntSize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Retail4TreeLog")
object Retail4TreeLogPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(1, 4)

    private val generator = retail4TreeLog.toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}

@Serializable
@SerialName("Retail4WoodenPlatform")
object Retail4WoodenPlatformPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(3, 1)

    private val generator = ElasticStructurePattern(
        startingPattern = RectangularMetaTilePattern(
            metaTiles = listOf(
                Retail4.MetaTiles.WoodenPlatform.left,
            ),
            width = 1,
        ),
        repeatingPattern = RectangularMetaTilePattern(
            metaTiles = listOf(
                Retail4.MetaTiles.WoodenPlatform.center,
            ),
            width = 1,
        ),
        endingPattern = RectangularMetaTilePattern(
            metaTiles = listOf(
                Retail4.MetaTiles.WoodenPlatform.right,
            ),
            width = 1,
        ),
        orientation = ElasticStructurePatternOrientation.Horizontal,
    ).toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}
