package icesword.editor

import icesword.RezIndex
import icesword.editor.InsertionPrototype.ElasticInsertionPrototype
import icesword.editor.InsertionPrototype.KnotMeshInsertionPrototype
import icesword.editor.InsertionPrototype.WapObjectInsertionPrototype
import icesword.editor.SelectMode.AreaSelectingMode
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.map
import icesword.frp.mapNotNull
import icesword.frp.mapTillNext
import icesword.frp.reactTill
import icesword.frp.sample
import icesword.frp.switchMapNotNull
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
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

interface EditorMode

enum class Tool : EditorMode {
    MOVE,
    KNOT_BRUSH
}

class Editor(
    private val rezIndex: RezIndex,
    val world: World,
    // FIXME: Manage Editor's lifetime
    tillDispose: Till = Till.never,
) {
    companion object {
        fun importWwd(
            rezIndex: RezIndex,
            wwdWorld: Wwd.World,
        ): Editor {
            val world = World.importWwd(
                rezIndex = rezIndex,
                wwdWorld = wwdWorld,
            )

            return Editor(
                rezIndex = rezIndex,
                world = world,
            )
        }

        fun loadProject(
            rezIndex: RezIndex,
            wwdWorldTemplate: Wwd.World,
            projectData: ProjectData,
        ): Editor {
            val world = World.load(
                rezIndex = rezIndex,
                worldData = projectData.world,
                wwdWorldTemplate = wwdWorldTemplate,
            )

            return Editor(
                rezIndex = rezIndex,
                world = world,
            )
        }
    }

    private fun buildSelectMode() = object : Tilled<SelectMode> {
        override fun build(till: Till) = SelectMode(
            world = world,
            tillExit = till,
        )
    }

    private val _editorMode = MutCell<Tilled<EditorMode>>(
        initialValue = buildSelectMode(),
    )

    private fun enterModeTilled(mode: Tilled<EditorMode>) {
        _editorMode.set(mode)
    }

    fun enterSelectMode() {
        enterModeTilled(buildSelectMode())
    }

    private fun enterModeTillExit(build: (tillExit: Till) -> EditorMode) {
        _editorMode.set(
            object : Tilled<EditorMode> {
                override fun build(till: Till) = build(till)
            }
        )
    }

    private fun enterMode(editorMode: EditorMode) {
        enterModeTillExit { editorMode }
    }

    val editorMode: Cell<EditorMode> = _editorMode
        .mapTillNext(tillFreeze = tillDispose) { tilled, tillNext ->
            tilled.build(till = tillNext)
        }

    val selectedTool: Cell<Tool?> = editorMode.map { it as? Tool }

    val selectionMode: Cell<SelectMode?> = editorMode.map { it as? SelectMode }

    val areaSelectingMode: Cell<AreaSelectingMode?> =
        selectionMode.switchMapNotNull { it.areaSelectingMode }

    fun selectTool(tool: Tool) {
        enterMode(tool)
    }

    private val _selectedKnotBrush = MutCell(KnotBrush.Additive)

    val selectedKnotBrush: Cell<KnotBrush> = _selectedKnotBrush

    fun selectKnotBrush(knotBrush: KnotBrush) {
        selectTool(Tool.KNOT_BRUSH)
        _selectedKnotBrush.set(knotBrush)
    }

    // TODO: Implement multi-entity selection set
    private val _selectedEntity: MutCell<Entity?> = MutCell(null)

    val selectedEntity: Cell<Entity?> = _selectedEntity

    // FIXME: Port to area selection
    fun selectEntityAt(worldPosition: IntVec2) {
        val entities = world.entities.sample()

        // TODO: Implement multi-entity selection set
        val selectableEntities = entities.filter { it.isSelectableIn(area = IntRect.ZERO) }

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

    val insertionMode: Cell<InsertionMode?> = editorMode.map { it as? InsertionMode }

    val wapObjectInsertionMode: Cell<WapObjectInsertionMode?> =
        insertionMode.map { it as? WapObjectInsertionMode }

    fun enterInsertionMode(
        insertionPrototype: InsertionPrototype,
    ) {
        val insertionMode = when (insertionPrototype) {
            is ElasticInsertionPrototype -> ElasticInsertionMode(
                metaTileLayer = world.metaTileLayer,
                insertionPrototype = insertionPrototype,
            )
            is KnotMeshInsertionPrototype -> KnotMeshInsertionMode(
                knotMeshLayer = world.knotMeshLayer,
                insertionPrototype = insertionPrototype,
            )
            is WapObjectInsertionPrototype -> WapObjectInsertionMode(
                rezIndex = rezIndex,
                wapObjects = world.wapObjects,
                insertionPrototype = insertionPrototype,
            )
        }

        enterMode(insertionMode)
    }

    fun exportWorld() {
        val fileName = "test.wwd"

        val wwd = world.export()
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
