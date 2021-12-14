package icesword.editor

import icesword.ImageSetId
import icesword.editor.knot_mesh.StructurePattern
import icesword.editor.retails.retail1.retail1StructurePatterns
import icesword.editor.retails.retail3.retail3StructurePatterns
import icesword.geometry.IntVec2
import icesword.wwd.Wwd

interface RetailLadderPrototype {
    val ladderTop: MetaTile
    val ladder: MetaTile
    val ladderBottom: MetaTile
}

sealed class Retail(
    val naturalIndex: Int,
) {
    companion object {
        // TODO: Nuke
        val theRetail = Retail1
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

    object Retail1 : Retail(naturalIndex = 1), RetailLadderPrototype {
        override val ladderTop = MetaTile(310)

        override val ladder = MetaTile(311)

        override val ladderBottom = MetaTile(312)

        override val elevatorPrototype = ElevatorPrototype(
            elevatorImageSetId = ImageSetId(
                fullyQualifiedId = "LEVEL1_IMAGES_ELEVATORS",
            ),
            wwdObjectPrototype = Wwd.Object_.empty().copy(
                logic = encode("Elevator"),
                imageSet = encode("LEVEL_ELEVATORS"),
            ),
        )

        override val structurePatterns: List<StructurePattern> =
            retail1StructurePatterns
    }

    object Retail2 : Retail(naturalIndex = 2)

    object Retail3 : Retail(naturalIndex = 3), RetailLadderPrototype {
        override val ladderTop = MetaTile(668)

        override val ladder = MetaTile(669)

        override val ladderBottom = MetaTile(670)

        override val elevatorPrototype = ElevatorPrototype(
            elevatorImageSetId = ImageSetId(
                fullyQualifiedId = "LEVEL3_IMAGES_ELEVATOR1",
            ),
            wwdObjectPrototype = Wwd.Object_.empty().copy(
                logic = encode("Elevator"),
                imageSet = encode("LEVEL_ELEVATOR1"),
            ),
        )

        override val structurePatterns: List<StructurePattern> =
            retail3StructurePatterns
    }

    object Retail4 : Retail(naturalIndex = 4)
    object Retail5 : Retail(naturalIndex = 5)
    object Retail6 : Retail(naturalIndex = 6)
    object Retail7 : Retail(naturalIndex = 7)
    object Retail8 : Retail(naturalIndex = 8)
    object Retail9 : Retail(naturalIndex = 9)
    object Retail10 : Retail(naturalIndex = 10)
    object Retail11 : Retail(naturalIndex = 11)
    object Retail12 : Retail(naturalIndex = 12)
    object Retail13 : Retail(naturalIndex = 13)
    object Retail14 : Retail(naturalIndex = 14)
}
