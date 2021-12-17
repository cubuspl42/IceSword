package icesword.editor

import icesword.frp.Cell
import icesword.frp.DynamicSet
import icesword.frp.MutCell
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.filterDynamic
import icesword.frp.map
import icesword.frp.reactTill
import icesword.frp.sample
import icesword.geometry.IntRect
import icesword.geometry.IntVec2


class KnotBrushMode(
    val knotMesh: KnotMesh,
) : EditorMode {
    private val _selectedKnotBrush = MutCell(KnotBrush.Additive)

    val selectedKnotBrush: Cell<KnotBrush> = _selectedKnotBrush

    fun selectKnotBrush(knotBrush: KnotBrush) {
        _selectedKnotBrush.set(knotBrush)
    }

    fun paintKnots(
        knotCoord: Cell<IntVec2>,
        till: Till,
    ) {
        val selectedBrush: KnotBrush =
            selectedKnotBrush.sample()

        knotCoord.reactTill(till) { worldPosition ->
            val globalKnotCoord = closestKnot(worldPosition)

            when (selectedBrush) {
                KnotBrush.Additive -> knotMesh.putKnot(
                    globalKnotCoord = globalKnotCoord,
                )
                KnotBrush.Eraser -> knotMesh.removeKnot(
                    globalKnotCoord = globalKnotCoord,
                )
            }
        }
    }

    override fun toString(): String = "KnotBrushMode()"
}
