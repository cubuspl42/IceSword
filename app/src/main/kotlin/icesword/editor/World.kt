@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.RezIndex
import icesword.editor.KnotPrototype.Level3OvergroundRockPrototype
import icesword.editor.KnotPrototype.Level3UndergroundRockPrototype
import icesword.frp.DynamicSet
import icesword.frp.MutableDynamicSet
import icesword.frp.filterType
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
    val retail: Retail,
    private val wwdWorld: Wwd.World,
    initialStartPoint: IntVec2,
    initialKnotMeshes: Set<KnotMesh>,
    initialElastics: Set<Elastic>,
    initialWapObjects: Set<WapObject>,
    initialHorizontalElevators: Set<HorizontalElevator>,
    initialVerticalElevators: Set<VerticalElevator>,
    initialFloorSpikeRows: Set<FloorSpikeRow>,
    initialEntities: Set<Entity>,
) {
    companion object {
        private const val wwdPlaneIndex = 1

        fun importWwd(
            wwdWorld: Wwd.World,
        ): World {
            val name = wwdWorld.name.decode()

            val retail = extractRetail(name)

            val startPoint = IntVec2(wwdWorld.startX, wwdWorld.startY)

            val initialKnotMeshes = setOf(
                KnotMesh.createSquare(
                    initialTileOffset = tileAtPoint(startPoint) + IntVec2(-2, 4),
                    knotPrototype = Level3UndergroundRockPrototype,
                    initialSideLength = 16,
                ),
                KnotMesh.createSquare(
                    initialTileOffset = tileAtPoint(startPoint) + IntVec2(8, -4),
                    knotPrototype = Level3OvergroundRockPrototype,
                    initialSideLength = 4,
                ),
                KnotMesh.createSquare(
                    initialTileOffset = tileAtPoint(startPoint) + IntVec2(12, -4),
                    knotPrototype = Level3OvergroundRockPrototype,
                    initialSideLength = 1,
                ),
            )

            val initialElastics = setOf(
                Elastic(
                    prototype = LadderPrototype,
                    generator = LadderPrototype.buildGenerator(retail = retail),
                    initialBounds = IntRect(
                        position = IntVec2(83, 82),
                        size = IntSize(1, 8),
                    ),
                ),
            )

            return World(
                retail = retail,
                wwdWorld = wwdWorld,
                initialStartPoint = startPoint,
                initialKnotMeshes = initialKnotMeshes,
                initialElastics = initialElastics,
                initialHorizontalElevators = emptySet(),
                initialVerticalElevators = emptySet(),
                initialWapObjects = emptySet(),
                initialFloorSpikeRows = emptySet(),
                initialEntities = emptySet(),
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

            // TODO: Remove the default (3)
            val retail = Retail.fromNaturalIndex(worldData.retailNaturalIndex ?: 3)

            val initialKnotMeshes = loadInitialEntities(
                entitiesData = worldData.knotMeshes,
                load = { KnotMesh.load(it) },
            )

            val initialElastics = loadInitialEntities(
                entitiesData = worldData.elastics,
                load = { Elastic.load(retail = retail, data = it) },
            )

            val initialHorizontalElevators = loadInitialEntities(
                entitiesData = worldData.horizontalElevators,
                load = { HorizontalElevator.load(rezIndex = rezIndex, retail = retail, data = it) },
            )

            val initialVerticalElevators = loadInitialEntities(
                entitiesData = worldData.verticalElevators,
                load = { VerticalElevator.load(rezIndex = rezIndex, retail = retail, data = it) },
            )

            val initialWapObjects = loadInitialEntities(
                entitiesData = worldData.wapObjects,
                load = { WapObject.load(rezIndex = rezIndex, retail = retail, data = it) },
            )

            val initialFloorSpikeRows = loadInitialEntities(
                entitiesData = worldData.floorSpikeRows,
                load = { FloorSpikeRow.load(rezIndex = rezIndex, data = it) },
            )

            val initialEntities = worldData.entities.map {
                when (it) {
                    is EnemyData -> Enemy.load(rezIndex = rezIndex, data = it)
                    is PathElevatorData -> PathElevator.load(rezIndex = rezIndex, retail = retail, data = it)
                    is RopeData -> Rope.load(rezIndex = rezIndex, retail = retail, data = it)
                    is CrateStackData -> CrateStack.load(rezIndex = rezIndex, retail = retail, data = it)
                }
            }.toSet()

            return World(
                retail = retail,
                wwdWorld = wwdWorldTemplate,
                initialStartPoint = worldData.startPoint,
                initialKnotMeshes = initialKnotMeshes,
                initialElastics = initialElastics,
                initialHorizontalElevators = initialHorizontalElevators,
                initialVerticalElevators = initialVerticalElevators,
                initialWapObjects = initialWapObjects,
                initialFloorSpikeRows = initialFloorSpikeRows,
                initialEntities = initialEntities,
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
                initialFloorSpikeRows +
                initialEntities
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

    fun insertEntity(entity: Entity) {
        _entities.add(entity)
    }

    val knotMeshLayer = KnotMeshLayer(
        knotMeshes = knotMeshes,
        knotMetaTileBuilder = retail.buildKnotMetaTileBuilder(),
    )

    val metaTileLayer = MetaTileLayer(
        knotMeshLayer = knotMeshLayer,
        elastics = elastics,
    )

    val tiles = metaTileLayer.tiles


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

        val entities = this.entities.volatileContentView
            .mapNotNull { it.toEntityData() }

        return WorldData(
            retailNaturalIndex = retail.naturalIndex,
            startPoint = startPoint,
            knotMeshes = knotMeshes,
            elastics = elastics,
            horizontalElevators = horizontalElevators,
            verticalElevators = verticalElevators,
            wapObjects = wapObjects,
            floorSpikeRows = floorSpikeRows,
            entities = entities,
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
    val retailNaturalIndex: Int? = null,
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
    val entities: List<EntityData> = emptyList(),
)

@Serializable
data class LegacyWapObjectData(
    val position: IntVec2,
)

private val levelIndexRegex = """(\d+)""".toRegex()

fun extractRetail(name: String): Retail {
    val levelIndexMatch =
        levelIndexRegex.find(name) ?: throw IllegalArgumentException("No retail index in world's name: $name")
    val levelNaturalIndex = levelIndexMatch.value.toInt()
    val retail = Retail.fromNaturalIndex(levelNaturalIndex)
    return retail
}
