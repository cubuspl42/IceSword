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
                        tiles = world.tiles,
                    ),
                    cameraFocusPoint = world.cameraFocusPoint,
                )
            },
        )
    }
}

fun worldView_(
    world: World,
    tillDetach: Till,
): HTMLElement {
    fun <A> toRows(map: Map<IntVec2, A>): List<List<A?>> {
        val keys = map.keys

        val minX = keys.map { it.x }.minOrNull() ?: 0
        val maxX = keys.map { it.x }.maxOrNull() ?: 0

        val minY = keys.map { it.y }.minOrNull() ?: 0
        val maxY = keys.map { it.y }.maxOrNull() ?: 0

        return (minY..maxY).map { y ->
            (minX..maxX).map { x -> map[IntVec2(x, y)] }
        }
    }

    fun table(rows: List<HTMLElement>): HTMLElement =
        createHtmlElement("div").apply {
            style.display = "table"
            rows.forEach(::appendChild)
        }

    fun tr(cells: List<HTMLElement>): HTMLElement =
        createHtmlElement("div").apply {
            style.display = "table-row"
            cells.forEach(::appendChild)
        }

    fun td(child: HTMLElement): HTMLElement =
        createHtmlElement("div").apply {
            style.display = "table-cell"
            appendChild(child)
        }

    fun tileImg(tileId: Int): HTMLElement {
        val tileIdStr = tileId.toString().padStart(3, '0')
        return Image().apply {
            src = "images/CLAW/LEVEL3/TILES/ACTION/$tileIdStr.png"
            style.display = "block"
        }
    }

    val root = createHtmlElement("div")


//    val tillDetach = Till()

    root.onMouseDrag(tillDetach).reactTill(tillDetach) { mouseDrag ->
        val initialXy = mouseDrag.position.sample()
        val delta = mouseDrag.position.map { xy: IntVec2 -> xy - initialXy }

        world.dragCamera(offsetDelta = delta, tillStop = mouseDrag.tillEnd)
    }

    return table(
        toRows(world.tiles).map { tileIdRow ->
            tr(tileIdRow.map { tileId ->
                td(tileImg(tileId ?: 1))
            })
        }
    )
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
