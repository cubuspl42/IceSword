package icesword.editor

import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.sample
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.scene.Tileset
import icesword.tileAtPoint
import icesword.wwd.DumpWwd.dumpWwd
import icesword.wwd.OutputDataStream.OutputStream
import icesword.wwd.Wwd
import kotlinx.browser.document
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.File
import org.w3c.files.FilePropertyBag
import kotlinx.serialization.encodeToString

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
        fun importWwd(
            tileset: Tileset,
            wwdWorld: Wwd.World,
        ): Editor {
            val world = World.importWwd(wwdWorld)

            return Editor(
                world = world,
                tileset = tileset,
            )
        }

        fun loadProject(
            tileset: Tileset,
            projectData: ProjectData,
        ): Editor {
            val world = World.load(
                worldData = projectData.world,
            )

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

        val knotMesh = KnotMesh.createSquare(
            initialTileOffset = tileAtPoint(insertionPoint),
            knotPrototype = knotPrototype,
            initialSideLength = 2,
        )

        world.knotMeshLayer.insertKnotMesh(knotMesh)

        selectEntity(knotMesh)
    }

    fun exportWorld() {
        val fileName = "test.wwd"

        val wwd = world.dump()
        val outputStream = OutputStream()

        dumpWwd(outputStream = outputStream, wwd = wwd)

        val worldBuffer = outputStream.toArrayBuffer()

        val worldFile = File(
            arrayOf(worldBuffer),
            fileName,
            FilePropertyBag(
                type = "application/x-wwd",
            ),
        )

        console.log("worldFile", worldFile)

        downloadFile(file = worldFile)
    }


    private fun toProjectData(): ProjectData =
        ProjectData(
            world = world.toData(),
        )

    fun saveProject() {
        val projectData = toProjectData()
        val projectDataString = Json.encodeToString(projectData)

        val projectFile = File(
            fileBits = arrayOf(projectDataString),
            fileName = "test.iceSword.json",
            options = FilePropertyBag(
                type = "application/x-wwd",
            ),
        )

        downloadFile(file = projectFile)
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

@Serializable
data class ProjectData(
    val world: WorldData,
)

private fun downloadFile(file: File) {
    val element = createAnchorElement(
        href = URL.createObjectURL(file),
        download = file.name,
    )

    element.click()
}

private fun createAnchorElement(href: String, download: String): HTMLAnchorElement {
    val element = document.createElement("a") as HTMLAnchorElement
    element.href = href
    element.download = download
    return element
}
