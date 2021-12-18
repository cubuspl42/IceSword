package icesword.editor.elastic.prototype

import icesword.editor.ElasticGenerator
import icesword.editor.retails.Retail
import icesword.editor.retails.retail2PlatformPattern
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
