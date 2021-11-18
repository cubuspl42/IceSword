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


class KnotSelectMode(
    private val editor: Editor,
    val knotMesh: KnotMesh,
    tillExit: Till,
) : EditorMode {
    private val world: World
        get() = editor.world

    val selectMode = SelectMode(
        factory = object : SelectModeFactory<KnotAreaSelectingMode> {
            override fun createSubAreaSelectingMode(
                selectionArea: Cell<IntRect>,
                confirm: Stream<Unit>,
                tillExit: Till,
            ) = KnotAreaSelectingMode(
                selectionArea = selectionArea,
                confirm = confirm,
                tillExit = tillExit,
            )
        },
        tillExit = tillExit,
    )

    val knotAreaSelectMode: Cell<KnotAreaSelectingMode?> =
        selectMode.areaSelectingMode.map { it?.subMode }

    val knotsInSelectionArea: DynamicSet<IntVec2> = DynamicSet.diff(
        knotAreaSelectMode.map { it?.coveredKnots ?: DynamicSet.empty() }
    )

    private val _selectedKnots = MutCell(emptySet<IntVec2>())

    val selectedKnots: Cell<Set<IntVec2>> = _selectedKnots

    private fun getGlobalKnotCoordsInArea(area: Cell<IntRect>): DynamicSet<IntVec2> =
        knotMesh.globalKnotCoords.filterDynamic { globalKnotCoord: IntVec2 ->
            area.map { it.contains(knotCenter(globalKnotCoord)) }
        }

    private fun selectKnots(globalKnotCoords: Set<IntVec2>) {
        _selectedKnots.set(globalKnotCoords)
    }

    fun removeSelectedKnots() {
        knotMesh.removeKnots(selectedKnots.sample())
    }

    fun extractKnotMesh() {
        val knotsGlobalCoords = selectedKnots.sample()

        knotsGlobalCoords.firstOrNull()?.let { tileOffset ->
            val knotsLocalCoords = knotsGlobalCoords.map { it - tileOffset }.toSet()

            world.insertKnotMesh(
                knotMesh = KnotMesh(
                    knotPrototype = knotMesh.knotPrototype,
                    initialTileOffset = tileOffset,
                    initialLocalKnots = knotsLocalCoords,
                )
            )

            knotMesh.removeKnots(globalKnotCoords = knotsGlobalCoords)
        }
    }

    inner class KnotAreaSelectingMode(
        selectionArea: Cell<IntRect>,
        confirm: Stream<Unit>,
        tillExit: Till,
    ) {
        val coveredKnots: DynamicSet<IntVec2> =
            getGlobalKnotCoordsInArea(area = selectionArea)

        init {
            confirm.reactTill(till = tillExit) {
                selectKnots(coveredKnots.sample())
            }
        }

        override fun toString(): String = "KnotAreaSelectingMode()"
    }

    override fun toString(): String = "KnotSelectMode()"
}
