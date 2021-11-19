package icesword.editor

import icesword.frp.Cell
import icesword.frp.DynamicSet
import icesword.frp.Stream
import icesword.frp.StreamSink
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.filterDynamic
import icesword.frp.map
import icesword.frp.mergeWith
import icesword.frp.reactTill
import icesword.geometry.IntRect
import icesword.geometry.IntVec2

sealed interface Foo<T> {
    class Bar<T> : Foo<T>
    class Baz<T> : Foo<T>
}

fun <T> f(foo: Foo<T>) {
    when (foo) {
        is Foo.Bar -> TODO()
        is Foo.Baz -> TODO()
    }

}

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

    val state: Cell<SelectModeState> = Stream.follow(
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
                        subMode = factory.createSubAreaSelectingMode(
                            selectionArea = selectionArea,
                            confirm = confirm,
                            tillExit = till,
                        ),
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
        val subMode: SubAreaSelectingMode,
        val selectionArea: Cell<IntRect>,
        confirm: Stream<Unit>,
        abort: Stream<Unit>,
        tillExit: Till,
    ) : SelectModeState() {
        val enterIdleMode: Stream<Tilled<IdleMode>> =
            confirm.mergeWith(abort).map {
                object : Tilled<IdleMode> {
                    override fun build(till: Till): SelectMode<SubAreaSelectingMode>.IdleMode =
                        IdleMode()
                }
            }

//        protected abstract fun onConfirm()

        fun onConfirm() {}

        init {
            confirm.reactTill(till = tillExit) { onConfirm() }
        }

        override fun toString(): String = "AreaSelectingMode()"
    }
}
