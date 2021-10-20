package icesword.geometry

import kotlinx.serialization.Serializable

@Serializable
data class IntSize(
    val width: Int,
    val height: Int,
) {
    companion object {
        val ZERO = IntSize(0, 0)
    }

    fun toRect(position: IntVec2 = IntVec2.ZERO): IntRect =
        IntRect(position, this)

    operator fun times(s: Int): IntSize =
        IntSize(width * s, height * s)
}
