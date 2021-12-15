package icesword.editor.retails.retail3

import icesword.editor.KnotPrototype
import icesword.editor.MetaTile
import icesword.editor.elastic.ElasticStructurePattern
import icesword.editor.elastic.ElasticStructurePatternOrientation
import icesword.editor.elastic.RectangularMetaTilePattern
import icesword.editor.knot_mesh.KnotStructurePattern
import icesword.editor.knot_mesh.MetaTilePattern1x1
import icesword.editor.knot_mesh.MetaTilePattern2x1
import icesword.editor.knot_mesh.StructureConcavePattern
import icesword.editor.knot_mesh.StructureConvexPattern
import icesword.editor.retails.Retail3

private val grass = object : KnotStructurePattern(
    convexPattern = StructureConvexPattern(
        topLeft = MetaTilePattern2x1(MetaTile(603), MetaTile(604)),
        top = MetaTilePattern1x1(Retail3.MetaTiles.grass),
        topRight = MetaTilePattern2x1(MetaTile(605), MetaTile(607)),
    )
) {
    override fun test(knotPrototype: KnotPrototype): Boolean =
        knotPrototype is KnotPrototype.Level3OvergroundRockPrototype
}

private val rock = object : KnotStructurePattern(
    convexPattern = StructureConvexPattern(
        topLeft = MetaTilePattern2x1(MetaTile(620), MetaTile(621)),
        top = MetaTilePattern1x1(MetaTile(622)),
        topRight = MetaTilePattern1x1(MetaTile(623)),
        left = MetaTilePattern2x1(MetaTile(628), MetaTile(629)),
        right = MetaTilePattern1x1(MetaTile(631)),
        bottomLeft = MetaTilePattern2x1(MetaTile(632), MetaTile(633)),
        bottom = MetaTilePattern1x1(MetaTile(634)),
        bottomRight = MetaTilePattern1x1(MetaTile(635)),
    ),
    concavePattern = StructureConcavePattern(
        topLeft = MetaTilePattern1x1(MetaTile(617)),
        topRight = MetaTilePattern2x1(MetaTile(618), MetaTile(619)),
        bottomLeft = MetaTilePattern1x1(MetaTile(638)),
        bottomRight = MetaTilePattern2x1(MetaTile(642), MetaTile(643)),
    ),
    fill = MetaTilePattern1x1(MetaTile(630)),
) {
    override fun test(knotPrototype: KnotPrototype): Boolean =
        knotPrototype is KnotPrototype.Level3RockPrototype
}

val retail3LadderPattern = run {
    val ladder = Retail3.MetaTiles.Ladder
    ElasticStructurePattern(
        startingPattern = RectangularMetaTilePattern(
            metaTiles = listOf(ladder.top),
            width = 1,
        ),
        repeatingPattern = RectangularMetaTilePattern(
            metaTiles = listOf(ladder.core),
            width = 1,
        ),
        endingPattern = RectangularMetaTilePattern(
            metaTiles = listOf(MetaTile(670)),
            width = 1,
        ),
        orientation = ElasticStructurePatternOrientation.Vertical,
    )
}

val retail3KnotStructurePatterns = listOf(
    grass,
    rock,
)
