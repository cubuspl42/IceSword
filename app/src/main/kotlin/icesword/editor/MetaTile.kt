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

    object GrassUpper : MetaTile(604)

    object LadderTop : MetaTile(668)
    object Ladder : MetaTile(669)
    object LadderBottom : MetaTile(670)

    object SpikeTop : MetaTile(698)
    object SpikeBottom : MetaTile(704)

    object RockTop : MetaTile(622)
    object RockLeftSideOuter : MetaTile(624)
    object RockLeftSideInner : MetaTile(625)
    object RockRightSide : MetaTile(631)
    object RockLowerLeftCorner : MetaTile(638)
    object RockLowerRightCornerOuter : MetaTile(642)
    object RockLowerRightCornerInner : MetaTile(643)
}
