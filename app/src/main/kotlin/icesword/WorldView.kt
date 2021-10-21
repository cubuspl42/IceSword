package icesword


import html.*
import icesword.editor.*
import icesword.frp.*
import icesword.geometry.IntVec2
import icesword.scene.*
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent
import kotlin.math.roundToInt


fun worldView(
    editor: Editor,
    tileset: Tileset,
    tillDetach: Till,
): HTMLElement {
    val world = editor.world

    val root = createHtmlElement("div").apply {
        className = "worldView"
        tabIndex = 0

        style.apply {
            display = "grid"
            setProperty("grid-template-columns", "minmax(0, 1fr)")
            setProperty("grid-template-rows", "minmax(0, 1fr)")
        }

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

    root.onKeyDown().reactTill(tillDetach) {
        when (it.key) {
            "s" -> editor.selectTool(Tool.SELECT)
            "v" -> editor.selectTool(Tool.MOVE)
            "1" -> {
                val tileCoord = IntVec2(79, 92)
                val tileId = editor.world.tiles.volatileContentView[tileCoord]
                println("Tile ID @ $tileCoord = $tileId")
            }
        }
    }

    return root.apply {
        val viewTransform = world.cameraFocusPoint.map { -it }

        val planeLayer = Layer(
            transform = viewTransform,
            nodes = DynamicSet.union(
                DynamicSet.of(
                    setOf(
                        DynamicSet.of(
                            setOf(
                                TileLayer(
                                    tileset = tileset,
                                    tiles = world.tiles.contentDynamicView.map {
                                        OffsetTilesView(IntVec2.ZERO, it)
                                    },
                                ),
                            )
                        ),
                        world.ropes.map(
                            tag = "WorldView/planeUiLayer/ropes.map",
                        ) {
                            RopeNode(it)
                        },
                    ),
                ),
            ),
            buildOverlayElements = { svg ->
                world.knotMeshLayer.knotMeshes.map(
                    tag = "WorldView/buildOverlayElements/knotMeshes.map",
                ) { knotMesh ->
                    createKnotMeshOverlayElement(
                        svg = svg,
                        editor = editor,
                        knotMesh = knotMesh,
                        viewport = this,
                        viewTransform = viewTransform,
                        tillDetach = tillDetach,
                    )
                }
            },
        )

        // TODO: React
        val planeUiLayer = Layer(
            transform = Cell.constant(IntVec2.ZERO),
            nodes = DynamicSet.union(
                DynamicSet.of(
                    setOf(
                        DynamicSet.of(
                            setOf(
                                StartPointUi(
                                    viewTransform = viewTransform,
                                    startPoint = world.startPointEntity,
                                ),
                            )
                        ),
                        world.knotMeshLayer.knotMeshes.map(
                            tag = "WorldView/planeUiLayer/knotMeshes.map",
                        ) { knotMesh ->
                            KnotMeshUi(
                                viewTransform = viewTransform,
                                knotMesh,
                            )
                        },
                        world.elastics.map(
                            tag = "WorldView/planeUiLayer/elastics.map",
                        ) {
                            ElasticUi(
                                viewTransform = viewTransform,
                                it,
                            )
                        },
                    ),
                ),
            ),
        )

        appendChild(
            scene(tillDetach) {
                Scene(
                    layers = listOf(
                        planeLayer,
                        planeUiLayer,
                    ),
                    buildOverlayElements = { svg ->
                        DynamicSet.union(
                            DynamicSet.of(
                                setOf(
                                    DynamicSet.of(
                                        setOf(
                                            createStartPointOverlayElement(
                                                editor = editor,
                                                svg = svg,
                                                startPoint = world.startPointEntity,
                                                viewport = this,
                                                viewTransform = viewTransform,
                                                tillDetach = tillDetach,
                                            ),
                                        ),
                                    ),
                                    world.elastics.map(
                                        tag = "WorldView/buildOverlayElements/elastics.map",
                                    ) {
                                        createElasticOverlayElement(
                                            editor = editor,
                                            svg = svg,
                                            elastic = it,
                                            viewport = this,
                                            viewTransform = viewTransform,
                                            tillDetach = tillDetach,
                                        )
                                    },
                                    world.ropes.map(
                                        tag = "WorldView/buildOverlayElements/ropes.map",
                                    ) {
                                        createRopeOverlayElement(
                                            editor = editor,
                                            svg = svg,
                                            viewport = this,
                                            viewTransform = viewTransform,
                                            rope = it,
                                            tillDetach = tillDetach,
                                        )
                                    },
                                ),
                            ),
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
    root.onKeyDown().reactTill(tillDetach) { event ->
        val selectedEntity = editor.selectedEntity.sample()

        if (selectedEntity != null) {
            when (event.key) {
                "ArrowRight" -> selectedEntity.setPosition(
                    selectedEntity.position.sample() + IntVec2(TILE_SIZE, 0)
                )
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

    root.onMouseDrag(button = 0, till = tillDetach)
        .reactTill(tillDetach) { mouseDrag ->
            val viewportPosition =
                mouseDrag.position.map(root::calculateRelativePosition)

            val knotCoord: Cell<IntVec2> =
                world.transformToWorld(viewportPosition)

            editor.paintKnots(
                knotCoord = knotCoord,
                till = mouseDrag.tillEnd,
            )
        }
}


private fun MouseEvent.relativePosition(origin: HTMLElement): IntVec2 {
    val rect = origin.getBoundingClientRect()
    val originPosition = IntVec2(rect.x.roundToInt(), rect.y.roundToInt())
    return this.clientPosition - originPosition
}

private fun HTMLElement.calculateRelativePosition(clientPosition: IntVec2): IntVec2 {
    val rect = getBoundingClientRect()
    val originPosition = IntVec2(rect.x.roundToInt(), rect.y.roundToInt())
    return clientPosition - originPosition
}

class MouseDrag(
    val position: Cell<IntVec2>,
    val tillEnd: Till,
) {
    companion object {
        fun start(
            element: Element,
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

