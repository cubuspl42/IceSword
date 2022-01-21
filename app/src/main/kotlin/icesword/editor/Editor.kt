package icesword.editor

import fetchWorld
import icesword.CombinedRezIndex
import icesword.EditorTextureBank
import icesword.JsonRezIndex
import icesword.RezIndex
import icesword.RezTextureBank
import icesword.TILE_SIZE
import icesword.asFullOfInstances
import icesword.editor.entities.CrateStack
import icesword.editor.entities.CrumblingPeg
import icesword.editor.entities.Elevator
import icesword.editor.entities.Enemy
import icesword.editor.entities.Entity
import icesword.editor.entities.EntityTilePosition
import icesword.editor.entities.Fixture
import icesword.editor.entities.FloorSpikeRow
import icesword.editor.entities.KnotMesh
import icesword.editor.entities.KnotPrototype
import icesword.editor.entities.PathElevator
import icesword.editor.entities.Rope
import icesword.editor.entities.TogglePeg
import icesword.editor.entities.WapObject
import icesword.editor.entities.Warp
import icesword.editor.entities.move
import icesword.editor.modes.EditPathElevatorMode
import icesword.editor.modes.ElasticInsertionMode
import icesword.editor.modes.EnemyInsertionMode
import icesword.editor.modes.EntitySelectMode
import icesword.editor.modes.FixtureInsertionMode
import icesword.editor.modes.FloorSpikeInsertionMode
import icesword.editor.modes.HorizontalElevatorInsertionMode
import icesword.editor.modes.InsertionMode
import icesword.editor.modes.InsertionPrototype
import icesword.editor.modes.InsertionPrototype.ElasticInsertionPrototype
import icesword.editor.modes.InsertionPrototype.EnemyInsertionPrototype
import icesword.editor.modes.InsertionPrototype.FixtureInsertionPrototype
import icesword.editor.modes.InsertionPrototype.FloorSpikeInsertionPrototype
import icesword.editor.modes.InsertionPrototype.HorizontalElevatorInsertionPrototype
import icesword.editor.modes.InsertionPrototype.KnotMeshInsertionPrototype
import icesword.editor.modes.InsertionPrototype.PathElevatorInsertionPrototype
import icesword.editor.modes.InsertionPrototype.VerticalElevatorInsertionPrototype
import icesword.editor.modes.InsertionPrototype.WapObjectAlikeInsertionPrototype
import icesword.editor.modes.InsertionPrototype.WapObjectInsertionPrototype
import icesword.editor.modes.KnotBrushMode
import icesword.editor.modes.KnotMeshInsertionMode
import icesword.editor.modes.KnotPaintMode
import icesword.editor.modes.KnotSelectMode
import icesword.editor.modes.PathElevatorInsertionMode
import icesword.editor.modes.PickWarpTargetMode
import icesword.editor.modes.SimpleWapObjectAlikeInsertionMode
import icesword.editor.modes.VerticalElevatorInsertionMode
import icesword.editor.modes.WapObjectAlikeInsertionMode
import icesword.editor.modes.WapObjectInsertionMode
import icesword.editor.retails.Retail
import icesword.frp.Cell
import icesword.frp.CellLoop
import icesword.frp.DynamicMap
import icesword.frp.DynamicView
import icesword.frp.MutCell
import icesword.frp.Stream
import icesword.frp.StreamSink
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.contentDynamicView
import icesword.frp.map
import icesword.frp.mapNested
import icesword.frp.mapTillNext
import icesword.frp.orElse
import icesword.frp.reactTill
import icesword.frp.switchMapNested
import icesword.frp.switchMapOrNull
import icesword.frp.update
import icesword.geometry.IntVec2
import icesword.loadRetailTextureBank
import icesword.tileAtPoint
import icesword.utils.roundToMultipleOf
import icesword.wwd.DumpWwd.dumpWwd
import icesword.wwd.OutputDataStream.OutputStream
import icesword.wwd.Wwd
import kotlinx.browser.document
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.File
import org.w3c.files.FilePropertyBag

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

    private fun enterPickWarpTargetMode(warp: Warp) {
        enterModeTilled(
            object : Tilled<PickWarpTargetMode> {
                override fun build(till: Till) = PickWarpTargetMode(
                    warp = warp,
                    tillExit = till,
                ).also { mode ->
                    mode.exit.reactTill(till) { enterSelectMode() }
                }
            }
        )
    }

    val editorMode: Cell<EditorMode> = _editorMode
        .mapTillNext(tillFreeze = tillDispose) { tilled, tillNext ->
            tilled.build(till = tillNext)
        }

    val selectedTool: Cell<Tool?> = editorMode.map { it as? Tool }

    private val entitySelectMode: Cell<EntitySelectMode?> =
        editorMode.map { it as? EntitySelectMode }

    private val entitySelectModeState: Cell<EntitySelectMode.State?> =
        entitySelectMode.switchMapNested { it.state }

    private val entitySelectIdleMode: Cell<EntitySelectMode.IdleMode?> =
        entitySelectModeState.map { it as? EntitySelectMode.IdleMode }

    private val entitySelectSelectingMode: Cell<EntitySelectMode.SelectingMode?> =
        entitySelectModeState.map { it as? EntitySelectMode.SelectingMode }

    val knotSelectMode: Cell<KnotSelectMode?> =
        editorMode.map { it as? KnotSelectMode }

    val knotPaintMode: Cell<KnotPaintMode?> =
        editorMode.map { it as? KnotPaintMode }

    val knotPaintOverReadyMode = knotPaintMode.switchMapOrNull { knotPaintModeNow ->
        knotPaintModeNow?.paintOverReadyMode
    }

    val selectedKnotPrototype =
        knotPaintMode.mapNested { it.knotPrototype }

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

    private val _pointerWorldCoord = CellLoop<IntVec2?>(placeholderValue = null)

    val pointerWorldPixelCoord: Cell<IntVec2?>
        get() = _pointerWorldCoord.asCell

    val pointerWorldTileCoord: Cell<IntVec2?>
        get() = pointerWorldPixelCoord.mapNested { tileAtPoint(it) }

    fun closePointerWorldPixelCoord(
        pointerWorldPixelCoord: Cell<IntVec2?>,
    ) {
        _pointerWorldCoord.close(pointerWorldPixelCoord)
    }

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

    private val _editElevatorProperties = StreamSink<Elevator<*>>()

    val editElevatorProperties: Stream<Elevator<*>>
        get() = _editElevatorProperties

    val selectionContext: Cell<SelectionContext?> = buildSingleEntitySelectionContext()
        .orElse(buildMultiEntitySelectionContext())

    private fun buildSingleEntitySelectionContext() = selectedEntity.mapNested { selectedEntity ->
        when (selectedEntity) {
            is PathElevator -> object : PathElevatorSelectionContext {
                override val entity: PathElevator = selectedEntity

                override fun enterEditPathElevatorMode() {
                    this@Editor.enterEditPathElevatorMode()
                }
            }
            is FloorSpikeRow -> object : FloorSpikeRowSelectionContext {
                override val entity: FloorSpikeRow = selectedEntity

                override fun editSpikes() {
                    _editFloorSpikeRowSpikes.send(selectedEntity)
                }
            }
            is WapObject -> object : WapObjectSelectionContext {
                override val entity: WapObject = selectedEntity

                override fun editProperties() {
                    _editWapObjectProperties.send(selectedEntity)
                }
            }
            is Enemy -> object : EnemySelectionContext {
                override val entity: Enemy = selectedEntity

                override fun editPickups() {
                    _editEnemyPickups.send(selectedEntity)
                }
            }
            is KnotMesh -> object : SingleKnotMeshSelectionContext {
                override val entity: KnotMesh = selectedEntity

                override fun enterKnotSelectMode() {
                    this@Editor.enterKnotSelectMode()
                }

                override fun enterKnotBrushMode() {
                    this@Editor.enterKnotBrushMode(
                        knotMesh = selectedEntity,
                    )
                }
            }
            is Rope -> object : RopeSelectionContext {
                override val entity: Rope = selectedEntity

                override fun editSpeed() {
                    _editRopeSpeed.send(selectedEntity)
                }
            }
            is CrateStack -> object : CrateStackSelectionContext {
                override val entity: CrateStack = selectedEntity

                override fun editPickups() {
                    _editCrateStackPickups.send(selectedEntity)
                }
            }
            is CrumblingPeg -> object : CrumblingPegSelectionContext {
                override val entity: CrumblingPeg = selectedEntity
            }
            is TogglePeg -> object : TogglePegSelectionContext {
                override val entity: TogglePeg = selectedEntity

                override fun editTiming() {
                    _editTogglePegTiming.send(selectedEntity)
                }
            }
            is Warp -> object : WarpSelectionContext {
                override val entity: Warp = selectedEntity

                override fun editTarget() {
                    _editWarpTarget.send(selectedEntity)
                }

                override fun pickTarget() {
                    enterPickWarpTargetMode(selectedEntity)
                }
            }
            is Fixture -> null
            is Elevator<*> -> object : ElevatorSelectionContext {
                override val entity: Elevator<*> = selectedEntity

                override fun editProperties() {
                    _editElevatorProperties.send(selectedEntity)
                }
            }
            else -> SimpleSingleEntitySelectionContext(
                entity = selectedEntity,
            )
        }
    }

    private fun buildMultiEntitySelectionContext() = selectedEntities.map { selectedEntitiesNow ->
        inline fun <reified TEntity : Entity> asMultipleSelection(): Set<TEntity>? {
            val entities = selectedEntitiesNow.asFullOfInstances<TEntity>()
            return entities?.takeIf { it.isNotEmpty() }?.toSet()
        }

        val selectedKnotMeshes = asMultipleSelection<KnotMesh>()

        when {
            selectedKnotMeshes != null -> object : MultipleKnotMeshesSelectionContext {
                override fun mergeKnotMeshes() {
                    KnotMesh.createMerged(selectedKnotMeshes)?.let(world::insertKnotMesh)
                    world.removeEntities(selectedKnotMeshes)
                }
            }
            else -> null
        }
    }

    fun buildEditorTilesView(): DynamicView<EditorTilesView> {
        val elasticPreviewTiles = elasticInsertionMode.switchMapNested { it.elasticPreview }
            .mapNested { it.metaTileLayerProduct.tiles }

        val fixturePreviewTiles = fixtureInsertionMode.switchMapNested { it.preview }
            .mapNested { it.metaTileLayerProduct.tiles }

        val previewTiles: Cell<DynamicMap<IntVec2, Int>> =
            elasticPreviewTiles
                .orElse(fixturePreviewTiles)
                .orElse(Cell.constant(DynamicMap.empty()))

        val primaryTilesView = world.tiles.contentDynamicView.map {
            OffsetTilesView(
                offset = IntVec2.ZERO,
                localTilesView = it,
            )
        }

        val previewTilesView = DynamicMap.diff(previewTiles).also {
            it.changes.subscribe { } // FIXME: DynamicView keep-alive
        }.contentDynamicView.map {
            OffsetTilesView(
                offset = IntVec2.ZERO,
                localTilesView = it,
            )
        }

        return DynamicView.map2(
            primaryTilesView,
            previewTilesView,
        ) {
                primaryTilesViewUnpacked,
                previewTilesViewUnpacked,
            ->
            object : EditorTilesView {
                override val primaryTilesView: TilesView = primaryTilesViewUnpacked

                override val previewTilesView: TilesView = previewTilesViewUnpacked
            }
        }
    }

    fun setSelectedEntities(entities: Set<Entity>) {
        _selectedEntities.set(entities)
    }

    fun selectEntities(entities: Set<Entity>) {
        _selectedEntities.update { it + entities }
    }

    fun unselectEntities(entities: Set<Entity>) {
        _selectedEntities.update { it - entities }
    }

    fun invertEntitySelection(entities: Set<Entity>) {
        val oldSelectedEntities = _selectedEntities.sample()

        _selectedEntities.set(
            oldSelectedEntities -
                    oldSelectedEntities.intersect(entities) +
                    (entities - oldSelectedEntities)
        )
    }

    fun isEntitySelected(entity: Entity): Cell<Boolean> =
        selectedEntities.map { it.contains(entity) }

    fun isEntityFocused(entity: Entity): Cell<Boolean> =
        entitySelectIdleMode.switchMapNested { idleMode ->
            idleMode.focusedEntity.map { it == entity }
        }.map { it ?: false }

    fun projectEntitySelectionState(entity: Entity): Cell<EntitySelectMode.SelectionState?> =
        entitySelectSelectingMode.switchMapNested { selectingMode ->
            selectingMode.selectionProjection.switchMapNested { it.projectEntitySelectionState(entity) }
        }

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

    private val elasticInsertionMode: Cell<ElasticInsertionMode?> =
        insertionMode.map { it as? ElasticInsertionMode }

    private val fixtureInsertionMode: Cell<FixtureInsertionMode?> =
        insertionMode.map { it as? FixtureInsertionMode }

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
            is FixtureInsertionPrototype -> FixtureInsertionMode(
                rezIndex = rezIndex,
                retail = retail,
                world = world,
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
