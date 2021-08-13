package icesword.editor

import fetchWorld
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.geometry.IntVec2
import icesword.scene.Tileset
import icesword.tileAtPoint
import icesword.tileTopLeftCorner
import loadTileset

enum class Tool {
    select,
    move,
}

class Editor(
    val world: World,
    val tileset: Tileset,
) {
    companion object {
        suspend fun load(): Editor {
            val tileset = loadTileset()

            val wwdWorld = fetchWorld()

            val world = World.load(wwdWorld)

            return Editor(
                world = world,
                tileset = tileset,
            )
        }
    }

    private val _selectedTool = MutCell(Tool.select)

    val selectedTool: Cell<Tool> = _selectedTool

    fun selectTool(tool: Tool) {
        _selectedTool.set(tool)
    }

    private val _selectedEntity: MutCell<Entity?> = MutCell(null)

    val selectedEntity: Cell<Entity?> = _selectedEntity

    fun selectEntityAt(worldPosition: IntVec2) {
        val metaTileClusters = world.metaTileClusters.volatileContentView

        val selectableMetaTileClusters = metaTileClusters.filter { it.isSelectableAt(worldPosition) }

        val selectedMetaTileCluster = selectedEntity.sample() as? MetaTileCluster?

        val metaTileClusterToSelect =
            selectableMetaTileClusters.indexOfOrNull(selectedMetaTileCluster)?.let { index ->
                val n = selectableMetaTileClusters.size
                selectableMetaTileClusters[(index + 1) % n]
            } ?: selectableMetaTileClusters.firstOrNull()

        metaTileClusterToSelect
            ?.let { entity -> selectEntity(entity) }
    }

    private fun selectEntity(entity: Entity) {
        _selectedEntity.sample()?.unselect()
        _selectedEntity.set(entity.also { it.select() })
    }

    fun insertLeaves() {
        val focusPoint = world.cameraFocusPoint.sample()
        val insertionPoint = focusPoint + IntVec2(512, 512)

        val metaTileCluster = MetaTileCluster(
            initialTileOffset = tileAtPoint(insertionPoint),
            localMetaTiles = LeavesPrototype.metaTiles,
        )

        world.planeTiles.insertMetaTileCluster(metaTileCluster)
    }
}

private fun <T> Iterable<T>.indexOfOrNull(element: T?): Int? {
    val i = this.indexOf(element)
    return when {
        i >= 0 -> i
        else -> null
    }
}

fun <T> List<T>.rotate(n: Int) =
    slice(n until size) + slice(0 until n)