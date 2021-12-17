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
import icesword.editor.retails.Retail3.MetaTiles.Rock

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

// What's the difference between 612, 624 and 628 (left side)?
// 609 is similar but visually different, when should it be used?

// 625 and 629 are similar but visually different. It seems to be a stylistic
// choice.

private val rock = object : KnotStructurePattern(
    convexPattern = StructureConvexPattern(
        topLeft = MetaTilePattern2x1(MetaTile(620), MetaTile(621)),
        top = MetaTilePattern1x1(Rock.Top),
        topRight = MetaTilePattern1x1(MetaTile(623)),
        left = MetaTilePattern2x1(Rock.LeftSideOuter, Rock.LeftSideInner),
        right = MetaTilePattern1x1(Rock.RightSide),
        bottomLeft = MetaTilePattern2x1(MetaTile(632), MetaTile(633)),
        bottom = MetaTilePattern1x1(MetaTile(634)),
        bottomRight = MetaTilePattern1x1(MetaTile(635)),
    ),
    concavePattern = StructureConcavePattern(
        topLeft = MetaTilePattern1x1(MetaTile(617)),
        topRight = MetaTilePattern2x1(MetaTile(618), MetaTile(619)),
        bottomLeft = MetaTilePattern1x1(Rock.LowerLeftCorner),
        bottomRight = MetaTilePattern2x1(Rock.LowerRightCornerOuter, Rock.LowerRightCornerInner),
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
