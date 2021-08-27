package icesword


import icesword.editor.Editor
import icesword.editor.Elastic
import icesword.editor.Tool
import icesword.editor.closestKnot
import icesword.frp.*
import icesword.geometry.IntVec2
import icesword.scene.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import kotlin.math.roundToInt


fun worldView(
    editor: Editor,
    tileset: Tileset,
    tillDetach: Till,
): HTMLElement {
    val world = editor.world

    val root = createHtmlElement("div").apply {
        tabIndex = 0
        style.width = "100%"
        style.height = "100%"

        addEventListener("contextmenu", { it.preventDefault() })
    }

    root.onMouseDrag(button = 2, till = tillDetach).reactTill(tillDetach) { mouseDrag ->
        val initialXy = mouseDrag.position.sample()
        val delta = mouseDrag.position.map { initialXy - it }

        world.dragCamera(offsetDelta = delta, tillStop = mouseDrag.tillEnd)
    }

    editor.selectedTool.reactTillNext(tillDetach) { tool, tillNext ->
        when (tool) {
            Tool.SELECT -> setupSelectToolController(
                editor = editor,
                root = root,
                tillDetach = tillNext,
            )
            Tool.MOVE -> setupMoveToolController(
                editor = editor,
                root = root,
                tillDetach = tillNext,
            )
            Tool.KNOT_BRUSH -> setupKnotBrushToolController(
                editor = editor,
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
            nodes = DynamicSet.of(
                setOf(
                    TileLayer(
                        tileset = tileset,
                        tiles = world.tiles,
                    ),
                )
            ),
        )

        // TODO: React
        val planeUiLayer = Layer(
            transform = Cell.constant(IntVec2.ZERO),
            nodes = DynamicSet.of(
                setOf(
                    KnotMeshUi(
                        viewTransform = viewTransform,
                        world.knotMesh,
                    ),
                )
            ).unionWith(
                world.elastics.map {
                    ElasticUi(
                        viewTransform = viewTransform,
                        it,
                    )
                },
            ),
        )

        appendChild(
            scene(tillDetach) {
                Scene(
                    layers = listOf(
                        planeLayer,
                        planeUiLayer,
                    ),
                    overlayElements = world.elastics.map {
                        createElasticOverlayElement(
                            elastic = it,
                            viewport = this,
                            viewTransform = viewTransform,
                            tillDetach = tillDetach,
                        )
                    },
                )
            },
        )
    }
}

fun setupSelectToolController(
    editor: Editor,
    root: HTMLElement,
    tillDetach: Till,
) {
    val world = editor.world

    root.onClick().reactTill(tillDetach) {
        val viewportPosition = it.relativePosition(root)
        val worldPosition = world.transformToWorld(viewportPosition)
        editor.selectEntityAt(worldPosition.sample())
    }
}


fun setupMoveToolController(
    editor: Editor,
    root: HTMLElement,
    tillDetach: Till,
) {
    val world = editor.world

    root.onMouseDrag(button = 0, till = tillDetach).reactTill(tillDetach) { mouseDrag ->
        editor.selectedEntity.sample()?.let { selectedEntity ->
            val worldPosition = world.transformToWorld(mouseDrag.position)
            val initialWorldPosition = worldPosition.sample()
            val tileOffsetDelta = worldPosition.map {
                (it - initialWorldPosition).divRound(TILE_SIZE)
            }

            selectedEntity.move(
                tileOffsetDelta = tileOffsetDelta,
                tillStop = mouseDrag.tillEnd,
            )
        }
    }

    root.onKeyDown().reactTill(tillDetach) { event ->
        val selectedElastic = editor.selectedEntity.sample() as? Elastic

        if (selectedElastic != null) {
            when (event.key) {
                "ArrowRight" -> selectedElastic.expandRight()
            }
        }
    }
}

fun setupKnotBrushToolController(
    editor: Editor,
    root: HTMLElement,
    tillDetach: Till,
) {
    val world = editor.world

    root.onMouseDrag(button = 0, till = tillDetach).reactTill(tillDetach) { mouseDrag ->
        world.transformToWorld(mouseDrag.position)
            .reactTill(mouseDrag.tillEnd) { worldPosition ->
                val knotCoord = closestKnot(worldPosition)
                world.knotMesh.putKnot(knotCoord)
            }
    }
}


private fun MouseEvent.relativePosition(origin: HTMLElement): IntVec2 {
    val rect = origin.getBoundingClientRect()
    val originPosition = IntVec2(rect.x.roundToInt(), rect.y.roundToInt())
    return this.clientPosition - originPosition
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

