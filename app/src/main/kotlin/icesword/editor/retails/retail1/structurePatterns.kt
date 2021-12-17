package icesword.editor.retails.retail1

import icesword.editor.KnotPrototype
import icesword.editor.MetaTile
import icesword.editor.WapObjectPropsData
import icesword.editor.elastic.ElasticStructurePattern
import icesword.editor.elastic.ElasticStructurePatternOrientation
import icesword.editor.elastic.RectangularMetaTilePattern
import icesword.editor.knot_mesh.MetaTilePattern1x1
import icesword.editor.knot_mesh.MetaTilePattern2x1
import icesword.editor.knot_mesh.StructureConcavePattern
import icesword.editor.knot_mesh.StructureConvexPattern
import icesword.editor.knot_mesh.KnotStructurePattern
import icesword.editor.retails.Retail1

private val foundation = object : KnotStructurePattern(
    convexPattern = StructureConvexPattern(
        topLeft = MetaTilePattern1x1(MetaTile(303)),
        top = MetaTilePattern1x1(Retail1.MetaTiles.Foundation.top),
        topRight = MetaTilePattern2x1(MetaTile(305), MetaTile(307)),
        left = MetaTilePattern1x1(MetaTile(302)),
        right = MetaTilePattern1x1(MetaTile(308)),
        bottomLeft = MetaTilePattern1x1(MetaTile(920)),
        bottom = MetaTilePattern1x1(MetaTile(12)),
        bottomRight = MetaTilePattern1x1(MetaTile(308)),
    ),
    concavePattern = StructureConcavePattern(
        topLeft = MetaTilePattern1x1(MetaTile(12)),
        topRight = MetaTilePattern1x1(MetaTile(12)),
        bottomLeft = MetaTilePattern1x1(MetaTile(309)),
        bottomRight = MetaTilePattern1x1(MetaTile(74)),
    ),
    fill = MetaTilePattern1x1(MetaTile(12)),
) {
    override fun test(knotPrototype: KnotPrototype): Boolean =
        knotPrototype is KnotPrototype.Level1Foundation
}

val retail1LadderPattern = ElasticStructurePattern(
    startingPattern = RectangularMetaTilePattern(
        metaTiles = listOf(MetaTile(310)),
        width = 1,
    ),
    repeatingPattern = RectangularMetaTilePattern(
        metaTiles = listOf(MetaTile(311)),
        width = 1,
    ),
    endingPattern = RectangularMetaTilePattern(
        metaTiles = listOf(MetaTile(312)),
        width = 1,
    ),
    orientation = ElasticStructurePatternOrientation.Vertical,
)

val retail1ColumnPattern = ElasticStructurePattern(
    startingPattern = RectangularMetaTilePattern(
        metaTiles = listOf(MetaTile(933)),
        width = 1,
    ),
    repeatingPattern = RectangularMetaTilePattern(
        metaTiles = listOf(MetaTile(934)),
        width = 1,
    ),
    endingPattern = RectangularMetaTilePattern(
        metaTiles = listOf(Retail1.MetaTiles.Column.bottom),
        width = 1,
    ),
    wapObject = WapObjectPropsData(
        x = 28,
        y = 32,
        i = 2,
        logic = "DoNothing",
        imageSet = "LEVEL_ARCHESFRONT",
    ),
    orientation = ElasticStructurePatternOrientation.Vertical,
)

val retail1KnotStructurePatterns: List<KnotStructurePattern> = listOf(
    foundation,
)
