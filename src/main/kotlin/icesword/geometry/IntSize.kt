package icesword.geometry

data class IntSize(
    val width: Int,
    val height: Int,
) {
    fun toRect(position: IntVec2 = IntVec2.ZERO): IntRect =
        IntRect(position, this)
}
