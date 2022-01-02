package icesword.editor.entities.knot_mesh

import icesword.editor.MetaTile
import icesword.geometry.IntVec2

data class MetaTilePattern1x1(
    val tile: MetaTile,
) : CornerMetaTilePattern, VerticalMetaTilePattern, HorizontalMetaTilePattern, FillMetaTilePattern {
    override fun buildConvexTopLeftMatcher(): List<KnotPatternMatcher> = listOf(object : KnotPatternMatcher {
        override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
            if (
                !knotExists(IntVec2(-1, -1)) &&
                !knotExists(IntVec2(0, -1)) &&
                !knotExists(IntVec2(-1, 0)) &&
                testKnot(IntVec2(0, 0))
            ) tile else null
        }
    })

    override fun buildConvexTopMatcher(): List<KnotPatternMatcher> = listOf(object : KnotPatternMatcher {
        override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
            if (
                !knotExists(IntVec2(-1, -1)) &&
                !knotExists(IntVec2(0, -1)) &&
                testKnot(IntVec2(-1, 0)) &&
                testKnot(IntVec2(0, 0))
            ) tile else null
        }
    })

    override fun buildConvexTopRightMatcher(): List<KnotPatternMatcher> = listOf(object : KnotPatternMatcher {
        override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
            if (
                !knotExists(IntVec2(-1, -1)) &&
                !knotExists(IntVec2(0, -1)) &&
                testKnot(IntVec2(-1, 0)) &&
                !knotExists(IntVec2(0, 0))
            ) tile else null
        }
    })

    override fun buildConvexLeftMatcher(): List<KnotPatternMatcher> = listOf(object : KnotPatternMatcher {
        override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
            if (
                !knotExists(IntVec2(-1, -1)) &&
                testKnot(IntVec2(0, -1)) &&
                !knotExists(IntVec2(-1, 0)) &&
                testKnot(IntVec2(0, 0))
            ) tile else null
        }
    })

    override fun buildMatcher() = object : KnotPatternMatcher {
        override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
            if (
                testKnot(IntVec2(-1, -1)) &&
                testKnot(IntVec2(0, -1)) &&
                testKnot(IntVec2(-1, 0)) &&
                testKnot(IntVec2(0, 0))
            ) tile else null
        }
    }

    override fun buildConvexRightMatcher(): List<KnotPatternMatcher> = listOf(object : KnotPatternMatcher {
        override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
            if (
                testKnot(IntVec2(-1, -1)) &&
                !knotExists(IntVec2(0, -1)) &&
                testKnot(IntVec2(-1, 0)) &&
                !knotExists(IntVec2(0, 0))
            ) tile else null
        }
    })

    override fun buildConvexBottomLeftMatcher(): List<KnotPatternMatcher> = listOf(object : KnotPatternMatcher {
        override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
            if (
                !knotExists(IntVec2(-1, -1)) &&
                testKnot(IntVec2(0, -1)) &&
                !knotExists(IntVec2(-1, 0)) &&
                !knotExists(IntVec2(0, 0))
            ) tile else null
        }
    })

    override fun buildConvexBottomMatcher(): List<KnotPatternMatcher> = listOf(object : KnotPatternMatcher {
        override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
            if (
                testKnot(IntVec2(-1, -1)) &&
                testKnot(IntVec2(0, -1)) &&
                !knotExists(IntVec2(-1, 0)) &&
                !knotExists(IntVec2(0, 0))
            ) tile else null
        }
    })

    override fun buildConvexBottomRightMatcher(): List<KnotPatternMatcher> = listOf(object : KnotPatternMatcher {
        override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
            if (
                testKnot(IntVec2(-1, -1)) &&
                !knotExists(IntVec2(0, -1)) &&
                !knotExists(IntVec2(-1, 0)) &&
                !knotExists(IntVec2(0, 0))
            ) tile else null
        }
    })

    override fun buildConcaveTopLeftMatcher(): List<KnotPatternMatcher> = listOf(object : KnotPatternMatcher {
        override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
            if (
                testKnot(IntVec2(-1, -1)) &&
                testKnot(IntVec2(0, -1)) &&
                testKnot(IntVec2(-1, 0)) &&
                !knotExists(IntVec2(0, 0))
            ) tile else null
        }
    })


    override fun buildConcaveTopRightMatcher(): List<KnotPatternMatcher> = listOf(object : KnotPatternMatcher {
        override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
            if (
                testKnot(IntVec2(-1, -1)) &&
                testKnot(IntVec2(0, -1)) &&
                !knotExists(IntVec2(-1, 0)) &&
                testKnot(IntVec2(0, 0))
            ) tile else null
        }
    })

    override fun buildConcaveBottomLeftMatcher(): List<KnotPatternMatcher> = listOf(object : KnotPatternMatcher {
        override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
            if (
                testKnot(IntVec2(-1, -1)) &&
                !knotExists(IntVec2(0, -1)) &&
                testKnot(IntVec2(-1, 0)) &&
                testKnot(IntVec2(0, 0))
            ) tile else null
        }
    })

    override fun buildConcaveBottomRightMatcher(): List<KnotPatternMatcher> = listOf(object : KnotPatternMatcher {
        override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
            if (
                !knotExists(IntVec2(-1, -1)) &&
                testKnot(IntVec2(0, -1)) &&
                testKnot(IntVec2(-1, 0)) &&
                testKnot(IntVec2(0, 0))
            ) tile else null
        }
    })
}
