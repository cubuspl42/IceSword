package icesword.editor

import icesword.editor.entities.CrumblingPeg

sealed interface SelectionContext

interface SingleKnotMeshSelectionContext : SelectionContext {
    fun enterKnotSelectMode()

    fun enterKnotBrushMode()
}

interface MultipleKnotMeshesSelectionContext : SelectionContext {
    fun mergeKnotMeshes()
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
