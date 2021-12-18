package icesword.editor.elastic.prototype

import icesword.editor.ElasticGenerator
import icesword.editor.retails.Retail
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

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}

@Serializable
@SerialName("Retail1Spikes")
object Retail1SpikesPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(4, 2)

    private val generator = retail1SpikesPattern.toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}
