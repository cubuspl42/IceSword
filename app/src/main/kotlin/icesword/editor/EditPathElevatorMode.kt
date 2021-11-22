package icesword.editor

import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.Stream
import icesword.frp.StreamSink
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.map
import icesword.frp.switchMap
import icesword.geometry.IntVec2
import icesword.geometry.Line
import kotlinx.css.pre
import kotlin.math.absoluteValue
import kotlin.math.max


class EditPathElevatorMode(
    private val editor: Editor,
    val pathElevator: PathElevator,
    tillExit: Till,
) : EditorMode {
    sealed interface State {
        val enterNextState: Stream<Tilled<State>>
    }

    inner class IdleMode : State {
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

            fun buildSnappedPosition(
                tp: IntVec2,
                prevStep: PathElevatorStep?,
                nextStep: PathElevatorStep?,
            ): IntVec2 {
                val tp1 = when {
                    prevStep != null && nextStep != null -> snapToIntersection(
                        p1 = prevStep.position.sample(),
                        p2 = nextStep.position.sample(),
                        p3 = tp,
                    )
                    else -> null
                }

                val tp2 = when {
                    prevStep != null -> snapToLine(
                        p1 = prevStep.position.sample(),
                        p2 = tp,
                    )
                    else -> null
                }

                val tp3 = when {
                    nextStep != null -> snapToLine(
                        p1 = nextStep.position.sample(),
                        p2 = tp,
                    )
                    else -> null
                }

                return tp1 ?: tp2 ?: tp3 ?: tp
            }

            val snappedPosition = targetPosition.switchMap { tp ->
                Cell.map2(
                    step.previous,
                    step.next,
                ) { prevStep, nextStep ->
                    buildSnappedPosition(
                        tp = tp,
                        prevStep = prevStep,
                        nextStep = nextStep,
                    )
                }
            }

            _selectedStep.set(step)

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

        fun removeSelectedStep() {
            TODO("Not yet implemented")
        }
    }

    inner class MoveStepMode(
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

    private val _selectedStep = MutCell<PathElevatorStep?>(null)

    val selectedStep: Cell<PathElevatorStep?> = _selectedStep

    val state: Cell<State> = Stream.follow<State>(
        initialValue = Tilled.pure(IdleMode()),
        extractNext = { it.enterNextState },
        till = tillExit,
    )
}

private fun snapToIntersection(
    p1: IntVec2,
    p2: IntVec2,
    p3: IntVec2,
): IntVec2? {
    val threshold = 40

    fun directionalLines(p: IntVec2): List<Line> = listOf(
        Line.vertical.includingPoint(p),
        Line.horizontal.includingPoint(p),
        Line.neSw.includingPoint(p),
        Line.nwSe.includingPoint(p),
    )

    val lines1 = directionalLines(p1)
    val lines2 = directionalLines(p2)

    lines1.forEach { line1 ->
        lines2.forEach { line2 ->
            val pi = line1.intersection(line2)
            if (pi != null) {
                val d = (p3 - pi).length
                if (d < 20) {
                    return pi
                }
            }
        }
    }

    return null
}

private fun snapToLine(
    p1: IntVec2,
    p2: IntVec2,
): IntVec2? {
    val threshold = 20

    val d = (p2 - p1)

    return when {
        // Horizontal axis
        d.x.absoluteValue < threshold -> p2.copy(x = p1.x, y = p2.y)
        // Vertical axis
        d.y.absoluteValue < threshold -> p2.copy(x = p2.x, y = p1.y)
        // NW/SE axis
        (d.x - d.y).absoluteValue < threshold -> {
            val a = max(d.x, d.y)
            p1 + IntVec2(x = a, y = a)
        }
        // NE/SW axis
        (d.x + d.y).absoluteValue < threshold -> {
            val sx = if (d.x >= 0) 1 else -1
            val sy = if (d.y >= 0) 1 else -1
            val a = max(d.x.absoluteValue, d.y.absoluteValue)
            p1 + IntVec2(x = sx * a, y = sy * a)
        }
        else -> null
    }
}
