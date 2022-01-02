package icesword.editor

import icesword.editor.entities.KnotMesh
import icesword.editor.entities.closestKnot
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.reactTill
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
