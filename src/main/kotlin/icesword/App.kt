package icesword

import icesword.frp.*
import icesword.geometry.IntVec2
import icesword.wwd.Wwd
import org.khronos.webgl.get

class World(
    val tiles: Map<IntVec2, Int>,
) {
    companion object {
        fun load(wwdWorld: Wwd.World): World {
            val action = wwdWorld.planes[1]

            val startV = IntVec2(wwdWorld.startX, wwdWorld.startY)
            val startTileV = startV / 64

            val i0 = startTileV.y
            val j0 = startTileV.x

            val tiles = (i0 until action.tilesHigh)
                .flatMap { i ->
                    (j0 until action.tilesWide).map { j ->
                        val k = i * action.tilesWide + j
                        val t = action.tiles[k]
                        IntVec2(j - j0, i - i0) to t
                    }
                }
                .filter { (_, tileId) -> tileId > 0 }
                .toMap()

            return World(
                tiles = tiles,
            )
        }
    }

    private val _cameraFocusPoint = MutCell(IntVec2.ZERO)

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
