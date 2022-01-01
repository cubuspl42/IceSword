package icesword.editor

sealed interface SelectionContext

interface KnotMeshSelectionContext : SelectionContext {
    fun enterKnotSelectMode()

    fun enterKnotBrushMode()
}

interface PathElevatorSelectionContext : SelectionContext {
    fun enterEditPathElevatorMode()
}

interface EnemySelectionContext : SelectionContext {
    fun editPickups()
}

interface FloorSpikeRowSelectionContext : SelectionContext {
    fun editSpikes()
}

interface RopeSelectionContext : SelectionContext {
    fun editSpeed()
}

interface CrateStackSelectionContext : SelectionContext {
    fun editPickups()
}

interface WapObjectSelectionContext : SelectionContext {
    fun editProperties()
}

data class CrumblingPegSelectionContext(
    val crumblingPeg: CrumblingPeg,
) : SelectionContext

interface TogglePegSelectionContext : SelectionContext {
    fun editTiming()
}

interface WarpSelectionContext : SelectionContext {
    fun editTarget()
}
