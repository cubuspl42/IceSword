package icesword.editor

open class MetaTile(
    val tileId: Int?,
) {
    object None : MetaTile(null)

    object Log : MetaTile(657)
    object LogLeft : MetaTile(null)
    object LogRight : MetaTile(null)

    object LeavesUpper : MetaTile(644)
    object LeavesLower : MetaTile(651)
    object LeavesUpperLeft : MetaTile(645)
    object LeavesLowerLeft : MetaTile(650)
    object LeavesUpperRight : MetaTile(649)
    object LeavesLowerRight : MetaTile(656)

    object GrassUpper : MetaTile(604)
}
