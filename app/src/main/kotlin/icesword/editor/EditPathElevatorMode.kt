package icesword.editor

import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.StreamSink
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.map
import icesword.geometry.IntVec2
import kotlinx.css.pre
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.sign


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

            val threshold = 20

            val snappedPosition = targetPosition.map { tp ->
                step.previous?.let { prevStep ->
                    val pp = prevStep.position.sample()
                    val d = (tp - pp)
                    when {
                        // Horizontal axis
                        d.x.absoluteValue < threshold -> tp.copy(x = pp.x, y = tp.y)
                        // Vertical axis
                        d.y.absoluteValue < threshold -> tp.copy(x = tp.x, y = pp.y)
                        // NW/SE axis
                        (d.x - d.y).absoluteValue < threshold -> {
                            val a = max(d.x, d.y)
                            pp + IntVec2(x = a, y = a)
                        }
                        // NE/SW axis
                        (d.x + d.y).absoluteValue < threshold -> {
                            val sx = if (d.x >= 0) 1 else -1
                            val sy = if (d.y >= 0) 1 else -1
                            val a = max(d.x.absoluteValue, d.y.absoluteValue)
                            pp + IntVec2(x = sx * a, y = sy * a)
                        }
                        else -> tp
                    }
                } ?: tp
            }

            _enterMoveStepMode.send(
                object : Tilled<MoveStepMode> {
                    override fun build(till: Till): MoveStepMode {
                        step.moveTo(
                            globalPosition = snappedPosition,
                            tillStop = till,
                        )

                        return MoveStepMode(
                            targetPosition = snappedPosition,
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
