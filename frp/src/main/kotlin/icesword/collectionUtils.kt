package icesword

import icesword.collections.SetFactory

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

fun <T> Sequence<T>.toMutableSetThrough(through: SetFactory<T>): MutableSet<T> {
    val set = through.create()
    for (item in this) set.add(item)
    return set
}

/**
 * Moves the given item at the `oldIndex` to the `newIndex`
 */
fun <T> MutableList<T>.moveAt(oldIndex: Int, newIndex: Int) {
    if (oldIndex !in indices) throw IllegalArgumentException()
    if (newIndex !in indices) throw IllegalArgumentException()

    if (oldIndex != newIndex) {
        val item = this[oldIndex]
        removeAt(oldIndex)
        if (oldIndex > newIndex)
            add(newIndex, item)
        else
            add(newIndex - 1, item)
    }
}

fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    if (index1 !in indices) throw IllegalArgumentException()
    if (index2 !in indices) throw IllegalArgumentException()

    val tmp = this[index1]
    this[index1] = this[index2]
    this[index2] = tmp
}

//fun <T> List<T>.withMovedAt(
//    oldIndex: Int,
//    newIndex: Int,
//): List<T> =
//    toMutableList().apply { moveAt(oldIndex, newIndex) }

fun <T> List<T>.withSwapped(
    index1: Int,
    index2: Int,
): List<T> =
    toMutableList().apply { swap(index1, index2) }

//fun <T> Set<T>.minusFast(other: Set<T>): Set<T> = when {
//    other.isEmpty() -> this.toSet()
//    else -> this.filterNotTo(fastSetOf()) { it in other }
//}
