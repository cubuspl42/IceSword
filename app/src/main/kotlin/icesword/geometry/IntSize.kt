package icesword.geometry

import kotlinx.serialization.Serializable

@Serializable
data class IntSize(
    val width: Int,
    val height: Int,
) {
    companion object {
        val ZERO = IntSize(0, 0)

        val UNIT = IntSize(1, 1)
    }

    fun toRect(position: IntVec2 = IntVec2.ZERO): IntRect =
        IntRect(position, this)

    operator fun times(s: Int): IntSize =
        IntSize(width * s, height * s)

    fun toVec2(): IntVec2 =
        IntVec2(width, height)

    operator fun div(s: Int): IntSize =
        IntSize(
            width = width / s,
            height = height / s,
        )
}
