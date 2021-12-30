package icesword.editor

import icesword.frp.Cell
import icesword.frp.CellLoop
import icesword.frp.DynamicSet
import icesword.frp.Stream
import icesword.frp.StreamSink
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.divertMapOrNever
import icesword.frp.map
import icesword.frp.mapNested
import icesword.frp.reactTill
import icesword.frp.switchMap
import icesword.frp.switchMapOrNull
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2


class KnotPaintMode(
    val knotPrototype: KnotPrototype,
    private val knotMeshes: DynamicSet<KnotMesh>,
    private val tillExit: Till,
) : EditorMode {

    data class BrushCursor(
        val knotArea: IntRect,
    ) {
        val knotCoord: IntVec2
            get() = knotArea.topLeft

        val knotCoords: Set<IntVec2>
            get() = knotArea.points().toSet()
    }

    sealed interface InputState

    interface BrushOverInputMode : InputState {
        val brushPosition: Cell<IntVec2>
    }

    object BrushOutInputMode : InputState

    sealed interface State {
        val enterNextState: Stream<Tilled<State>>
    }

    interface IdleMode : State {
        val readyMode: Cell<ReadyMode?>
    }

    interface PaintingMode : State {
        val targetKnotMesh: KnotMesh
    }

    interface ReadyMode {
        val targetKnotMesh: KnotMesh

        val deltaKnotMesh: KnotMesh

        fun paintKnots(stop: Stream<Unit>)

        val enterPaintingMode: Stream<Tilled<PaintingMode>>
    }

    private val inputStateLoop: CellLoop<InputState> =
        CellLoop(placeholderValue = BrushOutInputMode)

    private val inputState: Cell<InputState> = inputStateLoop.asCell

    fun closeInputLoop(inputState: Cell<InputState>) {
        inputStateLoop.close(inputState)
    }

    private val brushPosition: Cell<IntVec2?> = inputState.switchMap { inputStateNow ->
        when (inputStateNow) {
            is BrushOverInputMode -> inputStateNow.brushPosition
            BrushOutInputMode -> Cell.constant(null)
        }
    }

    val brushCursor: Cell<BrushCursor?> = brushPosition.mapNested { brushPositionNow ->
        val brushKnotCoord = closestKnot(brushPositionNow)

        BrushCursor(
            knotArea = IntRect(
                position = brushKnotCoord,
                size = IntSize.UNIT,
            )
        )
    }

    val state: Cell<State> = Stream.followTillNext<State>(
        initialValue = buildIdleMode(),
        extractNext = { it.enterNextState },
        till = tillExit,
    )

    val readyMode: Cell<ReadyMode?> =
        state.switchMapOrNull { (it as? IdleMode)?.readyMode }

    private fun buildIdleMode() = Tilled.pure(
        object : IdleMode {
            override val readyMode: Cell<ReadyMode?> =
                brushCursor.map { brushCursorOrNull ->
                    brushCursorOrNull?.let { brushCursor ->
                        buildReadyMode(brushCursor = brushCursor)
                    }
                }

            override val enterNextState: Stream<Tilled<State>> =
                readyMode.divertMapOrNever { it.enterPaintingMode }
        },
    )

    private fun buildReadyMode(brushCursor: BrushCursor): ReadyMode? {
        val targetKnotMeshOrNull = knotMeshes.volatileContentView.firstOrNull {
            val knotMeshKnotCoords = it.globalKnotCoords.volatileContentView
            val adjacentKnotCoords = brushCursor.knotArea.expand(1).points()

            it.knotPrototype == knotPrototype && adjacentKnotCoords.any(knotMeshKnotCoords::contains)
        }

        return targetKnotMeshOrNull?.let { targetKnotMesh ->
            val enterPaintingMode = StreamSink<Tilled<PaintingMode>>()

            object : ReadyMode {
                override val targetKnotMesh: KnotMesh = targetKnotMesh

                override val deltaKnotMesh: KnotMesh
                    get() = TODO("Not yet implemented")

                override fun paintKnots(stop: Stream<Unit>) {
                    enterPaintingMode.send(
                        buildPaintingMode(
                            targetKnotMesh = targetKnotMesh,
                            stop = stop,
                        )
                    )
                }

                override val enterPaintingMode: Stream<Tilled<PaintingMode>> =
                    enterPaintingMode
            }
        }
    }

    private fun buildPaintingMode(
        targetKnotMesh: KnotMesh,
        stop: Stream<Unit>,
    ) = object : Tilled<PaintingMode> {
        override fun build(till: Till): PaintingMode {
            brushCursor.reactTill(till) { brushCursorNowOrNull ->
                brushCursorNowOrNull?.let { brushCursorNow ->
                    brushCursorNow.knotCoords.forEach { knotCoord ->
                        targetKnotMesh.putKnot(globalKnotCoord = knotCoord)
                    }
                }
            }

            return object : PaintingMode {
                override val targetKnotMesh: KnotMesh = targetKnotMesh

                override val enterNextState: Stream<Tilled<State>> =
                    stop.map { buildIdleMode() }
            }
        }
    }

    override fun toString(): String = "KnotPaintMode()"
}
