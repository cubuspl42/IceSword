package icesword.editor

import fetchWorld
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.geometry.IntVec2
import icesword.scene.Tileset
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

        metaTileClusters.minByOrNull {
            val position = tileTopLeftCorner(it.tileOffset.sample())
            val diff = position - worldPosition
            diff.lengthSquared
        }?.let { entity ->
            selectEntity(entity)
        }
    }

    private fun selectEntity(entity: Entity) {
        _selectedEntity.sample()?.unselect()
        _selectedEntity.set(entity.also { it.select() })
    }
}
