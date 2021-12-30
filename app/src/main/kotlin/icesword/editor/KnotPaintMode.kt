package icesword.editor

import icesword.frp.Cell
import icesword.frp.CellLoop
import icesword.frp.DynamicSet
import icesword.frp.Stream
import icesword.frp.StreamSink
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.divertMap
import icesword.frp.divertMapOrNever
import icesword.frp.map
import icesword.frp.mapNested
import icesword.frp.reactTill
import icesword.frp.switchMapOrNull
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2


class KnotPaintMode(
    val knotPrototype: KnotPrototype,
    private val world: World,
    tillExit: Till,
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
        val state: Cell<IdleModeState>
    }

    interface PaintingMode : State {
        val paintedKnotMesh: KnotMesh
    }

    sealed interface IdleModeState

    interface BrushOverMode : IdleModeState {
        val brushCursor: Cell<BrushCursor>

        val paintReadyMode: Cell<PaintReadyMode>
    }

    object BrushOutMode : IdleModeState

    sealed interface PaintReadyMode {
        fun paintKnots(stop: Stream<Unit>)

        val enterPaintingMode: Stream<Tilled<PaintingMode>>
    }

    interface PaintOverReadyMode : PaintReadyMode {
        val targetKnotMesh: KnotMesh
    }

    interface PaintNewReadyMode : PaintReadyMode

    private val knotMeshes: DynamicSet<KnotMesh> = world.knotMeshes

    private val inputStateLoop: CellLoop<InputState> =
        CellLoop(placeholderValue = BrushOutInputMode)

    private val inputState: Cell<InputState> = inputStateLoop.asCell

    fun closeInputLoop(inputState: Cell<InputState>) {
        inputStateLoop.close(inputState)
    }

    val state: Cell<State> = Stream.followTillNext<State>(
        initialValue = buildIdleMode(),
        extractNext = { it.enterNextState },
        till = tillExit,
    )

    private val idleMode: Cell<IdleMode?> =
        state.map { it as? IdleMode }

    val brushOverMode: Cell<BrushOverMode?> =
        idleMode.switchMapOrNull { idleModeNowOrNull ->
            idleModeNowOrNull?.state?.map { it as? BrushOverMode }
        }

    val paintReadyMode: Cell<PaintReadyMode?> =
        brushOverMode.switchMapOrNull { it?.paintReadyMode }

    val paintOverReadyMode: Cell<PaintOverReadyMode?> =
        paintReadyMode.mapNested { it as? PaintOverReadyMode }

    private fun buildIdleMode() = Tilled.pure(
        object : IdleMode {
            override val state: Cell<IdleModeState> = inputState.map { inputStateNow ->
                when (inputStateNow) {
                    is BrushOverInputMode -> buildBrushOverMode(inputStateNow)
                    BrushOutInputMode -> BrushOutMode
                }
            }

            val brushOverMode: Cell<BrushOverMode?> =
                state.map { it as? BrushOverMode }

            override val enterNextState: Stream<Tilled<State>> =
                brushOverMode.divertMapOrNever { brushOverMode ->
                    brushOverMode.paintReadyMode.divertMap { it.enterPaintingMode }
                }
        },
    )

    private fun buildBrushOverMode(inputMode: BrushOverInputMode): BrushOverMode =
        object : BrushOverMode {
            override val brushCursor: Cell<BrushCursor> = inputMode.brushPosition.map { brushPositionNow ->
                val brushKnotCoord = closestKnot(brushPositionNow)

                BrushCursor(
                    knotArea = IntRect(
                        position = brushKnotCoord,
                        size = IntSize.UNIT,
                    )
                )
            }

            override val paintReadyMode: Cell<PaintReadyMode> =
                brushCursor.map { brushCursorNow ->
                    buildPaintReadyMode(brushCursorNow = brushCursorNow)
                }

            private fun buildPaintReadyMode(brushCursorNow: BrushCursor): PaintReadyMode =
                buildPaintOverReadyMode(brushCursorNow = brushCursorNow)
                    ?: buildPaintNewReadyMode(brushCursorNow = brushCursorNow)

            private fun buildPaintOverReadyMode(brushCursorNow: BrushCursor): PaintOverReadyMode? {
                val adjacentKnotCoords = brushCursorNow.knotArea.expand(1).points()

                val targetKnotMeshOrNull = knotMeshes.volatileContentView.firstOrNull {
                    val knotMeshKnotCoords = it.globalKnotCoords.volatileContentView
                    it.knotPrototype == knotPrototype && adjacentKnotCoords.any(knotMeshKnotCoords::contains)
                }

                return targetKnotMeshOrNull?.let { targetKnotMesh ->
                    val enterPaintingMode = StreamSink<Tilled<PaintingMode>>()

                    object : PaintOverReadyMode {
                        override val targetKnotMesh: KnotMesh = targetKnotMesh

                        override fun paintKnots(stop: Stream<Unit>) {
                            enterPaintingMode.send(
                                buildPaintingMode(
                                    paintedKnotMesh = targetKnotMesh,
                                    stop = stop,
                                )
                            )
                        }

                        override val enterPaintingMode: Stream<Tilled<PaintingMode>> =
                            enterPaintingMode
                    }
                }
            }

            private fun buildPaintNewReadyMode(brushCursorNow: BrushCursor): PaintNewReadyMode {
                val enterPaintingMode = StreamSink<Tilled<PaintingMode>>()

                return object : PaintNewReadyMode {
                    override fun paintKnots(stop: Stream<Unit>) {
                        val newKnotMesh = KnotMesh.createSquare(
                            knotPrototype = knotPrototype,
                            initialTileOffset = brushCursorNow.knotCoord,
                            initialSideLength = 1,
                        )

                        world.insertKnotMesh(newKnotMesh)

                        enterPaintingMode.send(
                            buildPaintingMode(
                                paintedKnotMesh = newKnotMesh,
                                stop = stop,
                            )
                        )
                    }

                    override val enterPaintingMode: Stream<Tilled<PaintingMode>> =
                        enterPaintingMode
                }
            }

            private fun buildPaintingMode(
                paintedKnotMesh: KnotMesh,
                stop: Stream<Unit>,
            ) = object : Tilled<PaintingMode> {
                override fun build(till: Till): PaintingMode {
                    brushCursor.reactTill(till) { brushCursorNow ->
                        brushCursorNow.knotCoords.forEach { knotCoord ->
                            paintedKnotMesh.putKnot(globalKnotCoord = knotCoord)
                        }
                    }

                    return object : PaintingMode {
                        override val paintedKnotMesh: KnotMesh = paintedKnotMesh

                        override val enterNextState: Stream<Tilled<State>> =
                            stop.map { buildIdleMode() }
                    }
                }
            }
        }

    override fun toString(): String = "KnotPaintMode()"
}
