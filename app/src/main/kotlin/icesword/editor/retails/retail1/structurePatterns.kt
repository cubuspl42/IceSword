package icesword.editor.retails.retail1

import icesword.editor.KnotPrototype
import icesword.editor.MetaTile
import icesword.editor.WapObjectPropsData
import icesword.editor.elastic.ElasticLinearPattern
import icesword.editor.elastic.ElasticLinearPatternOrientation
import icesword.editor.elastic.ElasticRectangularPattern
import icesword.editor.elastic.LinearMetaTilePattern
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
        left = MetaTilePattern1x1(Retail1.MetaTiles.Foundation.left),
        right = MetaTilePattern1x1(Retail1.MetaTiles.Foundation.right),
        bottomLeft = MetaTilePattern1x1(MetaTile(920)),
        bottom = MetaTilePattern1x1(MetaTile(12)),
        bottomRight = MetaTilePattern1x1(Retail1.MetaTiles.Foundation.right),
    ),
    concavePattern = StructureConcavePattern(
        topLeft = MetaTilePattern1x1(MetaTile(12)),
        topRight = MetaTilePattern1x1(MetaTile(12)),
        bottomLeft = MetaTilePattern1x1(Retail1.MetaTiles.Foundation.concaveBottomLeft),
        bottomRight = MetaTilePattern1x1(Retail1.MetaTiles.Foundation.concaveBottomRight),
    ),
    fill = MetaTilePattern1x1(MetaTile(12)),
) {
    override fun test(knotPrototype: KnotPrototype): Boolean =
        knotPrototype is KnotPrototype.Level1Foundation
}

val retail1LadderPattern = ElasticLinearPattern(
    startingPattern = LinearMetaTilePattern(
        metaTiles = listOf(MetaTile(310)),
        width = 1,
    ),
    repeatingPattern = LinearMetaTilePattern(
        metaTiles = listOf(MetaTile(311)),
        width = 1,
    ),
    endingPattern = LinearMetaTilePattern(
        metaTiles = listOf(MetaTile(312)),
        width = 1,
    ),
    orientation = ElasticLinearPatternOrientation.Vertical,
)

val retail1KnotStructurePatterns: List<KnotStructurePattern> = listOf(
    foundation,
)
