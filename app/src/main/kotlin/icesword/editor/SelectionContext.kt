package icesword.editor

import icesword.editor.entities.CrumblingPeg
import icesword.editor.entities.Entity

interface SelectionContext

interface SingleEntitySelectionContext : SelectionContext {
    val entity: Entity
}

data class SimpleSingleEntitySelectionContext(
    override val entity: Entity,
) : SingleEntitySelectionContext

interface SingleKnotMeshSelectionContext : SingleEntitySelectionContext {
    fun enterKnotSelectMode()

    fun enterKnotBrushMode()
}

interface MultipleKnotMeshesSelectionContext : SelectionContext {
    fun mergeKnotMeshes()
}

interface PathElevatorSelectionContext : SingleEntitySelectionContext {
    fun enterEditPathElevatorMode()
}

interface EnemySelectionContext : SingleEntitySelectionContext {
    fun editPickups()
}

interface FloorSpikeRowSelectionContext : SingleEntitySelectionContext {
    fun editSpikes()
}

interface RopeSelectionContext : SingleEntitySelectionContext {
    fun editSpeed()
}

interface CrateStackSelectionContext : SingleEntitySelectionContext {
    fun editPickups()
}

interface WapObjectSelectionContext : SingleEntitySelectionContext {
    fun editProperties()
}

interface CrumblingPegSelectionContext : SingleEntitySelectionContext {
    override val entity: CrumblingPeg
}

interface TogglePegSelectionContext : SingleEntitySelectionContext {
    fun editTiming()
}

interface WarpSelectionContext : SingleEntitySelectionContext {
    fun editTarget()

    fun pickTarget()
}
