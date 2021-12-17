package icesword.editor

import icesword.frp.Cell
import icesword.frp.DynamicSet
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.or
import icesword.frp.reactTill
import icesword.geometry.IntVec2


class KnotPaintMode(
     val knotPrototype: KnotPrototype,
    private val knotMeshes: DynamicSet<KnotMesh>,
    private val tillExit: Till,
) : EditorMode {
    fun paintKnots(
        brushWorldPosition: Cell<IntVec2>,
        tillStop: Till,
    ) {
        val brushKnotCoord = brushWorldPosition.map(::closestKnot)

        val initialBrushKnotCoord = brushKnotCoord.sample()

        val knotMeshOrNull = knotMeshes.volatileContentView.firstOrNull {
            val knotMeshKnotCoords = it.globalKnotCoords.volatileContentView

            it.knotPrototype == knotPrototype &&
                    knotMeshKnotCoords.contains(initialBrushKnotCoord)
        }

        knotMeshOrNull?.let { knotMesh ->
            brushKnotCoord.reactTill(tillStop.or(tillExit)) { globalKnotCoord ->
                knotMesh.putKnot(
                    globalKnotCoord = globalKnotCoord,
                )
            }
        }
    }

    override fun toString(): String = "KnotPaintMode()"
}
