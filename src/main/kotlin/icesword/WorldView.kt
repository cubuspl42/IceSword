package icesword


import createHtmlElement
import icesword.editor.Editor
import icesword.editor.Tool
import icesword.editor.World
import icesword.frp.*
import icesword.geometry.IntVec2
import icesword.scene.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent


fun worldView(
    editor: Editor,
    tileset: Tileset,
    tillDetach: Till,
): HTMLElement {
    val world = editor.world

    val root = createHtmlElement("div").apply {
        style.width = "100%"
        style.height = "100%"
    }

    root.onMouseDrag(button = 2, tillDetach).reactTill(tillDetach) { mouseDrag ->
        val initialXy = mouseDrag.position.sample()
        val delta = mouseDrag.position.map { initialXy - it }

        world.dragCamera(offsetDelta = delta, tillStop = mouseDrag.tillEnd)
    }

    editor.selectedTool.reactTillNext(tillDetach) { tool, tillNext ->
        when (tool) {
            Tool.select -> setupSelectToolController(
                world = world,
                root = root,
                tillDetach = tillNext,
            )
            Tool.move -> setupMoveToolController(
                world = world,
                root = root,
                tillDetach = tillNext,
            )
        }
    }

//    root.onMouseDrag(button = 0, tillDetach).reactTill(tillDetach) { mouseDrag ->
//        world.transformToWorld(mouseDrag.position)
//            .reactTill(mouseDrag.tillEnd) { worldPosition ->
//                val knotCoord = closestKnot(worldPosition)
//                world.knotMesh.putKnot(knotCoord)
//            }
//    }

    return root.apply {
        val viewTransform = world.cameraFocusPoint.map { -it }

        val planeLayer = Layer(
            transform = viewTransform,
            nodes = listOf(
                TileLayer(
                    tileset = tileset,
                    tiles = world.tiles,
                ),
            ),
        )

        // TODO: React
        val planeUiLayer = Layer(
            transform = Cell.constant(IntVec2.ZERO),
            nodes = listOf(
                KnotMeshUi(
                    viewTransform = viewTransform,
                    world.knotMesh,
                ),
            ) + world.metaTileClusters.content.sample().map {
                MetaTileClusterUi(
                    viewTransform = viewTransform,
                    it,
                )
            },
        )

        appendChild(
            scene(tillDetach) {
                Scene(
                    layers = listOf(
                        planeLayer,
                        planeUiLayer,
                    ),
                )
            },
        )
    }
}

fun setupSelectToolController(
    world: World,
    root: HTMLElement,
    tillDetach: Till,
) {

}

fun setupMoveToolController(
    world: World,
    root: HTMLElement,
    tillDetach: Till,
) {
    root.onMouseDrag(button = 0, tillDetach).reactTill(tillDetach) { mouseDrag ->
        val selectedMetaTileCluster = world.selectedMetaTileCluster

        val worldPosition = world.transformToWorld(mouseDrag.position)
        val initialWorldPosition = worldPosition.sample()
        val tileOffsetDelta = worldPosition.map {
            (it - initialWorldPosition) / TILE_SIZE
        }

        selectedMetaTileCluster.move(
            tileOffsetDelta = tileOffsetDelta,
            tillStop = mouseDrag.tillEnd,
        )
    }
}

private fun HTMLElement.onMouseDrag(button: Short, till: Till): Stream<MouseDrag> =
    this.onMouseDown(button = button).until(till).map { event ->
        MouseDrag.start(
            element = this,
            initialPosition = event.clientPosition,
            button = button,
            tillAbort = till,
        )
    }

private fun HTMLElement.onMouseDown(button: Short): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mousedown")
        .filter { e: MouseEvent -> e.button == button }

private fun HTMLElement.onMouseMove(): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mousemove").cast()

private fun HTMLElement.onMouseUp(button: Short): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mouseup")
        .filter { e: MouseEvent -> e.button == button }

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
            button: Short,
            tillAbort: Till,
        ): MouseDrag {
            val tillEnd = element.onMouseUp(button = button).tillNext(tillAbort)

            val position = element.onMouseMove().map { it.clientPosition }
                .hold(initialPosition, till = tillEnd)

            return MouseDrag(
                position = position,
                tillEnd = tillEnd,
            )
        }
    }
}

