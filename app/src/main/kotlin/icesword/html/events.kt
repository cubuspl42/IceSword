package icesword.html

import icesword.MouseDrag
import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.Subscription
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.cast
import icesword.frp.filter
import icesword.frp.hold
import icesword.frp.map
import icesword.frp.mergeWith
import icesword.frp.reactTill
import icesword.frp.tillNext
import icesword.frp.units
import icesword.frp.until
import icesword.geometry.IntVec2
import org.w3c.dom.DataTransfer
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


fun Element.onDragEnter(): Stream<DragEvent> =
    this.onEvent<DragEvent>("dragenter")

fun Element.onDragOver(): Stream<DragEvent> =
    this.onEvent<DragEvent>("dragover")

fun Element.onDragLeave(): Stream<DragEvent> =
    this.onEvent<DragEvent>("dragleave")

fun Element.onDrop(): Stream<DragEvent> =
    this.onEvent<DragEvent>("drop")


sealed interface DragState {
    val nextState: Stream<Tilled<DragState>>

    class Idle(override val nextState: Stream<Tilled<DragState>>) : DragState
    class Over(val enterEvent: DragEvent, override val nextState: Stream<Tilled<DragState>>) : DragState
}

class DragHandler(
    private val element: Element,
    till: Till,
) {
    private fun buildIdleState(): Tilled<DragState.Idle> = Tilled.pure(
        DragState.Idle(
            nextState = element.onDragEnter().map { buildOverState(event = it) }
        )
    )

    private fun buildOverState(event: DragEvent): Tilled<DragState.Over> = Tilled.pure(
        DragState.Over(
            enterEvent = event,
            nextState = element.onDragLeave().map { buildIdleState() }
        )
    )

    val state: Cell<DragState> =
        Stream.follow<DragState>(
            initialValue = buildIdleState(),
            extractNext = { it.nextState },
            till = till,
        )

    val onDrop: Stream<DragEvent> =
        element.onDrop()
}

fun Element.handleDrags(
    test: (dataTransfer: DataTransfer) -> Boolean,
    till: Till,
): DragHandler {
    onDragEnter().reactTill(till) {
        console.log("onDragEnter", it.target)
    }

//    onDragOver().reactTill(till) {
//        println("onDragOver: $it")
//    }

    onDragLeave().reactTill(till) {
        console.log("onDragLeave", it.target)
    }

    onDragEnter().reactTill(till) { ev ->
        ev.dataTransfer?.let {
            if (test(it)) {
                ev.preventDefault()
            }
        }
    }

    onDragOver().reactTill(till) { ev ->
        ev.dataTransfer?.let {
            if (test(it)) {
                ev.preventDefault()
            }
        }
    }

    return DragHandler(
        element = this,
        till = till,
    )
}


sealed interface MousePosition {
    value class Entered(val position: Cell<IntVec2>) : MousePosition
    object Left : MousePosition
}

fun Element.trackMousePosition(till: Till): Cell<MousePosition> {
    val onMove = this.onMouseMove()
    val onEnter = onMove.mergeWith(this.onMouseEnter())
    val onLeave = this.onMouseLeave()

    val mousePosition = onEnter.map { enterEvent ->
        MousePosition.Entered(
            position = onMove.map { it.clientPosition }
                .hold(enterEvent.clientPosition, till = onLeave.tillNext(till)),
        )
    }.mergeWith(
        onLeave.map { MousePosition.Left }
    ).hold(
        initialValue = MousePosition.Left,
        till = till,
    )

    return mousePosition
}

fun HTMLElement.onClick(): Stream<MouseEvent> =
    this.onEvent("click")

fun HTMLElement.onKeyDown(): Stream<KeyboardEvent> =
    this.onEvent("keydown", useCapture = true)

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