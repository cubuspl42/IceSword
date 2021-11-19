package icesword.editor

import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.StreamSink
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.map
import icesword.geometry.IntVec2


class EditPathElevatorMode(
    private val editor: Editor,
    val pathElevator: PathElevator,
    tillExit: Till,
) : EditorMode {

    sealed interface State {
        val enterNextState: Stream<Tilled<State>>
    }

    class IdleMode : State {
        private val _enterMoveStepMode = StreamSink<Tilled<MoveStepMode>>()

        override val enterNextState: Stream<Tilled<MoveStepMode>>
            get() = _enterMoveStepMode

        fun moveStep(
            step: PathElevatorStep,
            positionDelta: Cell<IntVec2>,
            tillStop: Till,
        ) {
            val initialPosition = step.position.sample()
            val targetPosition = positionDelta.map { initialPosition + it }

            _enterMoveStepMode.send(
                object : Tilled<MoveStepMode> {
                    override fun build(till: Till): MoveStepMode {
                        step.moveTo(
                            globalPosition = targetPosition,
                            tillStop = till,
                        )

                        return MoveStepMode(
                            targetPosition = targetPosition,
                            tillStop = tillStop,
                        )
                    }
                }
            )
        }
    }

    class MoveStepMode(
        val targetPosition: Cell<IntVec2>,
        tillStop: Till,
    ) : State {
        private val _enterIdleMode = StreamSink<Tilled<IdleMode>>()

        override val enterNextState: Stream<Tilled<IdleMode>>
            get() = _enterIdleMode

        init {
            tillStop.subscribe {
                println("_enterIdleMode.send(Tilled.pure(IdleMode()))")
                _enterIdleMode.send(Tilled.pure(IdleMode()))
            }
        }
    }

    val state: Cell<State> = Stream.follow<State>(
        initialValue = Tilled.pure(IdleMode()),
        extractNext = { it.enterNextState },
        till = tillExit,
    )
}
