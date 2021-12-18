package icesword.editor.elastic.prototype

import icesword.editor.ElasticGenerator
import icesword.editor.retails.Retail
import icesword.editor.retails.Retail3
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
