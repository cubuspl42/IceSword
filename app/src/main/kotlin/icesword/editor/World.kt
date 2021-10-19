package icesword.editor

import icesword.frp.*
import icesword.geometry.IntVec2
import icesword.tileAtPoint
import icesword.wwd.Wwd
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import org.khronos.webgl.Int32Array
import org.khronos.webgl.get
import org.khronos.webgl.set

class World(
    private val wwdWorld: Wwd.World,
    val startPoint: IntVec2,
    val wwdTiles: Map<IntVec2, Int>,
) {
    companion object {
        private const val wwdPlaneIndex = 1

        fun load(wwdWorld: Wwd.World): World {
            val actionPlane = wwdWorld.planes[wwdPlaneIndex]

            val startPoint = IntVec2(wwdWorld.startX, wwdWorld.startY)

            val tiles = (0 until actionPlane.tilesHigh)
                .flatMap { i ->
                    (0 until actionPlane.tilesWide).map { j ->
                        val k = i * actionPlane.tilesWide + j
                        val t = actionPlane.tiles[k]
                        IntVec2(j, i) to t
                    }
                }
                .filter { (_, tileId) -> tileId > 0 }
                .toMap()

            return World(
                wwdWorld = wwdWorld,
                startPoint = startPoint,
                wwdTiles = tiles,
            )
        }
    }

    init {
        val w: dynamic = window
        w.world = this
    }

    private val baseTiles = DynamicMap.of(wwdTiles)

    val startPointEntity = StartPoint(
        initialPosition = startPoint,
    )

    private val metaEntities: DynamicSet<Entity> = DynamicSet.of(
        setOf(
            startPointEntity,
        ),
    )

    val knotMeshLayer = KnotMeshLayer(
        startPoint = startPoint,
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

    private val _cameraFocusPoint = MutCell(startPoint)

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
        val knotMeshes = knotMeshLayer.knotMeshes.volatileContentView
            .map { it.toData() }.toSet()

        return WorldData(
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
    val knotMeshes: Set<KnotMeshData>,
)