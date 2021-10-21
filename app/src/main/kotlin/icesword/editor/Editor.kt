package icesword.editor

import TextureBank
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.reactTill
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
    val textureBank: TextureBank,
    val world: World,
) {
    companion object {
        fun importWwd(
            textureBank: TextureBank,
            wwdWorld: Wwd.World,
        ): Editor {
            val world = World.importWwd(
                textureBank = textureBank,
                wwdWorld = wwdWorld,
            )

            return Editor(
                textureBank = textureBank,
                world = world,
            )
        }

        fun loadProject(
            textureBank: TextureBank,
            wwdWorldTemplate: Wwd.World,
            projectData: ProjectData,
        ): Editor {
            val world = World.load(
                textureBank = textureBank,
                worldData = projectData.world,
                wwdWorldTemplate = wwdWorldTemplate,
            )

            return Editor(
                textureBank = textureBank,
                world = world,
            )
        }
    }

    private val _selectedTool = MutCell(Tool.SELECT)

    val selectedTool: Cell<Tool> = _selectedTool

    fun selectTool(tool: Tool) {
        _selectedTool.set(tool)
    }

    private val _selectedKnotBrush = MutCell(KnotBrush.Additive)

    val selectedKnotBrush: Cell<KnotBrush> = _selectedKnotBrush

    fun selectKnotBrush(knotBrush: KnotBrush) {
        selectTool(Tool.KNOT_BRUSH)
        _selectedKnotBrush.set(knotBrush)
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

    fun paintKnots(
        knotCoord: Cell<IntVec2>,
        till: Till,
    ) {
        val selectedKnotMeshOrNull: KnotMesh? =
            selectedEntity.sample() as? KnotMesh

        val selectedBrush: KnotBrush =
            selectedKnotBrush.sample()

        selectedKnotMeshOrNull?.let { selectedKnotMesh ->
            knotCoord.reactTill(till) { worldPosition ->
                val globalKnotCoord = closestKnot(worldPosition)

                when (selectedBrush) {
                    KnotBrush.Additive -> selectedKnotMesh.putKnot(
                        globalKnotCoord = globalKnotCoord,
                    )
                    KnotBrush.Eraser -> selectedKnotMesh.removeKnot(
                        globalKnotCoord = globalKnotCoord,
                    )
                }
            }
        }
    }

    fun insertElastic(prototype: ElasticPrototype) {
        val elastic = Elastic(
            prototype = prototype,
            initialBounds = IntRect(
                position = tileAtPoint(entityInsertionPoint),
                size = prototype.defaultSize,
            ),
        )

        world.metaTileLayer.insertElastic(elastic)

        selectEntity(elastic)
    }

    fun insertKnotMesh(knotPrototype: KnotPrototype) {
        val knotMesh = KnotMesh.createSquare(
            initialTileOffset = tileAtPoint(entityInsertionPoint),
            knotPrototype = knotPrototype,
            initialSideLength = 2,
        )

        world.knotMeshLayer.insertKnotMesh(knotMesh)

        selectEntity(knotMesh)
    }

    fun insertRope() {
        world.ropes.add(
            Rope(
                texture = textureBank.rope,
                initialPosition = entityInsertionPoint,
            )
        )
    }

    private val entityInsertionPoint: IntVec2
        get() {
            val focusPoint = world.cameraFocusPoint.sample()
            val insertionPoint = focusPoint + IntVec2(512, 512)

            return insertionPoint
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
