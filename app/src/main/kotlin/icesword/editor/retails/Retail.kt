package icesword.editor.retails

import icesword.editor.ElevatorPrototype
import icesword.editor.KnotMetaTileBuilder
import icesword.editor.KnotPrototype
import icesword.editor.MetaTile
import icesword.editor.knot_mesh.StructurePattern
import icesword.geometry.IntVec2

interface RetailLadderPrototype {
    val ladderTop: MetaTile
    val ladder: MetaTile
    val ladderBottom: MetaTile
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

    open val elevatorPrototype: ElevatorPrototype
        get() = TODO()

    open val structurePatterns: List<StructurePattern> =
        emptyList()

    // TODO: Elastics

    fun buildKnotMetaTileBuilder(): KnotMetaTileBuilder = object : KnotMetaTileBuilder {
        private val structureMatchers =
            structurePatterns.map { it.toKnotMetaTileBuilder() }

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
