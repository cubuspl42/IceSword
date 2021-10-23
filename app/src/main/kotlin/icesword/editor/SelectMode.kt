package icesword.editor

import icesword.frp.Cell
import icesword.frp.DynamicSet
import icesword.frp.Stream
import icesword.frp.StreamSink
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.map
import icesword.frp.mergeWith
import icesword.frp.reactTill
import icesword.geometry.IntRect
import icesword.geometry.IntVec2

sealed interface SelectModeState

class SelectMode(
    world: World,
    tillExit: Till,
) : EditorMode {
    val state: Cell<SelectModeState> = Stream.follow(
        initialValue = object : Tilled<IdleMode> {
            override fun build(till: Till) = IdleMode()
        },
        extractNext = { mode: SelectModeState ->
            when (mode) {
                is IdleMode -> mode.enterAreaSelectingMode
                is AreaSelectingMode -> mode.enterIdleMode
            }
        },
        till = tillExit,
    )

    val areaSelectingMode: Cell<AreaSelectingMode?> =
        state.map { st -> st as? SelectMode.AreaSelectingMode }

    // TODO: Implement
    private fun getEntitiesInArea(area: Cell<IntRect>): DynamicSet<Entity> =
        DynamicSet.of(emptySet())

    // TODO: Implement
    private fun selectEntities(entities: Set<Entity>) {
    }

    override fun toString(): String = "SelectMode()"

    inner class IdleMode : SelectModeState {
        private val _enterAreaSelectingMode = StreamSink<Tilled<AreaSelectingMode>>()

        val enterAreaSelectingMode: Stream<Tilled<AreaSelectingMode>>
            get() = _enterAreaSelectingMode

        fun selectArea(
            anchorWorldCoord: IntVec2,
            targetWorldCoord: Cell<IntVec2>,
            confirm: Stream<Unit>,
            abort: Stream<Unit>,
        ) {
            val selectionArea: Cell<IntRect> =
                targetWorldCoord.map { target ->
                    IntRect.fromDiagonal(
                        anchorWorldCoord,
                        target,
                    )
                }

            _enterAreaSelectingMode.send(
                object : Tilled<AreaSelectingMode> {
                    override fun build(till: Till) = AreaSelectingMode(
                        selectionArea = selectionArea,
                        confirm = confirm,
                        abort = abort,
                        tillExit = till,
                    )
                }
            )
        }

        override fun toString(): String = "IdleMode()"
    }

    inner class AreaSelectingMode(
        val selectionArea: Cell<IntRect>,
        confirm: Stream<Unit>,
        abort: Stream<Unit>,
        tillExit: Till,
    ) : SelectModeState {

        val enterIdleMode: Stream<Tilled<IdleMode>> =
            confirm.mergeWith(abort).map {
                object : Tilled<IdleMode> {
                    override fun build(till: Till): IdleMode = IdleMode()
                }
            }

        val coveredEntities: DynamicSet<Entity> =
            getEntitiesInArea(
                area = selectionArea,
            )

        init {
            confirm.reactTill(till = tillExit) {
                selectEntities(coveredEntities.volatileContentView)
            }
        }

        override fun toString(): String = "AreaSelectingMode()"
    }
}
