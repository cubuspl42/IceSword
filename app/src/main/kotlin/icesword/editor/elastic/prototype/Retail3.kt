package icesword.editor.elastic.prototype

import icesword.editor.ElasticMetaTilesGenerator
import icesword.editor.retails.Retail
import icesword.editor.retails.Retail3
import icesword.editor.retails.retail3DarkSpikesPattern
import icesword.editor.retails.retail3LightSpikesPattern
import icesword.editor.retails.retail3RockLightPattern
import icesword.geometry.IntSize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Spikes")
object Retail3DarkSpikesPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(4, 2)

    private val generator = retail3DarkSpikesPattern.toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticMetaTilesGenerator {
        if (retail !is Retail3) throw UnsupportedOperationException()
        return generator
    }
}

@Serializable
@SerialName("LightSpikes")
object Retail3LightSpikesPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(4, 2)

    private val generator = retail3LightSpikesPattern.toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticMetaTilesGenerator {
        if (retail !is Retail3) throw UnsupportedOperationException()
        return generator
    }
}

@Serializable
@SerialName("RockLight")
object Retail3RockLightPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(4, 1)

    private val generator = retail3RockLightPattern.toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticMetaTilesGenerator {
        if (retail !is Retail3) throw UnsupportedOperationException()
        return generator
    }
}
