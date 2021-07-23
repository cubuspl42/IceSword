package icesword

import icesword.frp.*
import icesword.geometry.IntVec2
import icesword.scene.Scene
import icesword.scene.TileLayer
import icesword.scene.Tileset
import icesword.scene.scene
import org.w3c.dom.HTMLElement


import kotlinx.browser.document
import org.w3c.dom.Image
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent


fun worldView(
    world: World,
    tileset: Tileset,
    tillDetach: Till,
): HTMLElement {
    val root = createHtmlElement("div").apply {
        style.width = "100%"
        style.height = "100%"
    }

    root.onMouseDrag(tillDetach).reactTill(tillDetach) { mouseDrag ->
        println("onMouseDrag")

        val initialXy = mouseDrag.position.sample()
        val delta = mouseDrag.position.map { initialXy - it }

        world.dragCamera(offsetDelta = delta, tillStop = mouseDrag.tillEnd)
    }

    return root.apply {
        appendChild(
            scene(tillDetach) { context ->
                Scene(
                    root = TileLayer(
                        tileset = tileset,
                        tiles = world.tiles.content,
                    ),
                    cameraFocusPoint = world.cameraFocusPoint,
                )
            },
        )
    }
}

private fun HTMLElement.onMouseDrag(till: Till): Stream<MouseDrag> =
    this.onMouseDown().until(till).map { event ->
        MouseDrag.start(
            element = this,
            initialPosition = event.clientPosition,
            tillAbort = till,
        )
    }

private fun HTMLElement.onMouseDown(): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mousedown").cast()

private fun HTMLElement.onMouseMove(): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mousemove").cast()

private fun HTMLElement.onMouseUp(): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mouseup").cast()

private fun <E : Event> HTMLElement.onEvent(eventType: String): Stream<E> =
    Stream.source<Event> { notify -> this.subscribeToEvent(eventType, notify) }.cast()

private val MouseEvent.clientPosition: IntVec2
    get() = IntVec2(this.clientX, this.clientY)

private fun HTMLElement.subscribeToEvent(
    eventType: String,
    callback: ((Event) -> Unit),
): Subscription {
    this.addEventListener(eventType, callback)

    return object : Subscription {
        override fun unsubscribe() {
            this@subscribeToEvent.removeEventListener(eventType, callback)
        }
    }
}

class MouseDrag(
    val position: Cell<IntVec2>,
    val tillEnd: Till,
) {
    companion object {
        fun start(
            element: HTMLElement,
            initialPosition: IntVec2,
            tillAbort: Till,
        ): MouseDrag {
            // TODO: filter button
            val tillEnd = element.onMouseUp().tillNext(tillAbort)

            val position = element.onMouseMove().map { it.clientPosition }
                .hold(initialPosition, till = tillEnd)

            return MouseDrag(
                position = position,
                tillEnd = tillEnd,
            )
        }
    }
}


private fun createHtmlElement(tagName: String): HTMLElement =
    document.createElement(tagName) as HTMLElement
