package icesword.geometry

data class IntSize(
    val width: Int,
    val height: Int,
) {
    companion object {
        val ZERO = IntSize(0, 0)
    }

    fun toRect(position: IntVec2 = IntVec2.ZERO): IntRect =
        IntRect(position, this)
}
