@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.RezIndex
import icesword.editor.KnotPrototype.OvergroundRockPrototype
import icesword.editor.KnotPrototype.UndergroundRockPrototype
import icesword.frp.Cell
import icesword.frp.DynamicSet
import icesword.frp.MutCell
import icesword.frp.MutableDynamicSet
import icesword.frp.Till
import icesword.frp.filterType
import icesword.frp.map
import icesword.frp.switchMap
import icesword.frp.syncTill
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.tileAtPoint
import icesword.wwd.Wwd
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.khronos.webgl.Int32Array
import org.khronos.webgl.set

class World(
    private val wwdWorld: Wwd.World,
    initialStartPoint: IntVec2,
    initialKnotMeshes: Set<KnotMesh>,
    initialElastics: Set<Elastic>,
    initialWapObjects: Set<WapObject>,
    initialHorizontalElevators: Set<HorizontalElevator>,
    initialVerticalElevators: Set<VerticalElevator>,
    initialFloorSpikeRows: Set<FloorSpikeRow>,
) {
    companion object {
        private const val wwdPlaneIndex = 1

        fun importWwd(
            rezIndex: RezIndex,
            wwdWorld: Wwd.World,
        ): World {
            val startPoint = IntVec2(wwdWorld.startX, wwdWorld.startY)

            val initialKnotMeshes = setOf(
                KnotMesh.createSquare(
                    initialTileOffset = tileAtPoint(startPoint) + IntVec2(-2, 4),
                    knotPrototype = UndergroundRockPrototype,
                    initialSideLength = 16,
                ),
                KnotMesh.createSquare(
                    initialTileOffset = tileAtPoint(startPoint) + IntVec2(8, -4),
                    knotPrototype = OvergroundRockPrototype,
                    initialSideLength = 4,
                ),
                KnotMesh.createSquare(
                    initialTileOffset = tileAtPoint(startPoint) + IntVec2(12, -4),
                    knotPrototype = OvergroundRockPrototype,
                    initialSideLength = 1,
                ),
            )

            val initialElastics = setOf(
                Elastic(
                    prototype = LogPrototype,
                    initialBounds = IntRect(
                        position = IntVec2(83, 82),
                        size = IntSize(1, 16),
                    ),
                ),
                Elastic(
                    prototype = TreeCrownPrototype,

                    initialBounds = IntRect(
                        position = IntVec2(81, 92),
                        size = IntSize(5, 2),
                    ),
                ),
                Elastic(
                    prototype = TreeCrownPrototype,
                    initialBounds = IntRect(
                        position = IntVec2(79, 87),
                        size = IntSize(5, 2),
                    ),
                ),
                Elastic(
                    prototype = TreeCrownPrototype,
                    initialBounds = IntRect(
                        position = IntVec2(83, 84),
                        size = IntSize(5, 2),
                    ),
                ),
            )

            return World(
                wwdWorld = wwdWorld,
                initialStartPoint = startPoint,
                initialKnotMeshes = initialKnotMeshes,
                initialElastics = initialElastics,
                initialHorizontalElevators = emptySet(),
                initialVerticalElevators = emptySet(),
                initialWapObjects = emptySet(),
                initialFloorSpikeRows = emptySet(),
            )
        }

        fun load(
            rezIndex: RezIndex,
            wwdWorldTemplate: Wwd.World,
            worldData: WorldData,
        ): World {
            fun <D, E> loadInitialEntities(
                entitiesData: Set<D>,
                load: (D) -> E,
            ): Set<E> =
                entitiesData.map { load(it) }.toSet()

            val initialKnotMeshes = loadInitialEntities(
                entitiesData = worldData.knotMeshes,
                load = { KnotMesh.load(it) },
            )

            val initialElastics = loadInitialEntities(
                entitiesData = worldData.elastics,
                load = { Elastic.load(it) },
            )

            val initialHorizontalElevators = loadInitialEntities(
                entitiesData = worldData.horizontalElevators,
                load = { HorizontalElevator.load(rezIndex = rezIndex, data = it) },
            )

            val initialVerticalElevators = loadInitialEntities(
                entitiesData = worldData.verticalElevators,
                load = { VerticalElevator.load(rezIndex = rezIndex, data = it) },
            )

            val initialWapObjects = loadInitialEntities(
                entitiesData = worldData.wapObjects,
                load = { WapObject.load(rezIndex = rezIndex, data = it) },
            )

            val initialFloorSpikeRows = loadInitialEntities(
                entitiesData = worldData.floorSpikeRows,
                load = { FloorSpikeRow.load(rezIndex = rezIndex, data = it) },
            )

            return World(
                wwdWorld = wwdWorldTemplate,
                initialStartPoint = worldData.startPoint,
                initialKnotMeshes = initialKnotMeshes,
                initialElastics = initialElastics,
                initialHorizontalElevators = initialHorizontalElevators,
                initialVerticalElevators = initialVerticalElevators,
                initialWapObjects = initialWapObjects,
                initialFloorSpikeRows = initialFloorSpikeRows,
            )
        }
    }

    init {
        val w: dynamic = window
        w.world = this
    }

    private val _entities = MutableDynamicSet.of(
        initialContent = initialKnotMeshes +
                initialElastics +
                initialWapObjects +
                initialHorizontalElevators +
                initialVerticalElevators +
                initialFloorSpikeRows
    )

    val startPointEntity = StartPoint(
        initialPosition = initialStartPoint,
    )

    private val metaEntities: DynamicSet<Entity> = DynamicSet.of(
        setOf(
            startPointEntity,
        ),
    )

    val entities: DynamicSet<Entity> = DynamicSet.union(
        DynamicSet.of(
            setOf(
                _entities,
                metaEntities,
            ),
        )
    ).also {
        // FIXME
        it.changes.subscribe { }
    }

    val knotMeshes: DynamicSet<KnotMesh> = entities.filterType()

    val elastics: DynamicSet<Elastic> = entities.filterType()

    val wapObjects: DynamicSet<WapObject> = entities.filterType()

    val horizontalElevators: DynamicSet<HorizontalElevator> = entities.filterType()

    val verticalElevators: DynamicSet<VerticalElevator> = entities.filterType()

    val floorSpikeRows: DynamicSet<FloorSpikeRow> = entities.filterType()

    fun insertKnotMesh(knotMesh: KnotMesh) {
        _entities.add(knotMesh)
    }

    fun insertElastic(elastic: Elastic) {
        _entities.add(elastic)
    }

    fun insertWapObject(wapObject: WapObject) {
        _entities.add(wapObject)
    }

    fun insertElevator(elevator: Elevator<*>) {
        _entities.add(elevator)
    }

    fun insertFloorSpikeRow(floorSpikeRow: FloorSpikeRow) {
        _entities.add(floorSpikeRow)
    }

    val knotMeshLayer = KnotMeshLayer(
        knotMeshes = knotMeshes,
    )

    val metaTileLayer = MetaTileLayer(
        knotMeshLayer = knotMeshLayer,
        elastics = elastics,
    )

    val tiles = metaTileLayer.tiles

    private val _cameraFocusPoint = MutCell(initialStartPoint)

    val cameraFocusPoint: Cell<IntVec2>
        get() = _cameraFocusPoint

    fun transformToWorld(cameraPoint: IntVec2): Cell<IntVec2> =
        cameraFocusPoint.map { it + cameraPoint }

    fun transformToWorld(cameraPoint: Cell<IntVec2>): Cell<IntVec2> =
        cameraPoint.switchMap(this::transformToWorld)

    fun dragCamera(
        offsetDelta: Cell<IntVec2>,
        tillStop: Till,
    ) {
        val initialFocusPoint = _cameraFocusPoint.sample()
        val targetFocusPoint = offsetDelta.map { d -> initialFocusPoint + d }

        targetFocusPoint.syncTill(_cameraFocusPoint, till = tillStop)
    }

    fun export(): Wwd.World {
        val actionPlane = wwdWorld.planes[wwdPlaneIndex]

        val newTiles = Int32Array(Array(actionPlane.tiles.length) { -1 })

        tiles.volatileContentView.forEach { (tileOffset, tileId) ->
            val k = tileOffset.y * actionPlane.tilesWide + tileOffset.x
            newTiles[k] = tileId
        }

        val wapObjectExportables: List<WapObjectExportable> =
            entities.volatileContentView.mapNotNull { it as? WapObjectExportable }

        val objects = wapObjectExportables.flatMap { it.exportWapObjects() }

        val newActionPlane = actionPlane.copy(
            tiles = newTiles,
            objects = objects,
        )

        val startPoint = startPointEntity.position.sample()

        val newWorld = wwdWorld.copy(
            startX = startPoint.x,
            startY = startPoint.y,
            planes = wwdWorld.planes.mapIndexed { index, plane ->
                if (index == wwdPlaneIndex) newActionPlane
                else plane
            }
        )

        return newWorld
    }

    fun toData(): WorldData {
        val startPoint: IntVec2 = startPointEntity.position.sample()

        fun <E, D> exportEntitySet(
            entities: DynamicSet<E>,
            toData: (E) -> D,
        ): Set<D> =
            entities.volatileContentView.map { toData(it) }.toSet()

        val knotMeshes = exportEntitySet(
            entities = knotMeshes,
            toData = KnotMesh::toData,
        )

        val elastics = exportEntitySet(
            entities = elastics,
            toData = Elastic::toData,
        )

        val horizontalElevators = exportEntitySet(
            entities = horizontalElevators,
            toData = HorizontalElevator::toData,
        )

        // TODO: Dump it
        val verticalElevators = exportEntitySet(
            entities = verticalElevators,
            toData = VerticalElevator::toData,
        )

        val wapObjects = exportEntitySet(
            entities = wapObjects,
            toData = WapObject::toData,
        )

        val floorSpikeRows = exportEntitySet(
            entities = floorSpikeRows,
            toData = FloorSpikeRow::toData,
        )

        return WorldData(
            startPoint = startPoint,
            knotMeshes = knotMeshes,
            elastics = elastics,
            horizontalElevators = horizontalElevators,
            verticalElevators = verticalElevators,
            wapObjects = wapObjects,
            floorSpikeRows = floorSpikeRows,
        )
    }

    fun removeEntities(entities: Set<Entity>) {
        _entities.removeAll(entities)
    }

    init {
        tiles.changes.subscribe { change ->
//            println("World.tiles change: $change")
        } // FIXME
    }
}

@Serializable
data class WorldData(
    val startPoint: IntVec2,
    val knotMeshes: Set<KnotMeshData>,
    val elastics: Set<ElasticData>,
    // TODO: Nuke
    val ropes: Set<LegacyWapObjectData> = emptySet(),
    // TODO: Nuke
    val crumblingPegs: Set<LegacyWapObjectData> = emptySet(),
    val horizontalElevators: Set<HorizontalElevatorData> = emptySet(),
    val verticalElevators: Set<VerticalElevatorData> = emptySet(),
    val wapObjects: Set<WapObjectData> = emptySet(),
    val floorSpikeRows: Set<FloorSpikeRowData> = emptySet(),
)

@Serializable
data class LegacyWapObjectData(
    val position: IntVec2,
)
