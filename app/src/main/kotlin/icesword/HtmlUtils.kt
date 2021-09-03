package icesword

import icesword.frp.*
import icesword.geometry.IntVec2
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent

fun createHtmlElement(tagName: String): HTMLElement =
    document.createElement(tagName) as HTMLElement

fun createContainer(
    children: DynamicSet<HTMLElement>,
    tillDetach: Till,
): HTMLElement {
    val root = createHtmlElement("div")

    children.changes.reactTill(tillDetach) { change ->
        change.added.forEach { root.appendChild(it) }
        change.removed.forEach { root.removeChild(it) }
    }

    children.sample().forEach { root.appendChild(it) }

    return root
}

fun HTMLElement.onMouseDown(button: Short): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mousedown")
        .filter { e: MouseEvent -> e.button == button }

fun HTMLElement.onMouseMove(): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mousemove")

fun HTMLElement.onMouseUp(button: Short): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mouseup")
        .filter { e: MouseEvent -> e.button == button }

fun HTMLElement.onClick(): Stream<MouseEvent> =
    this.onEvent("click")

fun HTMLElement.onKeyDown(): Stream<KeyboardEvent> =
    this.onEvent("keydown", useCapture = true)

fun HTMLElement.onMouseDrag(
    button: Short,
    outer: HTMLElement? = null,
    till: Till,
): Stream<MouseDrag> =
    this.onMouseDown(button = button).until(till).map { event ->
        MouseDrag.start(
            element = outer ?: this,
            initialPosition = event.clientPosition,
            button = button,
            tillAbort = till,
        )
    }

fun <E : Event> HTMLElement.onEvent(
    eventType: String,
    useCapture: Boolean = false,
): Stream<E> =
    Stream.source<Event>(
        subscribeToSource = { notify ->
            this.subscribeToEvent(eventType, notify, useCapture = useCapture)
        },
        tag = "onEvent",
    ).cast()

fun HTMLElement.subscribeToEvent(
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

