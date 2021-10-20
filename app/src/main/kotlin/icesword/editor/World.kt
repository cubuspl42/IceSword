@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.editor.KnotPrototype.OvergroundRockPrototype
import icesword.editor.KnotPrototype.UndergroundRockPrototype
import icesword.frp.Cell
import icesword.frp.DynamicSet
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.switchMap
import icesword.frp.syncTill
import icesword.geometry.IntVec2
import icesword.tileAtPoint
import icesword.wwd.Wwd
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.khronos.webgl.Int32Array
import org.khronos.webgl.set

class World(
    private val wwdWorld: Wwd.World?,
    val initialStartPoint: IntVec2,
    initialKnotMeshes: Set<KnotMesh>,
) {
    companion object {
        private const val wwdPlaneIndex = 1

        fun importWwd(wwdWorld: Wwd.World): World {
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

            return World(
                wwdWorld = wwdWorld,
                initialStartPoint = startPoint,
                initialKnotMeshes = initialKnotMeshes,
            )
        }

        fun load(worldData: WorldData): World {
            val initialKnotMeshes = worldData.knotMeshes.map {
                KnotMesh.load(it)
            }.toSet()

            return World(
                wwdWorld = null,
                initialStartPoint = worldData.startPoint,
                initialKnotMeshes = initialKnotMeshes,
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
    )

    val elastics = metaTileLayer.elastics


    val entities: DynamicSet<Entity> = DynamicSet.union(
        DynamicSet.of(
            setOf(
                metaEntities,
                elastics,
                knotMeshLayer.knotMeshes,
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

    fun dump(): Wwd.World {
        // FIXME
        val wwdWorld =
            this.wwdWorld ?: throw UnsupportedOperationException("Cannot export WWD when world was loaded from project")

        val actionPlane = wwdWorld.planes[wwdPlaneIndex]

        val newTiles = Int32Array(Array(actionPlane.tiles.length) { -1 })

        tiles.volatileContentView.forEach { (tileOffset, tileId) ->
            val k = tileOffset.y * actionPlane.tilesWide + tileOffset.x
            newTiles[k] = tileId
        }

        val newActionPlane = actionPlane.copy(
            tiles = newTiles,
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

        return WorldData(
            startPoint = startPoint,
            knotMeshes = knotMeshes,
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
)