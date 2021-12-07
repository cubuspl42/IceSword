package icesword.html

import icesword.createStackWb
import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.divertMap
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.fuseNotNull
import icesword.frp.dynamic_list.staticListOf
import icesword.frp.map
import icesword.frp.mergeWith
import icesword.frp.reactTill
import icesword.frp.units
import org.w3c.dom.DataTransfer
import org.w3c.dom.DragEvent

sealed interface DropTargetState {
    object Idle : DropTargetState

    object DragOver : DropTargetState
}

class DropTargetWidget(
    override val root: HTMLWidget,
    val state: Cell<DropTargetState>,
    val onDrop: Stream<Unit>,
) : HTMLWidget.HTMLShadowWidget

private sealed interface DropTargetWidgetState {
    val nextState: Stream<Tilled<DropTargetWidgetState>>

    val onDrop: Stream<DragEvent>

    class Idle(
        override val nextState: Stream<Tilled<DropTargetWidgetState>>,
    ) : DropTargetWidgetState {
        override val onDrop: Stream<DragEvent> = Stream.never()
    }

    class DragOver(
        val dropOverlay: HTMLWidget,
        override val onDrop: Stream<DragEvent>,
        override val nextState: Stream<Tilled<DropTargetWidgetState>>,
    ) : DropTargetWidgetState
}

// A class is needed just to have cyclic functions
private class DropTargetWidgetStateFactory(
    private val test: (dataTransfer: DataTransfer) -> Boolean,
    private val onDragEnter: Stream<DragEvent>,
) {
    fun buildIdleState(): Tilled<DropTargetWidgetState.Idle> = Tilled.pure(
        DropTargetWidgetState.Idle(
            nextState = onDragEnter.map { buildOverState() }
        )
    )

    fun buildOverState() = object : Tilled<DropTargetWidgetState.DragOver> {
        override fun build(till: Till): DropTargetWidgetState.DragOver {
            val dropOverlay = createHTMLWidgetB(
                tagName = "div",
                children = DynamicList.empty(),
            ).build(tillDetach = till)

            reactAcceptIfPassed(
                onDragEvent = dropOverlay.onDragEnter(),
                test = test,
                till = till,
            )

            reactAcceptIfPassed(
                onDragEvent = dropOverlay.onDragOver(),
                test = test,
                till = till,
            )

            val onDragLeave = dropOverlay.onDragLeave()

            val onDrop = dropOverlay.onDrop()

            return DropTargetWidgetState.DragOver(
                dropOverlay = dropOverlay,
                onDrop = onDrop,
                nextState = onDragLeave.mergeWith(onDrop).map {
                    buildIdleState()
                },
            )
        }
    }
}

fun createDropTarget(
    child: HTMLWidgetB<*>,
    test: (dataTransfer: DataTransfer) -> Boolean,
): HTMLWidgetB<DropTargetWidget> = child.flatMapTillDetach { childW, tillDetach ->
    val onChildDragEnter = childW.onDragEnter()

    val stateFactory = DropTargetWidgetStateFactory(
        onDragEnter = onChildDragEnter,
        test = test,
    )

    val state = Stream.follow<DropTargetWidgetState>(
        initialValue = stateFactory.buildIdleState(),
        extractNext = { it.nextState },
        till = tillDetach,
    )

    val onDrop = state.divertMap { it.onDrop }

    reactAcceptIfPassed(
        onDragEvent = onChildDragEnter,
        test = test,
        till = tillDetach,
    )

    // Although the drop overlay should immediately cover the child, let's properly
    // handle dragover on child as well just to be sure
    reactAcceptIfPassed(
        onDragEvent = childW.onDragOver(),
        test = test,
        till = tillDetach,
    )

    createStackWb(
        children = staticListOf(
            constant(childW),
            state.map { (it as? DropTargetWidgetState.DragOver)?.dropOverlay },
        ).fuseNotNull()
    ).map { root ->
        DropTargetWidget(
            root = root,
            state = state.map {
                when (it) {
                    is DropTargetWidgetState.Idle -> DropTargetState.Idle
                    is DropTargetWidgetState.DragOver -> DropTargetState.DragOver
                }
            },
            onDrop = onDrop.units(),
        )
    }
}

// Accept drag events if they pass the test
private fun reactAcceptIfPassed(
    onDragEvent: Stream<DragEvent>,
    test: (dataTransfer: DataTransfer) -> Boolean,
    till: Till,
) {
    onDragEvent.reactTill(till) { ev ->
        ev.dataTransfer?.let {
            if (test(it)) {
                ev.preventDefault()
            }
        }
    }
}
