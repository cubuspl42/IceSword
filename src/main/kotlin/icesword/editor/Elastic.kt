package icesword.editor

import icesword.frp.*
import icesword.geometry.IntVec2

sealed class ElasticPrototype {
    abstract val metaTiles: Map<IntVec2, MetaTile>
}

object LeavesPrototype : ElasticPrototype() {
    override val metaTiles: Map<IntVec2, MetaTile> =
        (0..16).flatMap(::logLevel).toMap()
}

private fun logLevel(i: Int): Set<Pair<IntVec2, MetaTile>> = setOf(
    IntVec2(-1, i) to MetaTile.LOG_LEFT,
    IntVec2(0, i) to MetaTile.LOG,
    IntVec2(1, i) to MetaTile.LOG_RIGHT,
)