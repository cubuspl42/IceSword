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
    initialElevators: Set<Elevator>,
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
                initialElevators = emptySet(),
                initialWapObjects = emptySet()
            )
        }

        fun load(
            rezIndex: RezIndex,
            wwdWorldTemplate: Wwd.World,
            worldData: WorldData,
        ): World {
            val initialKnotMeshes = worldData.knotMeshes.map {
                KnotMesh.load(it)
            }.toSet()

            val initialElastics = worldData.elastics.map {
                Elastic.load(it)
            }.toSet()

            val ropes = worldData.ropes.map {
                WapObjectData(
                    prototype = WapObjectPrototype.RopePrototype,
                    position = it.position,
                )
            }.toSet()

            val crumblingPegs = worldData.crumblingPegs.map {
                WapObjectData(
                    prototype = WapObjectPrototype.RopePrototype,
                    position = it.position,
                )
            }.toSet()

            val initialElevators = worldData.elevators.map {
                Elevator.load(
                    rezIndex = rezIndex,
                    data = it,
                )
            }.toSet()

            val initialWapObjects = (worldData.wapObjects + ropes + crumblingPegs).map {
                WapObject.load(
                    rezIndex = rezIndex,
                    data = it,
                )
            }.toSet()

            return World(
                wwdWorld = wwdWorldTemplate,
                initialStartPoint = worldData.startPoint,
                initialKnotMeshes = initialKnotMeshes,
                initialElastics = initialElastics,
                initialElevators = initialElevators,
                initialWapObjects = initialWapObjects,
            )
        }
    }

    init {
        val w: dynamic = window
        w.world = this
    }

    val startPointEntity = StartPoint(
        initialPosition = initialStartPoint,
    )

    private val metaEntities: DynamicSet<Entity> = DynamicSet.of(
        setOf(
            startPointEntity,
        ),
    )

    val knotMeshLayer = KnotMeshLayer(
        startPoint = initialStartPoint,
        initialKnotMeshes = initialKnotMeshes,
    )

    val metaTileLayer = MetaTileLayer(
        knotMeshLayer = knotMeshLayer,
        initialElastics = initialElastics,
    )

    val elastics = metaTileLayer.elastics

    private val _wapObjects = MutableDynamicSet.of(
        initialWapObjects,
    )

    val wapObjects: MutableDynamicSet<WapObject>
        get() = _wapObjects

    private val _elevators = MutableDynamicSet.of(
        initialElevators,
    )

    val elevators: DynamicSet<Elevator>
        get() = _elevators

    fun insertElevator(elevator: Elevator) {
        _elevators.add(elevator)
    }

    val entities: DynamicSet<Entity> = DynamicSet.union(
        DynamicSet.of(
            setOf(
                metaEntities,
                elastics,
                knotMeshLayer.knotMeshes,
                elevators,
                wapObjects,
            ),
        )
    ).also {
        // FIXME
        it.changes.subscribe { }
    }

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

        val wapObjectExportables: Set<WapObjectExportable> =
            (wapObjects.volatileContentView + elevators.volatileContentView)

        val objects = wapObjectExportables.map { it.exportWapObject() }

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

        val knotMeshes = knotMeshLayer.knotMeshes.volatileContentView
            .map { it.toData() }.toSet()

        val elastics = metaTileLayer.elastics.volatileContentView
            .map { it.toData() }.toSet()

        val elevators = elevators.volatileContentView
            .map { it.toData() }.toSet()

        val wapObjects = wapObjects.volatileContentView
            .map { it.toData() }.toSet()

        return WorldData(
            startPoint = startPoint,
            knotMeshes = knotMeshes,
            elastics = elastics,
            elevators = elevators,
            wapObjects = wapObjects,
        )
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
    val ropes: Set<LegacyWapObjectData> = emptySet(),
    val crumblingPegs: Set<LegacyWapObjectData> = emptySet(),
    val elevators: Set<ElevatorData> = emptySet(),
    val wapObjects: Set<WapObjectData> = emptySet(),
)


@Serializable
data class LegacyWapObjectData(
    val position: IntVec2,
)
