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
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent

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