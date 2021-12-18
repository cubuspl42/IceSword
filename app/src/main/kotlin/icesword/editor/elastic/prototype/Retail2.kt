package icesword.editor.elastic.prototype

import icesword.editor.ElasticGenerator
import icesword.editor.retails.Retail
import icesword.editor.retails.doublePilePattern
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

    private val generator = doublePilePattern.toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}

@Serializable
@SerialName("Level2DoublePile")
object Retail2TowerTopPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(5, 3)

    private val generator = retail2TowerTop.toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator = generator
}
