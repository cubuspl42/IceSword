package icesword.editor.retails

import icesword.ImageSetId
import icesword.editor.ElevatorPrototype
import icesword.editor.MetaTile
import icesword.editor.TileGenerator
import icesword.editor.TileGeneratorContext
import icesword.editor.encode
import icesword.editor.knot_mesh.KnotStructurePattern
import icesword.editor.retails.retail3.retail3KnotStructurePatterns
import icesword.editor.retails.retail3.retail3LadderPattern
import icesword.wwd.Wwd

private val retailTileGenerator = object : TileGenerator {
    override fun buildTile(context: TileGeneratorContext): Int? = context.run {
        val metaTiles = Retail3.MetaTiles
        val ladder = Retail3.MetaTiles.Ladder

        when {
            // Tree crown
            containsAll(MetaTile.Log, MetaTile.LeavesUpper) -> 647
            containsAll(MetaTile.Log, MetaTile.LeavesLower) -> 653
            containsAll(MetaTile.LogLeft, MetaTile.LeavesLower) -> 652
            containsAll(MetaTile.LogRight, MetaTile.LeavesLower) -> 654

            // Side tree crown tip
            containsAll(MetaTile.Log, MetaTile.LeavesUpperRight) -> 663
            containsAll(MetaTile.Log, MetaTile.LeavesLowerRight) -> 665

            // Tree branches
            containsAll(MetaTile.Log, MetaTile.LeavesUpperLeft) -> 659
            containsAll(MetaTile.Log, MetaTile.LeavesLowerLeft) -> 661

            // Tree root
            containsAll(MetaTile.Log, metaTiles.grass) -> 666

            // Ladder connection to tree crown
            // Tile 660 is like 644, but with the "Climb" attribute
            containsAll(ladder.top, MetaTile.LeavesUpper) -> 660
            containsAll(ladder.core, MetaTile.LeavesLower) -> 667

            // Spikes
            containsAll(MetaTile.SpikeTop, MetaTile.RockRightSide) -> 712
            containsAll(MetaTile.SpikeBottom, MetaTile.RockLowerLeftCorner) -> 713

            containsAll(MetaTile.SpikeBottom, MetaTile.RockTop) -> 704

            containsAll(MetaTile.SpikeTop, MetaTile.RockLeftSideOuter) -> 701
            containsAll(MetaTile.SpikeTop, MetaTile.RockLeftSideInner) -> 702
            containsAll(MetaTile.SpikeBottom, MetaTile.RockLowerRightCornerOuter) -> 704
            containsAll(MetaTile.SpikeBottom, MetaTile.RockLowerRightCornerInner) -> 709 // 696

            else -> null
        }
    }
}

object Retail3 : Retail(naturalIndex = 3), RetailLadderPrototype {
    object MetaTiles {
        object Ladder {
            val top = MetaTile(668)

            val core = MetaTile(669)
        }

        val grass = MetaTile(604)
    }

    override val ladderGenerator = retail3LadderPattern.toElasticGenerator()

    override val elevatorPrototype = ElevatorPrototype(
        elevatorImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL3_IMAGES_ELEVATOR1",
        ),
        wwdObjectPrototype = Wwd.Object_.empty().copy(
            logic = encode("Elevator"),
            imageSet = encode("LEVEL_ELEVATOR1"),
        ),
    )

    override val knotStructurePatterns: List<KnotStructurePattern> =
        retail3KnotStructurePatterns

    override val tileGenerator: TileGenerator = retailTileGenerator
}
