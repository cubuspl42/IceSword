package icesword.editor

open class MetaTile(
    val tileId: Int?,
) {
    object None : MetaTile(null)

    object Log : MetaTile(657)
    object LogLeft : MetaTile(null)
    object LogRight : MetaTile(null)

    // What's the difference between 644 and 646?...
    object LeavesUpper : MetaTile(644)
    object LeavesLower : MetaTile(651)
    object LeavesUpperLeft : MetaTile(645)
    object LeavesLowerLeft : MetaTile(650)
    object LeavesUpperRight : MetaTile(649)
    object LeavesLowerRight : MetaTile(656)

    object SpikeTop : MetaTile(698)
    object SpikeBottom : MetaTile(704)
}
