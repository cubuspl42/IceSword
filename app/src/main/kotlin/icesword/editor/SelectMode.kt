package icesword.editor

import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.StreamSink
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.map
import icesword.geometry.IntRect
import icesword.geometry.IntVec2

interface SelectModeFactory<
        SubAreaSelectingMode,
        > {
    fun createSubAreaSelectingMode(
        selectionArea: Cell<IntRect>,
        confirm: Stream<Unit>,
        tillExit: Till,
    ): SubAreaSelectingMode
}

class SelectMode<
        SubAreaSelectingMode,
        >(
    private val factory: SelectModeFactory<SubAreaSelectingMode>,
    tillExit: Till,
) : EditorMode {

    open inner class SelectModeState

    val state: Cell<SelectModeState> = Stream.followTillNext(
        initialValue = object : Tilled<IdleMode> {
            override fun build(till: Till) = IdleMode()
        },
        extractNext = { mode: SelectModeState ->
            when (mode) {
                is IdleMode -> mode.enterAreaSelectingMode
                is AreaSelectingMode -> mode.enterIdleMode
                else -> throw UnsupportedOperationException()
            }
        },
        till = tillExit,
    )

    val areaSelectingMode: Cell<AreaSelectingMode?> =
        state.map { st -> st as? AreaSelectingMode }

    override fun toString(): String = "SelectMode()"

    inner class IdleMode : SelectModeState() {
        private val _enterAreaSelectingMode = StreamSink<Tilled<AreaSelectingMode>>()

        val enterAreaSelectingMode: Stream<Tilled<AreaSelectingMode>>
            get() = _enterAreaSelectingMode

        fun selectArea(
            anchorWorldCoord: IntVec2,
            targetWorldCoord: Cell<IntVec2>,
            confirm: Stream<Unit>,
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
                        subMode = factory.createSubAreaSelectingMode(
                            selectionArea = selectionArea,
                            confirm = confirm,
                            tillExit = till,
                        ),
                        selectionArea = selectionArea,
                        confirm = confirm,
                        tillExit = till,
                    )
                }
            )
        }

        override fun toString(): String = "IdleMode()"
    }

    inner class AreaSelectingMode(
        val subMode: SubAreaSelectingMode,
        val selectionArea: Cell<IntRect>,
        confirm: Stream<Unit>,
        tillExit: Till,
    ) : SelectModeState() {
        val enterIdleMode: Stream<Tilled<IdleMode>> = confirm.map {
            object : Tilled<IdleMode> {
                override fun build(till: Till): SelectMode<SubAreaSelectingMode>.IdleMode =
                    IdleMode()
            }
        }

        override fun toString(): String = "AreaSelectingMode()"
    }
}
