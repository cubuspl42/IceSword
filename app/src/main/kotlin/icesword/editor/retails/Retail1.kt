package icesword.editor.retails

import icesword.ImageSetId
import icesword.editor.ElevatorPrototype
import icesword.editor.MetaTile
import icesword.editor.TileGenerator
import icesword.editor.TileGeneratorContext
import icesword.editor.elastic.ElasticStructurePattern
import icesword.editor.elastic.ElasticStructurePatternOrientation
import icesword.editor.elastic.RectangularMetaTilePattern
import icesword.editor.encode
import icesword.editor.knot_mesh.KnotStructurePattern
import icesword.editor.retails.retail1.retail1KnotStructurePatterns
import icesword.editor.retails.retail1.retail1LadderPattern
import icesword.wwd.Wwd

val retail1SpikesPattern = ElasticStructurePattern(
    startingPattern = RectangularMetaTilePattern(
        metaTiles = emptyList(),
        width = 0,
    ),
    repeatingPattern = RectangularMetaTilePattern(
        metaTiles = listOf(
            Retail1.MetaTiles.Spikes.Top, Retail1.MetaTiles.Spikes.Bottom,
        ),
        width = 2,
    ),
    endingPattern = RectangularMetaTilePattern(
        metaTiles = emptyList(),
        width = 0,
    ),
    orientation = ElasticStructurePatternOrientation.Horizontal,
)

val retail1PlatformPattern = ElasticStructurePattern(
    startingPattern = RectangularMetaTilePattern(
        metaTiles = listOf(
            MetaTile(331),
        ),
        width = 1,
    ),
    repeatingPattern = RectangularMetaTilePattern(
        metaTiles = listOf(
            MetaTile(332),
        ),
        width = 1,
    ),
    endingPattern = RectangularMetaTilePattern(
        metaTiles = listOf(
            MetaTile(334),
        ),
        width = 1,
    ),
    orientation = ElasticStructurePatternOrientation.Horizontal,
)

private val retailTileGenerator = object : TileGenerator {
    override fun buildTile(context: TileGeneratorContext): Int? = context.run {
        when {
            // Foundation top / column bottom
            containsAll(Retail1.MetaTiles.Foundation.top, Retail1.MetaTiles.Column.bottom) -> 935

            // Spikes / foundation
            containsAll(Retail1.MetaTiles.Foundation.right, Retail1.MetaTiles.Spikes.Top) -> 319
            containsAll(Retail1.MetaTiles.Foundation.concaveBottomLeft, Retail1.MetaTiles.Spikes.Bottom) -> 322
            containsAll(Retail1.MetaTiles.Foundation.top, Retail1.MetaTiles.Spikes.Bottom) -> 327
            containsAll(Retail1.MetaTiles.Foundation.left, Retail1.MetaTiles.Spikes.Top) -> 321
            containsAll(Retail1.MetaTiles.Foundation.concaveBottomRight, Retail1.MetaTiles.Spikes.Bottom) -> 925

            else -> null
        }
    }
}

object Retail1 : Retail(naturalIndex = 1), RetailLadderPrototype {
    object MetaTiles {
        object Foundation {
            val left = MetaTile(302)

            val top = MetaTile(304)

            val right = MetaTile(308)

            val concaveBottomLeft = MetaTile(309)

            val concaveBottomRight = MetaTile(74)
        }

        object Column {
            val bottom = MetaTile(935)
        }

        object Spikes {
            object Top : MetaTile(326)
            object Bottom : MetaTile(327)
        }
    }

    override val ladderGenerator = retail1LadderPattern.toElasticGenerator()

    override val elevatorPrototype = ElevatorPrototype(
        elevatorImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL1_IMAGES_ELEVATORS",
        ),
        wwdObjectPrototype = Wwd.Object_.empty().copy(
            logic = encode("Elevator"),
            imageSet = encode("LEVEL_ELEVATORS"),
        ),
    )

    override val knotStructurePatterns: List<KnotStructurePattern> =
        retail1KnotStructurePatterns

    override val tileGenerator: TileGenerator = retailTileGenerator
}
