package icesword.html

import icesword.MouseDrag
import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.Subscription
import icesword.frp.Till
import icesword.frp.cast
import icesword.frp.filter
import icesword.frp.hold
import icesword.frp.map
import icesword.frp.mergeWith
import icesword.frp.tillNext
import icesword.frp.units
import icesword.frp.until
import icesword.geometry.IntVec2
import org.w3c.dom.DragEvent
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent

fun Element.onMouseDown(button: MouseButton): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mousedown")
        .filter { e: MouseEvent -> e.button == button.ordinal.toShort() }

fun Element.onMouseMove(): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mousemove")

fun Element.onMouseEnter(): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mouseenter")

fun Element.onMouseLeave(): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mouseleave")

fun Element.onMouseUp(button: MouseButton): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mouseup")
        .filter { e: MouseEvent -> e.button == button.ordinal.toShort() }

fun Element.onChange(): Stream<Unit> =
    this.onEvent<Event>("change").units()

fun Element.onDragStart(): Stream<DragEvent> =
    this.onEvent<DragEvent>("dragstart")

fun Element.onDragEnd(): Stream<DragEvent> =
    this.onEvent<DragEvent>("dragend")

fun Element.onDragEnter(): Stream<DragEvent> =
    this.onEvent<DragEvent>("dragenter")

fun Element.onDragOver(): Stream<DragEvent> =
    this.onEvent<DragEvent>("dragover")

fun Element.onDragLeave(): Stream<DragEvent> =
    this.onEvent<DragEvent>("dragleave")

fun Element.onDrop(): Stream<DragEvent> =
    this.onEvent<DragEvent>("drop")

data class DragGesture(
    val onEnd: Stream<Unit>,
)

fun Element.onDragGestureStart(): Stream<DragGesture> =
    this.onDragStart().map {
        DragGesture(
            onEnd = onDragEnd().units(),
        )
    }

sealed interface MousePosition {
    data class Over(
        val clientPosition: Cell<IntVec2>,
        val relativePosition: Cell<IntVec2>,
    ) : MousePosition

    object Out : MousePosition {
        override fun toString(): String = "Out"
    }
}

fun Element.trackMousePosition(till: Till): Cell<MousePosition> {
    val onMove = this.onMouseMove()
    val onEnter = onMove.mergeWith(this.onMouseEnter())
    val onLeave = this.onMouseLeave()

    val mousePosition = onEnter.map { enterEvent ->
        val clientPosition = onMove.map { it.clientPosition }
            .hold(enterEvent.clientPosition, till = onLeave.tillNext(till))

        val relativePosition = clientPosition.map(this::calculateRelativePosition)

        MousePosition.Over(
            clientPosition = clientPosition,
            relativePosition = relativePosition,
        )
    }.mergeWith(
        onLeave.map { MousePosition.Out }
    ).hold(
        initialValue = MousePosition.Out,
        till = till,
    )

    return mousePosition
}

interface MousePressedGesture {
    val pressClientPosition: IntVec2

    val released: Till
}

fun Element.trackMousePressed(button: MouseButton, till: Till): Stream<MousePressedGesture> {
    val onMouseDown = this.onMouseDown(button = button)
    val onMouseUp = this.onMouseUp(button = button)

    return onMouseDown.map { downEvent ->
        object : MousePressedGesture {
            override val pressClientPosition: IntVec2 =
                downEvent.clientPosition

            override val released: Till =
                onMouseUp.tillNext(orTill = till)
        }
    }
}

enum class DraggableState {
    Idle,
    Dragged,
}

fun Element.trackDraggingState(till: Till): Cell<DraggableState> =
    onDragStart().map { DraggableState.Dragged }
        .mergeWith(onDragEnd().map { DraggableState.Idle })
        .hold(DraggableState.Idle, till)

enum class KeyPressedState {
    Pressed,
    Released,
}

fun Element.trackKeyPressedState(key: String, till: Till): Cell<KeyPressedState> =
    onKeyDown(key = key).map { KeyPressedState.Pressed }
        .mergeWith(onKeyUp(key = key).map { KeyPressedState.Released })
        .hold(KeyPressedState.Released, till)

fun HTMLElement.onClick(): Stream<MouseEvent> =
    this.onEvent("click")

fun Element.onKeyDown(): Stream<KeyboardEvent> =
    this.onEvent("keydown", useCapture = true)

fun Element.onKeyDown(key: String): Stream<KeyboardEvent> =
    onKeyDown().filter { it.key == key }

fun Element.onKeyUp(): Stream<KeyboardEvent> =
    this.onEvent("keyup", useCapture = true)

fun Element.onKeyUp(key: String): Stream<KeyboardEvent> =
    this.onEvent("keyup", useCapture = true)

fun HTMLElement.onWheel(): Stream<WheelEvent> =
    this.onEvent("wheel", useCapture = true)

enum class MouseButton {
    Primary,
    Middle,
    Secondary,
}

fun Element.onMouseDrag(
    button: MouseButton,
    outer: HTMLElement? = null,
    filterTarget: Boolean = false,
    till: Till,
): Stream<MouseDrag> {
    val onMouseDown = this.onMouseDown(button = button)
    val onMouseDownFiltered =
        if (filterTarget) onMouseDown.filter { it.target === this }
        else onMouseDown
    return onMouseDownFiltered
        .until(till)
        .map { event ->
            MouseDrag.start(
                element = outer ?: this,
                initialPosition = event.clientPosition,
                button = button,
                tillAbort = till,
            )
        }
}

fun <E : Event> Element.onEvent(
    eventType: String,
    useCapture: Boolean = false,
): Stream<E> =
    Stream.source<Event>(
        subscribeToSource = { notify ->
            this.subscribeToEvent(eventType, notify, useCapture = useCapture)
        },
        tag = "onEvent",
    ).cast()

fun Element.subscribeToEvent(
    eventType: String,
    callback: ((Event) -> Unit),
    useCapture: Boolean = false,
): Subscription {
    this.addEventListener(eventType, callback, useCapture)

    return object : Subscription {
        override fun unsubscribe() {
            this@subscribeToEvent.removeEventListener(eventType, callback)
        }
    }
}

val MouseEvent.clientPosition: IntVec2
    get() = IntVec2(this.clientX, this.clientY)