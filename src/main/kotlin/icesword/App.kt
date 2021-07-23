package icesword

import icesword.editor.KnotMesh
import icesword.frp.*
import icesword.geometry.IntVec2
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

    private val knotMesh = KnotMesh(tileOffset = tileAtPoint(startPoint))

    val tiles = baseTiles.union(knotMesh.tiles)

    private val _cameraFocusPoint = MutCell(startPoint)

    val cameraFocusPoint: Cell<IntVec2>
        get() = _cameraFocusPoint

    fun dragCamera(
        offsetDelta: Cell<IntVec2>,
        tillStop: Till,
    ) {
        val initialFocusPoint = _cameraFocusPoint.sample()
        val targetFocusPoint = offsetDelta.map { d -> initialFocusPoint + d }

        println("initialFocusPoint: $initialFocusPoint")

        targetFocusPoint.syncTill(_cameraFocusPoint, till = tillStop)
    }
}
