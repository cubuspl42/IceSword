package icesword.editor.knot_mesh

import icesword.editor.MetaTile
import icesword.geometry.IntVec2

data class MetaTilePattern2x1(
    val left: MetaTile,
    val right: MetaTile,
) : CornerMetaTilePattern, HorizontalMetaTilePattern {
    override fun buildConvexTopLeftMatcher(): List<KnotPatternMatcher> = listOf(
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    !knotExists(IntVec2(-2, -1)) &&
                    !knotExists(IntVec2(-1, -1)) &&
                    !knotExists(IntVec2(0, -1)) &&
                    !knotExists(IntVec2(-2, 0)) &&
                    testKnot(IntVec2(-1, 0)) &&
                    testKnot(IntVec2(0, 0))
                ) right else null
            }
        },
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    !knotExists(IntVec2(-1, -1)) &&
                    !knotExists(IntVec2(0, -1)) &&
                    !knotExists(IntVec2(-1, 0)) &&
                    testKnot(IntVec2(0, 0))
                ) left else null
            }
        },
    )

    override fun buildConvexTopRightMatcher(): List<KnotPatternMatcher> = listOf(
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    !knotExists(IntVec2(-1, -1)) &&
                    !knotExists(IntVec2(0, -1)) &&
                    !knotExists(IntVec2(1, -1)) &&
                    testKnot(IntVec2(-1, 0)) &&
                    testKnot(IntVec2(0, 0)) &&
                    !knotExists(IntVec2(1, 0))
                ) left else null
            }
        },
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    !knotExists(IntVec2(-1, -1)) &&
                    !knotExists(IntVec2(0, -1)) &&
                    testKnot(IntVec2(-1, 0)) &&
                    !knotExists(IntVec2(0, 0))
                ) right else null
            }
        },
    )

    override fun buildConvexLeftMatcher(): List<KnotPatternMatcher> = listOf(
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    !knotExists(IntVec2(-2, -1)) &&
                    testKnot(IntVec2(-1, -1)) &&
                    testKnot(IntVec2(0, -1)) &&
                    !knotExists(IntVec2(-2, 0)) &&
                    testKnot(IntVec2(-1, 0)) &&
                    testKnot(IntVec2(0, 0))
                ) right else null
            }
        },
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    !knotExists(IntVec2(-1, -1)) &&
                    testKnot(IntVec2(0, -1)) &&
                    !knotExists(IntVec2(-1, 0)) &&
                    testKnot(IntVec2(0, 0))
                ) left else null
            }
        },
    )

    override fun buildConvexRightMatcher(): List<KnotPatternMatcher> = listOf(
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    testKnot(IntVec2(-1, -1)) &&
                    testKnot(IntVec2(0, -1)) &&
                    !knotExists(IntVec2(1, -1)) &&
                    testKnot(IntVec2(-1, 0)) &&
                    testKnot(IntVec2(0, 0)) &&
                    !knotExists(IntVec2(1, 0))
                ) left else null
            }
        },
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    testKnot(IntVec2(-1, -1)) &&
                    !knotExists(IntVec2(0, -1)) &&
                    testKnot(IntVec2(-1, 0)) &&
                    !knotExists(IntVec2(0, 0))
                ) right else null
            }
        },
    )

    override fun buildConvexBottomLeftMatcher(): List<KnotPatternMatcher> = listOf(
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    !knotExists(IntVec2(-2, -1)) &&
                    testKnot(IntVec2(-1, -1)) &&
                    testKnot(IntVec2(0, -1)) &&
                    !knotExists(IntVec2(-2, 0)) &&
                    !knotExists(IntVec2(-1, 0)) &&
                    !knotExists(IntVec2(0, 0))
                ) right else null
            }
        },
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    !knotExists(IntVec2(-1, -1)) &&
                    testKnot(IntVec2(0, -1)) &&
                    !knotExists(IntVec2(-1, 0)) &&
                    !knotExists(IntVec2(0, 0))
                ) left else null
            }
        },
    )

    override fun buildConvexBottomRightMatcher(): List<KnotPatternMatcher> = listOf(
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    testKnot(IntVec2(-1, -1)) &&
                    testKnot(IntVec2(0, -1)) &&
                    !knotExists(IntVec2(1, -1)) &&
                    !knotExists(IntVec2(-1, 0)) &&
                    !knotExists(IntVec2(0, 0)) &&
                    !knotExists(IntVec2(1, 0))
                ) left else null
            }
        },
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    testKnot(IntVec2(-1, -1)) &&
                    !knotExists(IntVec2(0, -1)) &&
                    !knotExists(IntVec2(-1, 0)) &&
                    !knotExists(IntVec2(0, 0))
                ) right else null
            }
        },
    )

    override fun buildConcaveTopLeftMatcher(): List<KnotPatternMatcher> = listOf(
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    testKnot(IntVec2(-2, -1)) &&
                    testKnot(IntVec2(-1, -1)) &&
                    testKnot(IntVec2(0, -1)) &&
                    testKnot(IntVec2(-2, 0)) &&
                    !knotExists(IntVec2(-1, 0)) &&
                    !knotExists(IntVec2(0, 0))
                ) right else null
            }
        },
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    testKnot(IntVec2(-1, -1)) &&
                    testKnot(IntVec2(0, -1)) &&
                    testKnot(IntVec2(-1, 0)) &&
                    !knotExists(IntVec2(0, 0))
                ) left else null
            }
        },
    )

    override fun buildConcaveTopRightMatcher(): List<KnotPatternMatcher> = listOf(
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    testKnot(IntVec2(-2, -1)) &&
                    testKnot(IntVec2(-1, -1)) &&
                    testKnot(IntVec2(0, -1)) &&
                    !knotExists(IntVec2(-2, 0)) &&
                    testKnot(IntVec2(-1, 0)) &&
                    testKnot(IntVec2(0, 0))
                ) right else null
            }
        },
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    testKnot(IntVec2(-1, -1)) &&
                    testKnot(IntVec2(0, -1)) &&
                    !knotExists(IntVec2(-1, 0)) &&
                    testKnot(IntVec2(0, 0))
                ) left else null
            }
        },
    )

    override fun buildConcaveBottomLeftMatcher(): List<KnotPatternMatcher> = listOf(
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    testKnot(IntVec2(-2, -1)) &&
                    !knotExists(IntVec2(-1, -1)) &&
                    !knotExists(IntVec2(0, -1)) &&
                    testKnot(IntVec2(-2, 0)) &&
                    testKnot(IntVec2(-1, 0)) &&
                    testKnot(IntVec2(0, 0))
                ) right else null
            }
        },
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    testKnot(IntVec2(-1, -1)) &&
                    !knotExists(IntVec2(0, -1)) &&
                    testKnot(IntVec2(-1, 0)) &&
                    testKnot(IntVec2(0, 0))
                ) left else null
            }
        },
    )

    override fun buildConcaveBottomRightMatcher(): List<KnotPatternMatcher> = listOf(
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    !knotExists(IntVec2(-2, -1)) &&
                    testKnot(IntVec2(-1, -1)) &&
                    testKnot(IntVec2(0, -1)) &&
                    testKnot(IntVec2(-2, 0)) &&
                    testKnot(IntVec2(-1, 0)) &&
                    testKnot(IntVec2(0, 0))
                ) right else null
            }
        },
        object : KnotPatternMatcher {
            override fun buildMetaTile(context: KnotPatternMatcherContext) = context.run {
                if (
                    !knotExists(IntVec2(-1, -1)) &&
                    testKnot(IntVec2(0, -1)) &&
                    testKnot(IntVec2(-1, 0)) &&
                    testKnot(IntVec2(0, 0))
                ) left else null
            }
        },
    )
}
