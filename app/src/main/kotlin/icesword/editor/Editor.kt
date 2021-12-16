package icesword.editor

import TextureBank
import icesword.CombinedRezIndex
import icesword.JsonRezIndex
import icesword.RezIndex
import icesword.editor.InsertionPrototype.ElasticInsertionPrototype
import icesword.editor.InsertionPrototype.HorizontalElevatorInsertionPrototype
import icesword.editor.InsertionPrototype.FloorSpikeInsertionPrototype
import icesword.editor.InsertionPrototype.KnotMeshInsertionPrototype
import icesword.editor.InsertionPrototype.VerticalElevatorInsertionPrototype
import icesword.editor.InsertionPrototype.WapObjectInsertionPrototype
import icesword.editor.EntitySelectMode.EntityAreaSelectingMode
import icesword.editor.InsertionPrototype.CrateStackInsertionPrototype
import icesword.editor.InsertionPrototype.EnemyInsertionPrototype
import icesword.editor.InsertionPrototype.PathElevatorInsertionPrototype
import icesword.editor.InsertionPrototype.RopeInsertionPrototype
import icesword.editor.retails.Retail
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.Stream
import icesword.frp.StreamSink
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.contains
import icesword.frp.map
import icesword.frp.mapTillNext
import icesword.frp.reactTill
import icesword.frp.switchMap
import icesword.frp.switchMapNotNull
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
    val rezIndex: RezIndex,
    val textureBank: TextureBank,
    val world: World,
    // FIXME: Manage Editor's lifetime
    tillDispose: Till = Till.never,
) {
    companion object {
        suspend fun importWwd(
            jsonRezIndex: JsonRezIndex,
            wwdWorld: Wwd.World,
        ): Editor {
            val worldImporter = World.importWwd(
                wwdWorld = wwdWorld,
            )

            val textureBank = TextureBank.load(
                rezIndex = jsonRezIndex,
                retail = worldImporter.retail,
            )

            val combinedRezIndex = CombinedRezIndex(
                delegate = jsonRezIndex,
                textureBank = textureBank,
            )

            val world = worldImporter.import(
                rezIndex = combinedRezIndex,
            )

            return Editor(
                rezIndex = combinedRezIndex,
                textureBank = textureBank,
                world = world,
            )
        }

        suspend fun loadProject(
            jsonRezIndex: JsonRezIndex,
            // TODO: Load WWD template dynamically?
            wwdWorldTemplate: Wwd.World,
            projectData: ProjectData,
        ): Editor {
            val worldLoader = World.load(
                worldData = projectData.world,
            )

            val textureBank = TextureBank.load(
                rezIndex = jsonRezIndex,
                retail = worldLoader.retail,
            )

            val combinedRezIndex = CombinedRezIndex(
                delegate = jsonRezIndex,
                textureBank = textureBank,
            )

            val world = worldLoader.load(
                wwdWorldTemplate = wwdWorldTemplate,
                rezIndex = combinedRezIndex,
            )

            return Editor(
                rezIndex = combinedRezIndex,
                textureBank = textureBank,
                world = world,
            )
        }
    }

    private fun buildSelectMode() = object : Tilled<EntitySelectMode> {
        override fun build(till: Till) = EntitySelectMode(
            editor = this@Editor,
            tillExit = till,
        )
    }

    private fun buildKnotSelectMode(knotMesh: KnotMesh) = object : Tilled<KnotSelectMode> {
        override fun build(till: Till) = KnotSelectMode(
            editor = this@Editor,
            knotMesh = knotMesh,
            tillExit = till,
        )
    }

    private fun buildEditPathElevatorMode(
        pathElevator: PathElevator,
    ) = object : Tilled<EditPathElevatorMode> {
        override fun build(till: Till) = EditPathElevatorMode(
            editor = this@Editor,
            pathElevator = pathElevator,
            tillExit = till,
        )
    }

    val retail: Retail
        get() = world.retail

    val camera = EditorCamera(
        initialFocusPoint = world.startPointEntity.position.sample(),
    )

    private val _editorMode = MutCell<Tilled<EditorMode>>(
        initialValue = buildSelectMode(),
    )

    private fun enterModeTilled(mode: Tilled<EditorMode>) {
        _editorMode.set(mode)
    }

    fun enterSelectMode() {
        enterModeTilled(buildSelectMode())
    }

    private fun enterKnotSelectMode() {
        (selectedEntity.sample() as? KnotMesh)?.let { selectedKnotMesh ->
            enterModeTilled(buildKnotSelectMode(
                knotMesh = selectedKnotMesh,
            ))
        }
    }

    private fun enterEditPathElevatorMode() {
        (selectedEntity.sample() as? PathElevator)?.let { selectedPathElevator ->
            enterModeTilled(buildEditPathElevatorMode(
                pathElevator = selectedPathElevator,
            ))
        }
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

    val entitySelectMode: Cell<EntitySelectMode?> =
        editorMode.map { it as? EntitySelectMode }

    val knotSelectMode: Cell<KnotSelectMode?> =
        editorMode.map { it as? KnotSelectMode }

    private val entityAreaSelectingMode: Cell<EntityAreaSelectingMode?> =
        entitySelectMode.switchMapNotNull { it.entityAreaSelectingMode }

    fun isAreaSelectionCovered(entity: Entity): Cell<Boolean> =
        entityAreaSelectingMode.switchMap {
            it?.coveredEntities?.contains(entity) ?: Cell.constant(false)
        }

    fun enterMoveMode() {
        selectTool(Tool.MOVE)
    }

    private fun selectTool(tool: Tool) {
        enterMode(tool)
    }

    private val _selectedKnotBrush = MutCell(KnotBrush.Additive)

    val selectedKnotBrush: Cell<KnotBrush> = _selectedKnotBrush

    fun selectKnotBrush(knotBrush: KnotBrush) {
        selectTool(Tool.KNOT_BRUSH)
        _selectedKnotBrush.set(knotBrush)
    }

    // TODO: Restore the select-entity-below functionality

    private val _selectedEntities: MutCell<Set<Entity>> = MutCell(emptySet())

    private val selectedEntities: Cell<Set<Entity>> = _selectedEntities

    val selectedEntity: Cell<Entity?> =
        selectedEntities.map { it.singleOrNull() }

    private val _editEnemyPickups = StreamSink<Enemy>()

    val editEnemyPickups: Stream<Enemy>
        get() = _editEnemyPickups

    private val _editFloorSpikeRowSpikes = StreamSink<FloorSpikeRow>()

    val editFloorSpikeRowSpikes: Stream<FloorSpikeRow>
        get() = _editFloorSpikeRowSpikes

    private val _editRopeSpeed = StreamSink<Rope>()

    val editRopeSpeed: Stream<Rope>
        get() = _editRopeSpeed

    private val _editCrateStackPickups = StreamSink<CrateStack>()

    val editCrateStackPickups: Stream<CrateStack>
        get() = _editCrateStackPickups

    private val _editWapObjectProperties = StreamSink<WapObject>()

    val editWapObjectProperties: Stream<WapObject>
        get() = _editWapObjectProperties

    val selectionMode: Cell<SelectionMode?> =
        selectedEntity.map { selectedEntity ->
            when (selectedEntity) {
                is PathElevator -> object : PathElevatorSelectionMode {
                    override fun enterEditPathElevatorMode() {
                        this@Editor.enterEditPathElevatorMode()
                    }
                }
                is FloorSpikeRow -> object : FloorSpikeRowSelectionMode {
                    override fun editSpikes() {
                        _editFloorSpikeRowSpikes.send(selectedEntity)
                    }
                }
                is WapObject -> object : WapObjectSelectionMode {
                    override fun editProperties() {
                        _editWapObjectProperties.send(selectedEntity)
                    }
                }
                is HorizontalElevator -> null
                is VerticalElevator -> null
                is Enemy -> object : EnemySelectionMode {
                    override fun editPickups() {
                        _editEnemyPickups.send(selectedEntity)
                    }
                }
                is StartPoint -> null
                is Elastic -> null
                is KnotMesh -> object : KnotMeshSelectionMode {
                    override fun enterKnotSelectMode() {
                        this@Editor.enterKnotSelectMode()
                    }

                    override fun enterKnotBrushMode() {
                        this@Editor.selectTool(Tool.KNOT_BRUSH)
                    }
                }
                is TileEntity -> null
                is Rope -> object : RopeSelectionMode {
                    override fun editSpeed() {
                        _editRopeSpeed.send(selectedEntity)
                    }
                }
                is CrateStack -> object : CrateStackSelectionMode {
                    override fun editPickups() {
                        _editCrateStackPickups.send(selectedEntity)
                    }
                }
                null -> null
            }
        }

    fun selectEntities(entities: Set<Entity>) {
        _selectedEntities.set(entities)
    }

    fun isEntitySelected(entity: Entity): Cell<Boolean> =
        selectedEntities.map { it.contains(entity) }

    fun moveSelectedEntities(
        positionDelta: Cell<IntVec2>,
        tillStop: Till,
    ) {
        val selectedEntities = this.selectedEntities.sample()

        selectedEntities.forEach {
            it.move(
                positionDelta = positionDelta,
                tillStop = tillStop
            )
        }
    }

    fun deleteSelectedEntities() {
        val selectedEntities = this.selectedEntities.sample()

        world.removeEntities(selectedEntities)
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

    val wapObjectAlikeInsertionMode: Cell<WapObjectAlikeInsertionMode?> =
        insertionMode.map { it as? WapObjectAlikeInsertionMode }

    fun enterInsertionMode(
        insertionPrototype: InsertionPrototype,
    ) {
        val insertionMode = when (insertionPrototype) {
            is ElasticInsertionPrototype -> ElasticInsertionMode(
                world = world,
                insertionPrototype = insertionPrototype,
            )
            is KnotMeshInsertionPrototype -> KnotMeshInsertionMode(
                world = world,
                insertionPrototype = insertionPrototype,
            )
            is WapObjectInsertionPrototype -> WapObjectInsertionMode(
                rezIndex = rezIndex,
                retail = retail,
                world = world,
                insertionPrototype = insertionPrototype,
            )
            is HorizontalElevatorInsertionPrototype -> HorizontalElevatorInsertionMode(
                world = world,
                rezIndex = rezIndex,
                insertionPrototype = insertionPrototype,
            )
            is VerticalElevatorInsertionPrototype -> VerticalElevatorInsertionMode(
                world = world,
                rezIndex = rezIndex,
                insertionPrototype = insertionPrototype,
            )
            is FloorSpikeInsertionPrototype -> FloorSpikeInsertionMode(
                world = world,
                rezIndex = rezIndex,
            )
            is PathElevatorInsertionPrototype -> PathElevatorInsertionMode(
                world = world,
                rezIndex = rezIndex,
                insertionPrototype = insertionPrototype,
            )
            is EnemyInsertionPrototype -> EnemyInsertionMode(
                world = world,
                rezIndex = rezIndex,
                insertionPrototype = insertionPrototype,
            )
            is RopeInsertionPrototype -> RopeInsertionMode(
                world = world,
                rezIndex = rezIndex,
                insertionPrototype = insertionPrototype,
            )
            is CrateStackInsertionPrototype -> CrateStackInsertionMode(
                world = world,
                rezIndex = rezIndex,
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
