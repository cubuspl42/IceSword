package icesword

import frpjs.FastSet

enum class SegregationTag {
    A,
    B,
}

enum class SegregationTag3 {
    A,
    B,
    C,
}


data class Segregated<T>(
    val groupA: List<T>,
    val groupB: List<T>,
)

data class Segregated3<T>(
    val groupA: List<T>,
    val groupB: List<T>,
    val groupC: List<T>,
)

fun <T> Iterable<T>.segregate(assign: (element: T) -> SegregationTag): Segregated<T> {
    val groupA = mutableListOf<T>()
    val groupB = mutableListOf<T>()

    this.forEach {
        when (assign(it)) {
            SegregationTag.A -> groupA.add(it)
            SegregationTag.B -> groupB.add(it)
        }
    }

    return Segregated(
        groupA = groupA,
        groupB = groupB,
    )
}

fun <T> Iterable<T>.segregate3(assign: (element: T) -> SegregationTag3): Segregated3<T> {
    val groupA = mutableListOf<T>()
    val groupB = mutableListOf<T>()
    val groupC = mutableListOf<T>()

    this.forEach {
        when (assign(it)) {
            SegregationTag3.A -> groupA.add(it)
            SegregationTag3.B -> groupB.add(it)
            SegregationTag3.C -> groupB.add(it)
        }
    }

    return Segregated3(
        groupA = groupA,
        groupB = groupB,
        groupC = groupC,
    )
}

fun <T> Sequence<T>.toMutableSetFast(): MutableSet<T> {
    val set = FastSet<T>()
    for (item in this) set.add(item)
    return set
}


fun <T> Set<T>.minusFast(other: Set<T>): Set<T> = when {
    other.isEmpty() -> this.toSet()
    else -> this.filterNotTo(FastSet()) { it in other }
}
