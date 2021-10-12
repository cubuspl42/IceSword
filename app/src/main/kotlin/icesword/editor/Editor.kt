package icesword.editor

import fetchWorld
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.sample
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.scene.Tileset
import icesword.tileAtPoint
import loadTileset

enum class Tool {
    SELECT,
    MOVE,
    KNOT_BRUSH
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

    private val _selectedTool = MutCell(Tool.SELECT)

    val selectedTool: Cell<Tool> = _selectedTool

    fun selectTool(tool: Tool) {
        _selectedTool.set(tool)
    }

    private val _selectedEntity: MutCell<Entity?> = MutCell(null)

    val selectedEntity: Cell<Entity?> = _selectedEntity

    fun selectEntityAt(worldPosition: IntVec2) {
        val entities = world.entities.sample()

        val selectableEntities = entities.filter { it.isSelectableAt(worldPosition) }

        val selectedEntity = selectedEntity.sample()

        val entityToSelect =
            selectableEntities.indexOfOrNull(selectedEntity)?.let { index ->
                val n = selectableEntities.size
                selectableEntities[(index + 1) % n]
            } ?: selectableEntities.firstOrNull()

        entityToSelect?.let { entity -> selectEntity(entity) }
    }

    private fun selectEntity(entity: Entity) {
        _selectedEntity.sample()?.unselect()
        _selectedEntity.set(entity.also { it.select() })

        println("Entity selected: $entity")
    }

    fun insertElastic(prototype: ElasticPrototype) {
        val focusPoint = world.cameraFocusPoint.sample()
        val insertionPoint = focusPoint + IntVec2(512, 512)

        val elastic = Elastic(
            prototype = prototype,
            initialBounds = IntRect(
                position = tileAtPoint(insertionPoint),
                size = prototype.defaultSize,
            ),
        )

        world.metaTileLayer.insertElastic(elastic)

        selectEntity(elastic)
    }

    fun insertKnotMesh(knotPrototype: KnotPrototype) {
        val focusPoint = world.cameraFocusPoint.sample()
        val insertionPoint = focusPoint + IntVec2(512, 512)

        val knotMesh = KnotMesh(
            initialTileOffset = tileAtPoint(insertionPoint),
            knotPrototype = knotPrototype,
            initialSize = 2,
        )

        world.knotMeshLayer.insertKnotMesh(knotMesh)

        selectEntity(knotMesh)
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