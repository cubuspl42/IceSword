package icesword.editor

import fetchWorld
import icesword.CombinedRezIndex
import icesword.EditorTextureBank
import icesword.JsonRezIndex
import icesword.RezIndex
import icesword.RezTextureBank
import icesword.TILE_SIZE
import icesword.editor.InsertionPrototype.ElasticInsertionPrototype
import icesword.editor.InsertionPrototype.HorizontalElevatorInsertionPrototype
import icesword.editor.InsertionPrototype.FloorSpikeInsertionPrototype
import icesword.editor.InsertionPrototype.KnotMeshInsertionPrototype
import icesword.editor.InsertionPrototype.VerticalElevatorInsertionPrototype
import icesword.editor.InsertionPrototype.WapObjectInsertionPrototype
import icesword.editor.EntitySelectMode.EntityAreaSelectingMode
import icesword.editor.InsertionPrototype.EnemyInsertionPrototype
import icesword.editor.InsertionPrototype.PathElevatorInsertionPrototype
import icesword.editor.InsertionPrototype.WapObjectAlikeInsertionPrototype
import icesword.editor.retails.Retail
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.Stream
import icesword.frp.StreamSink
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.contains
import icesword.frp.map
import icesword.frp.mapNested
import icesword.frp.mapTillNext
import icesword.frp.switchMap
import icesword.frp.switchMapNotNull
import icesword.frp.switchMapOrNull
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.loadRetailTextureBank
import icesword.utils.roundToMultipleOf
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
}

