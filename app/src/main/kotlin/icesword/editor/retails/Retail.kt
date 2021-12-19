package icesword.editor.retails

import icesword.editor.ElasticGenerator
import icesword.editor.ElevatorPrototype
import icesword.editor.KnotMetaTileBuilder
import icesword.editor.KnotPrototype
import icesword.editor.MetaTile
import icesword.editor.TileGenerator
import icesword.editor.TileGeneratorContext
import icesword.editor.elastic.ElasticStructurePattern
import icesword.editor.elastic.ElasticStructurePatternOrientation
import icesword.editor.elastic.RectangularMetaTilePattern
import icesword.editor.knot_mesh.KnotStructurePattern
import icesword.geometry.IntVec2

object LadderElasticGenerator{
    fun build(ladder: Retail.LadderPattern): ElasticGenerator =
        ElasticStructurePattern(
            startingPattern = RectangularMetaTilePattern(
                metaTiles = listOf(ladder.top),
                width = 1,
            ),
            repeatingPattern = RectangularMetaTilePattern(
                metaTiles = listOf(ladder.center),
                width = 1,
            ),
            endingPattern = RectangularMetaTilePattern(
                metaTiles = listOf(ladder.bottom),
                width = 1,
            ),
            orientation = ElasticStructurePatternOrientation.Vertical,
        ).toElasticGenerator()
}

interface RetailLadderPrototype {
    val ladderGenerator: ElasticGenerator
}

sealed class Retail(
    val naturalIndex: Int,
) {
    companion object {
        fun fromNaturalIndex(retailNaturalIndex: Int): Retail = when (retailNaturalIndex) {
            1 -> Retail1
            2 -> Retail2
            3 -> Retail3
            4 -> Retail4
            5 -> Retail5
            6 -> Retail6
            7 -> Retail7
            8 -> Retail8
            9 -> Retail9
            10 -> Retail10
            11 -> Retail11
            12 -> Retail12
            13 -> Retail13
            14 -> Retail14
            else -> throw IllegalArgumentException("No such retail: $retailNaturalIndex")
        }
    }

    interface LadderPattern {
        val top: MetaTile
        val center: MetaTile
        val bottom: MetaTile
    }

    open val elevatorPrototype: ElevatorPrototype
        get() = TODO()

    open val knotStructurePatterns: List<KnotStructurePattern> =
        emptyList()

    open val tileGenerator: TileGenerator = object : TileGenerator {
        override fun buildTile(context: TileGeneratorContext): Int? = null
    }

    fun buildKnotMetaTileBuilder(): KnotMetaTileBuilder = object : KnotMetaTileBuilder {
        private val structureMatchers =
            knotStructurePatterns.map { it.toKnotMetaTileBuilder() }

        override fun buildMetaTile(
            tileCoord: IntVec2,
            globalKnots: Map<IntVec2, KnotPrototype>,
        ): MetaTile? = structureMatchers.firstNotNullOfOrNull {
            it.buildMetaTile(
                tileCoord = tileCoord,
                globalKnots = globalKnots,
            )
        }
    }
}
