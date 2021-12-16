package icesword.editor

sealed interface SelectionMode

interface KnotMeshSelectionMode : SelectionMode {
    fun enterKnotSelectMode()

    fun enterKnotBrushMode()
}

interface PathElevatorSelectionMode : SelectionMode {
    fun enterEditPathElevatorMode()
}