class Editor(
    val rezIndex: RezIndex,
    val editorTextureBank: EditorTextureBank,
    val textureBank: RezTextureBank,
    val world: World,
    // FIXME: Manage Editor's lifetime
    tillDispose: Till = Till.never,
) {
    companion object {
        suspend fun createProject(
            jsonRezIndex: JsonRezIndex,
            editorTextureBank: EditorTextureBank,
            retail: Retail,
        ): Editor {
            val retailTextureBank = loadRetailTextureBank(
                retail = retail,
            )

            val textureBank = RezTextureBank.chained(
                textureBank1 = retailTextureBank,
                textureBank2 = editorTextureBank.gameTextureBank,
            )

            val combinedRezIndex = CombinedRezIndex(
                delegate = jsonRezIndex,
                textureBank = textureBank,
            )

            val wwdWorldTemplate: Wwd.World = fetchWorld(
                retail = retail,
            )

            val world = World.createEmpty(
                retail = retail,
                wwdWorld = wwdWorldTemplate,
                initialStartPoint = IntVec2(100 * 64, 50 * 64)
            )

            return Editor(
                rezIndex = combinedRezIndex,
                editorTextureBank = editorTextureBank,
                textureBank = textureBank,
                world = world,
            )
        }

        suspend fun importWwd(
            jsonRezIndex: JsonRezIndex,
            editorTextureBank: EditorTextureBank,
            wwdWorld: Wwd.World,
        ): Editor {
            val worldImporter = World.importWwd(
                wwdWorld = wwdWorld,
            )

            val retailTextureBank = loadRetailTextureBank(
                retail = worldImporter.retail,
            )

            val textureBank = RezTextureBank.chained(
                textureBank1 = retailTextureBank,
                textureBank2 = editorTextureBank.gameTextureBank,
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
                editorTextureBank = editorTextureBank,
                textureBank = textureBank,
                world = world,
            )
        }

        suspend fun loadProject(
            jsonRezIndex: JsonRezIndex,
            editorTextureBank: EditorTextureBank,
            projectData: ProjectData,
        ): Editor {
            val worldLoader = World.load(
                worldData = projectData.world,
            )

            val retail = worldLoader.retail

            val retailTextureBank = loadRetailTextureBank(
                retail = retail,
            )

            val textureBank = RezTextureBank.chained(
                textureBank1 = retailTextureBank,
                textureBank2 = editorTextureBank.gameTextureBank,
            )

            val combinedRezIndex = CombinedRezIndex(
                delegate = jsonRezIndex,
                textureBank = textureBank,
            )

            val wwdWorldTemplate: Wwd.World = fetchWorld(
                retail = retail,
            )

            val world = worldLoader.load(
                wwdWorldTemplate = wwdWorldTemplate,
                rezIndex = combinedRezIndex,
            )

            return Editor(
                rezIndex = combinedRezIndex,
                editorTextureBank = editorTextureBank,
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

    private fun buildKnotPaintMode(knotPrototype: KnotPrototype) = object : Tilled<KnotPaintMode> {
        override fun build(till: Till) = KnotPaintMode(
            knotPrototype = knotPrototype,
            world = world,
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

    private fun enterKnotBrushMode(
        knotMesh: KnotMesh,
    ) {
        enterMode(
            editorMode = KnotBrushMode(
                knotMesh = knotMesh,
            ),
        )
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

    fun enterKnotPaintMode(knotPrototype: KnotPrototype) {
        enterModeTilled(
            mode = buildKnotPaintMode(knotPrototype = knotPrototype),
        )
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

    val knotPaintMode: Cell<KnotPaintMode?> =
        editorMode.map { it as? KnotPaintMode }

    val knotPaintOverReadyMode = knotPaintMode.switchMapOrNull { knotPaintModeNow ->
        knotPaintModeNow?.paintOverReadyMode
    }

    val selectedKnotPrototype =
        knotPaintMode.mapNested { it.knotPrototype }

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

    private val _editTogglePegTiming = StreamSink<TogglePeg>()

    val editTogglePegTiming: Stream<TogglePeg>
        get() = _editTogglePegTiming

    private val _editWarpTarget = StreamSink<Warp>()

    val editWarpTarget: Stream<Warp>
        get() = _editWarpTarget

    val selectionMode: Cell<SelectionMode?> =
        selectedEntity.map { selectedEntity ->
            when (selectedEntity) {
                null -> null
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
                        this@Editor.enterKnotBrushMode(
                            knotMesh = selectedEntity,
                        )
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
                is CrumblingPeg -> CrumblingPegSelectionMode(
                    crumblingPeg = selectedEntity,
                )
                is TogglePeg -> object : TogglePegSelectionMode {
                    override fun editTiming() {
                        _editTogglePegTiming.send(selectedEntity)
                    }
                }
                is Warp -> object : WarpSelectionMode {
                    override fun editTarget() {
                        _editWarpTarget.send(selectedEntity)
                    }
                }
            }
        }

    fun selectEntitiesByArea(
        entities: Set<Entity>,
        selectionArea: IntRect,
    ) {
        val viewTransform = camera.transform.transform.sample()
        val viewSelectionArea = viewTransform.transform(selectionArea)

        if (viewSelectionArea.area > 4) {
            _selectedEntities.set(entities)
        } else {
            // If selection area is very small, let's assume the intention was
            // to select the next entity, not perform actual area selection

            val selectedEntities = this.selectedEntities.sample()

            val entitiesInArea = this.world.entities.volatileContentView
                .filter { it.isSelectableIn(selectionArea) }

            if (entitiesInArea.isEmpty()) {
                _selectedEntities.set(emptySet())
            } else {
                val entityToSelect = selectedEntities.singleOrNull()?.let { selectedEntity ->
                    val selectedEntityIndex = entitiesInArea.indexOfOrNull(selectedEntity)
                    selectedEntityIndex?.let { entitiesInArea[(it + 1) % entitiesInArea.size] }
                } ?: entitiesInArea.first()

                _selectedEntities.set(setOf(entityToSelect))
            }
        }
    }

    fun isEntitySelected(entity: Entity): Cell<Boolean> =
        selectedEntities.map { it.contains(entity) }

    fun moveSelectedEntities(
        positionDelta: Cell<IntVec2>,
        tillStop: Till,
    ) {
        val selectedEntitiesPositions = this.selectedEntities.sample()
            .map { it.entityPosition }

        val effectivePositionDelta = when {
            selectedEntitiesPositions.any { it is EntityTilePosition } ->
                positionDelta.map { p: IntVec2 -> p.map { it.roundToMultipleOf(TILE_SIZE) } }
            else -> positionDelta
        }

        selectedEntitiesPositions.forEach {
            it.move(
                positionDelta = effectivePositionDelta,
                tillStop = tillStop
            )
        }
    }

    fun deleteSelectedEntities() {
        val selectedEntities = this.selectedEntities.sample()

        world.removeEntities(selectedEntities)
    }

    val insertionMode: Cell<InsertionMode?> = editorMode.map { it as? InsertionMode }

    val wapObjectAlikeInsertionMode: Cell<WapObjectAlikeInsertionMode?> =
        insertionMode.map { it as? WapObjectAlikeInsertionMode }

    fun enterInsertionMode(
        insertionPrototype: InsertionPrototype,
    ) {
        val insertionMode = when (insertionPrototype) {
            is WapObjectAlikeInsertionPrototype -> SimpleWapObjectAlikeInsertionMode(
                rezIndex = rezIndex,
                retail = retail,
                world = world,
                insertionPrototype = insertionPrototype,
            )
            is ElasticInsertionPrototype -> ElasticInsertionMode(
                rezIndex = rezIndex,
                retail = retail,
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
