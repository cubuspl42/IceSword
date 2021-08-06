package icesword.editor

import fetchWorld
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.scene.Tileset
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
}
