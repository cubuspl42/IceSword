package icesword.editor.modes

import icesword.editor.EditorMode
import icesword.editor.entities.Warp
import icesword.frp.Cell
import icesword.frp.CellLoop
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.divertMap
import icesword.frp.map
import icesword.frp.mapTillNext
import icesword.frp.reactTill
import icesword.geometry.IntVec2


class PickWarpTargetMode(
    val warp: Warp,
    tillExit: Till,
) : EditorMode {
    sealed interface InputState

    interface PickerOverInputState : InputState {
        val pickerWorldPosition: Cell<IntVec2>

        val pick: Stream<Unit>
    }

    object PickerOutInputState : InputState

    sealed interface State {
        val pickerWorldPosition: Cell<IntVec2>?

        val exit: Stream<Unit>
    }

    object IdleState : State {
        override val pickerWorldPosition: Cell<IntVec2>? = null

        override val exit: Stream<Unit> = Stream.never()
    }

    class PickingState(
        inputState: PickerOverInputState,
    ) : State {
        override val pickerWorldPosition: Cell<IntVec2> =
            inputState.pickerWorldPosition

        override val exit: Stream<Unit> =
            inputState.pick
    }

    private val inputStateLoop: CellLoop<InputState> =
        CellLoop(placeholderValue = PickerOutInputState)

    private val inputState: Cell<InputState> = inputStateLoop.asCell

    fun closeInputLoop(inputState: Cell<InputState>) {
        inputStateLoop.close(inputState)
    }

    val state = inputState.mapTillNext(tillExit) { inputState, tillNext ->
        when (inputState) {
            is PickerOverInputState -> buildPickingState(
                inputState = inputState,
                tillNext = tillNext,
            )
            PickerOutInputState -> IdleState
        }
    }

    val pickingState: Cell<PickingState?> =
        state.map { it as? PickingState }

    private fun buildPickingState(
        inputState: PickerOverInputState,
        tillNext: Till,
    ): PickingState {
        inputState.pick.reactTill(tillNext) {
            warp.setTargetPosition(
                inputState.pickerWorldPosition.sample(),
            )
        }

        return PickingState(inputState = inputState)
    }

    val exit: Stream<Unit> =
        state.divertMap { it.exit }

    override fun toString(): String = "PickWarpTargetMode()"
}
