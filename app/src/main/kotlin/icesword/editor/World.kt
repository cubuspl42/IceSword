package icesword.editor

import icesword.frp.*
import icesword.geometry.IntVec2
import icesword.tileAtPoint
import icesword.wwd.Wwd
import org.khronos.webgl.get

class World(
    val startPoint: IntVec2,
    val wwdTiles: Map<IntVec2, Int>,
) {
    companion object {
        fun load(wwdWorld: Wwd.World): World {
            val action = wwdWorld.planes[1]

            val startPoint = IntVec2(wwdWorld.startX, wwdWorld.startY)

            val tiles = (0 until action.tilesHigh)
                .flatMap { i ->
                    (0 until action.tilesWide).map { j ->
                        val k = i * action.tilesWide + j
                        val t = action.tiles[k]
                        IntVec2(j, i) to t
                    }
                }
                .filter { (_, tileId) -> tileId > 0 }
                .toMap()

            return World(
                startPoint = startPoint,
                wwdTiles = tiles,
            )
        }
    }

    private val baseTiles = DynamicMap.of(wwdTiles)

    val planeTiles = PlaneTiles()

    val knotMeshLayer = KnotMeshLayer(
        startPoint = startPoint,
    )

    val elastics = planeTiles.elastics


    val experimentalTileLayer = ExperimentalTileLayer(
        startPoint = startPoint,
    )

    val entities: DynamicSet<Entity> = DynamicSet.union(
        DynamicSet.of(
            setOf(
//                elastics,
                knotMeshLayer.knotMeshes,
                experimentalTileLayer.tileEntities,
            ),
        )
    ).also {
        // FIXME
        it.changes.subscribe { }
    }

//    val selectedMetaTileCluster: MetaTileCluster
//        get() = metaTileClusters.content.sample().single { it.isSelected.sample() }

//    val tiles = baseTiles
//        .unionMerge(knotMeshLayer.globalTiles, tag = ".union(knotMesh.tiles ...) ")
//        .unionMerge(planeTiles.tiles, tag = ".union(planeTiles.tiles ...)")

    val tiles = knotMeshLayer.globalTiles

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

//        println("initialFocusPoint: $initialFocusPoint")

        targetFocusPoint.syncTill(_cameraFocusPoint, till = tillStop)
    }
}

