package icesword.editor.elastic.prototype

import icesword.editor.ElasticGenerator
import icesword.editor.MetaTile
import icesword.editor.retails.Retail
import icesword.editor.retails.Retail3
import icesword.editor.retails.retail3SpikesPattern
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Spikes")
object Retail3SpikesPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(4, 2)

    private val generator = retail3SpikesPattern.toElasticGenerator()

    override fun buildGenerator(retail: Retail): ElasticGenerator {
        if (retail !is Retail3) throw UnsupportedOperationException()
        return generator
    }
}
