package icesword.editor.retails

import icesword.ImageSetId
import icesword.editor.entities.CrumblingPegPrototype
import icesword.editor.entities.ElevatorPrototype
import icesword.editor.MetaTile
import icesword.editor.TileGenerator
import icesword.editor.TileGeneratorContext
import icesword.editor.entities.elastic.ElasticLinearPattern
import icesword.editor.entities.elastic.ElasticLinearPatternOrientation
import icesword.editor.entities.elastic.LinearMetaTilePattern
import icesword.editor.entities.encode
import icesword.editor.entities.knot_mesh.KnotStructurePattern
import icesword.editor.retails.Retail3.MetaTiles.Rock
import icesword.editor.retails.retail3.retail3KnotStructurePatterns
import icesword.editor.retails.retail3.retail3LadderPattern
import icesword.wwd.Wwd

private fun spikesPattern(spikes: Retail3.MetaTiles.Spikes): ElasticLinearPattern =
    ElasticLinearPattern(
        startingPattern = LinearMetaTilePattern(
            metaTiles = emptyList(),
            width = 0,
        ),
        repeatingPattern = LinearMetaTilePattern(
            metaTiles = listOf(spikes.top, spikes.bottom),
            width = 2,
        ),
        endingPattern = LinearMetaTilePattern(
            metaTiles = emptyList(),
            width = 0,
        ),
        orientation = ElasticLinearPatternOrientation.Horizontal,
    )

val retail3LightSpikesPattern = spikesPattern(spikes = Retail3.MetaTiles.LightSpikes)

val retail3DarkSpikesPattern = spikesPattern(spikes = Retail3.MetaTiles.DarkSpikes)

val retail3RockLightPattern = ElasticLinearPattern(
    startingPattern = LinearMetaTilePattern(
        metaTiles = listOf(Retail3.MetaTiles.RockLight.left),
        width = 1,
    ),
    repeatingPattern = LinearMetaTilePattern(
        metaTiles = listOf(Retail3.MetaTiles.RockLight.center),
        width = 1,
    ),
    endingPattern = LinearMetaTilePattern(
        metaTiles = listOf(Retail3.MetaTiles.RockLight.right),
        width = 1,
    ),
    orientation = ElasticLinearPatternOrientation.Horizontal,
)

class SpikesTileGenerator(
    private val spikes: Retail3.MetaTiles.Spikes,
    private val tileset: SpikesTileset,
) : TileGenerator {
    data class SpikesTileset(
        val topLeft: Int,
        val bottomLeft: Int,
        val top: Int,
        val bottom: Int,
        val topRightOuter: Int,
        val topRightInner: Int,
        val bottomRightInner: Int,
    )

    override fun buildTile(context: TileGeneratorContext): Int? = context.run {
        when {
            // Spikes

            containsAll(spikes.top, Rock.RightSide) -> tileset.topLeft
            containsAll(spikes.bottom, Rock.LowerLeftCorner) -> tileset.bottomLeft

            containsAll(spikes.bottom, Rock.Top) -> tileset.bottom

            containsAll(spikes.top, Rock.LeftSideOuter) -> tileset.topRightOuter
            containsAll(spikes.top, Rock.LeftSideInner) -> tileset.topRightInner
            containsAll(spikes.bottom, Rock.LowerRightCornerOuter) -> tileset.bottom
            containsAll(spikes.bottom, Rock.LowerRightCornerInner) -> tileset.bottomRightInner

            else -> null
        }
    }
}

private val retailTileGenerator = object : TileGenerator {
    private val lightSpikesTileGenerator = SpikesTileGenerator(
        spikes = Retail3.MetaTiles.LightSpikes,
        tileset = SpikesTileGenerator.SpikesTileset(
            topLeft = 685,
            bottomLeft = 691,
            top = 686,
            bottom = 692,
            topRightOuter = 689,
            topRightInner = 690,
            bottomRightInner = 696,
        )
    )

    private val darkSpikesTileGenerator = SpikesTileGenerator(
        spikes = Retail3.MetaTiles.DarkSpikes,
        tileset = SpikesTileGenerator.SpikesTileset(
            topLeft = 712,
            bottomLeft = 713,
            top = 698,
            bottom = 704,
            topRightOuter = 701,
            topRightInner = 702,
            bottomRightInner = 709,
        )
    )

    override fun buildTile(context: TileGeneratorContext): Int? = context.run {
        val metaTiles = Retail3.MetaTiles
        val ladder = Retail3.MetaTiles.Ladder

        val tile = when {
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

            // Rock / rock light
            containsAll(Rock.Top, Retail3.MetaTiles.RockLight.left) -> 639
            containsAll(Rock.Top, Retail3.MetaTiles.RockLight.center) -> 640
            containsAll(Rock.Top, Retail3.MetaTiles.RockLight.right) -> 641

            else -> null
        }

        tile ?: lightSpikesTileGenerator.buildTile(context) ?: darkSpikesTileGenerator.buildTile(context)
    }
}

object Retail3 : Retail(naturalIndex = 3), RetailLadderPrototype {
    object MetaTiles {
        interface Spikes {
            val top: MetaTile
            val bottom: MetaTile
        }

        object Ladder {
            val top = MetaTile(668)

            val core = MetaTile(669)
        }

        object Rock {
            object Top : MetaTile(622)
            object LeftSideOuter : MetaTile(624)
            object LeftSideInner : MetaTile(625)
            object RightSide : MetaTile(631)
            object LowerLeftCorner : MetaTile(638)
            object LowerRightCornerOuter : MetaTile(642)
            object LowerRightCornerInner : MetaTile(643)
        }

        object RockLight {
            val left = MetaTile(639)
            val center = MetaTile(640)
            val right = MetaTile(641)
        }

        object LightSpikes : Spikes {
            override val top = MetaTile(686)
            override val bottom = MetaTile(692)
        }

        object DarkSpikes : Spikes {
            override val top = MetaTile(698)
            override val bottom = MetaTile(704)
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

    val lightCrumblingPegPrototype = CrumblingPegPrototype(
        imageSetId = ImageSetId(fullyQualifiedId = "LEVEL3_IMAGES_CRUMBLINPEG1"),
        shortImageSetId = "LEVEL_CRUMBLINPEG1",
    )

    val darkCrumblingPegPrototype = CrumblingPegPrototype(
        imageSetId = ImageSetId(fullyQualifiedId = "LEVEL3_IMAGES_CRUMBLINPEG2"),
        shortImageSetId = "LEVEL_CRUMBLINPEG2",
    )
}
