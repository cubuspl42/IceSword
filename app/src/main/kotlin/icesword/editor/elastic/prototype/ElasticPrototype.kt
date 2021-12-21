package icesword.editor.elastic.prototype

import icesword.editor.ElasticGenerator
import icesword.editor.ElasticMetaTilesGenerator
import icesword.editor.MetaTile
import icesword.editor.WapObjectPropsData
import icesword.editor.elastic.ElasticRectangularFragment
import icesword.editor.elastic.ElasticRectangularPattern
import icesword.editor.retails.Retail
import icesword.editor.retails.Retail1.MetaTiles.Column
import icesword.editor.retails.Retail3
import icesword.editor.retails.RetailLadderPrototype
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
sealed class ElasticPrototype {
    abstract val defaultSize: IntSize

    abstract fun buildGenerator(retail: Retail): ElasticGenerator
}

@Serializable
@SerialName("Log")
object LogPrototype : ElasticPrototype() {
    private object Generator : ElasticMetaTilesGenerator() {
        override fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile> =
            (0 until size.height).flatMap(::logLevel).toMap()
    }

    private fun logLevel(i: Int): Set<Pair<IntVec2, MetaTile>> = setOf(
        IntVec2(-1, i) to MetaTile.LogLeft,
        IntVec2(0, i) to MetaTile.Log,
        IntVec2(1, i) to MetaTile.LogRight,
    )

    override val defaultSize: IntSize = IntSize(1, 4)

    override fun buildGenerator(retail: Retail): ElasticMetaTilesGenerator {
        if (retail !is Retail3) throw UnsupportedOperationException()
        return Generator
    }
}

@Serializable
@SerialName("TreeCrown")
object TreeCrownPrototype : ElasticPrototype() {
    private object Generator : ElasticMetaTilesGenerator() {
        override fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile> {
            val columns =
                listOf(
                    setOf(
                        IntVec2(0, 0) to MetaTile.LeavesUpperLeft,
                        IntVec2(0, 1) to MetaTile.LeavesLowerLeft,
                    ),
                ) +
                        (1..(size.width - 2)).map { j ->

                            setOf(
                                IntVec2(j, 0) to MetaTile.LeavesUpper,
                                IntVec2(j, 1) to MetaTile.LeavesLower,
                            )
                        } +
                        listOf(
                            setOf(
                                IntVec2(size.width - 1, 0) to MetaTile.LeavesUpperRight,
                                IntVec2(size.width - 1, 1) to MetaTile.LeavesLowerRight,
                            ),
                        )

            return columns.take(size.width).flatten().toMap()
        }
    }

    override val defaultSize: IntSize = IntSize(5, 2)

    override fun buildGenerator(retail: Retail): ElasticMetaTilesGenerator {
        if (retail !is Retail3) throw UnsupportedOperationException()
        return Generator
    }
}

@Serializable
@SerialName("Ladder")
object LadderPrototype : ElasticPrototype() {
    override val defaultSize: IntSize = IntSize(1, 4)

    override fun buildGenerator(retail: Retail): ElasticMetaTilesGenerator {
        if (retail !is RetailLadderPrototype) throw UnsupportedOperationException()
        return retail.ladderGenerator
    }
}
