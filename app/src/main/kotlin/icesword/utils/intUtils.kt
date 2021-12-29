package icesword.utils

import kotlinx.css.a
import kotlin.math.roundToInt

/// "Round-down" integer division, a rough equivalent of mathematical function
// f(a, b) = floor(a / b). Assumes b > 0.
fun Int.divFloor(b: Int): Int {
    if (b <= 0) throw IllegalArgumentException("divFloor is defined for b > 0")

    // https://stackoverflow.com/a/4168996/
    return if (this >= 0) this / b
    else (this - b + 1) / b
}


/// "Round-up" integer division, a rough equivalent of mathematical function
// f(a, b) = ceil(a / b). Assumes b > 0.
fun Int.divCeil(b: Int): Int {
    if (b <= 0) throw IllegalArgumentException("divCeil is defined for b > 0")

    // https://stackoverflow.com/a/30824434/
    val r = if (this % b > 0) 1 else 0
    return this / b + r
}

fun Int.roundToMultipleOf(n: Int): Int =
    (toDouble() / n).roundToInt() * n
