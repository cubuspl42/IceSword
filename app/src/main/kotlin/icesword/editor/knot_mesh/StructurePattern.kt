package icesword.editor.knot_mesh

import icesword.editor.KnotMetaTileBuilder
import icesword.editor.KnotPrototype
import icesword.editor.MetaTile
import icesword.geometry.IntVec2


interface KnotPatternMatcherContext {
    fun testKnot(relativeCoord: IntVec2): Boolean

    fun knotExists(relativeCoord: IntVec2): Boolean
}

interface KnotPatternMatcher {
    fun buildTile(context: KnotPatternMatcherContext): MetaTile?
}

interface CornerMetaTilePattern {
    fun buildConvexTopLeftMatcher(): List<KnotPatternMatcher>

    fun buildConvexTopRightMatcher(): List<KnotPatternMatcher>

    fun buildConvexBottomLeftMatcher(): List<KnotPatternMatcher>

    fun buildConvexBottomRightMatcher(): List<KnotPatternMatcher>

    fun buildConcaveTopLeftMatcher(): List<KnotPatternMatcher>

    fun buildConcaveTopRightMatcher(): List<KnotPatternMatcher>

    fun buildConcaveBottomLeftMatcher(): List<KnotPatternMatcher>

    fun buildConcaveBottomRightMatcher(): List<KnotPatternMatcher>
}

interface FillMetaTilePattern {
    fun buildConvexFillMatcher(): KnotPatternMatcher
}

interface VerticalMetaTilePattern {
    fun buildConvexTopMatcher(): List<KnotPatternMatcher>

    fun buildConvexBottomMatcher(): List<KnotPatternMatcher>
}

interface HorizontalMetaTilePattern {
    fun buildConvexLeftMatcher(): List<KnotPatternMatcher>

    fun buildConvexRightMatcher(): List<KnotPatternMatcher>
}

data class StructureConvexPattern(
    val topLeft: CornerMetaTilePattern? = null,
    val top: VerticalMetaTilePattern? = null,
    val topRight: CornerMetaTilePattern? = null,
    val left: HorizontalMetaTilePattern? = null,
    val fill: FillMetaTilePattern? = null,
    val right: HorizontalMetaTilePattern? = null,
    val bottomLeft: CornerMetaTilePattern? = null,
    val bottom: VerticalMetaTilePattern? = null,
    val bottomRight: CornerMetaTilePattern? = null,
) {
    fun buildMatchers(): List<KnotPatternMatcher> {
        val empty = emptyList<KnotPatternMatcher>()
        return (topLeft?.buildConvexTopLeftMatcher() ?: empty) +
                (topRight?.buildConvexTopRightMatcher() ?: empty) +
                (bottomLeft?.buildConvexBottomLeftMatcher() ?: empty) +
                (bottomRight?.buildConvexBottomRightMatcher() ?: empty) +
                (top?.buildConvexTopMatcher() ?: empty) +
                (left?.buildConvexLeftMatcher() ?: empty) +
                (right?.buildConvexRightMatcher() ?: empty) +
                (bottom?.buildConvexBottomMatcher() ?: empty) +
                (fill?.buildConvexFillMatcher()?.let(::listOf) ?: empty)
    }
}

data class StructureConcavePattern(
    val topLeft: CornerMetaTilePattern? = null,
    val topRight: CornerMetaTilePattern? = null,
    val bottomLeft: CornerMetaTilePattern? = null,
    val bottomRight: CornerMetaTilePattern? = null,
) {
    fun buildMatchers(): List<KnotPatternMatcher> {
        val empty = emptyList<KnotPatternMatcher>()
        return (topLeft?.buildConcaveTopLeftMatcher() ?: empty) +
                (topRight?.buildConcaveTopRightMatcher() ?: empty) +
                (bottomLeft?.buildConcaveBottomLeftMatcher() ?: empty) +
                (bottomRight?.buildConcaveBottomRightMatcher() ?: empty)
    }
}

// A meta-tile pattern describing a "structure", i.e. a meta-tile shape
// typically representing some kind of foundation, like rocks or bricks.
abstract class StructurePattern(
    val convexPattern: StructureConvexPattern,
    val concavePattern: StructureConcavePattern? = null,
    val fill: MetaTile? = null,
) {
    abstract fun test(knotPrototype: KnotPrototype): Boolean

    fun toKnotMetaTileBuilder(): KnotMetaTileBuilder = object : KnotMetaTileBuilder {
        private val matchers = convexPattern.buildMatchers() +
                (concavePattern?.buildMatchers() ?: emptyList())

        override fun buildMetaTile(
            tileCoord: IntVec2,
            globalKnots: Map<IntVec2, KnotPrototype>,
        ): MetaTile? {
            val context = object : KnotPatternMatcherContext {
                override fun testKnot(relativeCoord: IntVec2): Boolean =
                    globalKnots[tileCoord + relativeCoord]?.let { test(it) } ?: false

                override fun knotExists(relativeCoord: IntVec2): Boolean =
                    globalKnots.containsKey(tileCoord + relativeCoord)
            }

            return matchers.firstNotNullOfOrNull { it.buildTile(context) }
        }
    }
}
