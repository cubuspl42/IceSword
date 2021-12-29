package icesword.editor

sealed interface SelectionMode

interface KnotMeshSelectionMode : SelectionMode {
    fun enterKnotSelectMode()

    fun enterKnotBrushMode()
}

interface PathElevatorSelectionMode : SelectionMode {
    fun enterEditPathElevatorMode()
}

interface EnemySelectionMode : SelectionMode {
    fun editPickups()
}

interface FloorSpikeRowSelectionMode : SelectionMode {
    fun editSpikes()
}

interface RopeSelectionMode : SelectionMode {
    fun editSpeed()
}

interface CrateStackSelectionMode : SelectionMode {
    fun editPickups()
}

interface WapObjectSelectionMode : SelectionMode {
    fun editProperties()
}

data class CrumblingPegSelectionMode(
    val crumblingPeg: CrumblingPeg,
) : SelectionMode
