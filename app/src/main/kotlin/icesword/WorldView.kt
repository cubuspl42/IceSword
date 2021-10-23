package icesword


import TextureBank
import html.MouseButton
import html.MousePosition
import html.clientPosition
import html.createHtmlElement
import html.onClick
import html.onKeyDown
import html.onMouseDown
import html.onMouseDrag
import html.onMouseMove
import html.onMouseUp
import html.trackMousePosition
import icesword.editor.BasicInsertionMode
import icesword.editor.Editor
import icesword.editor.InsertWapObjectCommand
import icesword.editor.OffsetTilesView
import icesword.editor.SelectMode
import icesword.editor.Tool
import icesword.editor.WapObjectInsertionMode
import icesword.editor.World
import icesword.frp.Cell
import icesword.frp.DynamicSet
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.contentDynamicView
import icesword.frp.hold
import icesword.frp.map
import icesword.frp.mapNotNull
import icesword.frp.reactTill
import icesword.frp.reactTillNext
import icesword.frp.switchMapNotNull
import icesword.frp.tillNext
import icesword.frp.units
import icesword.geometry.IntVec2
import icesword.scene.ElasticUi
import icesword.scene.KnotMeshUi
import icesword.scene.Layer
import icesword.scene.Scene
import icesword.scene.StartPointUi
import icesword.scene.TileLayer
import icesword.scene.WapObjectStemNode
import icesword.scene.createAreaSelectionOverlayElement
import icesword.scene.createElasticOverlayElement
import icesword.scene.createKnotMeshOverlayElement
import icesword.scene.createStartPointOverlayElement
import icesword.scene.createWapObjectOverlayElement
import icesword.scene.scene
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent
import kotlin.math.roundToInt


fun worldView(
    editor: Editor,
    textureBank: TextureBank,
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

    root.onMouseDrag(button = MouseButton.Secondary, till = tillDetach)
        .reactTill(tillDetach) { mouseDrag ->
            val initialXy = mouseDrag.position.sample()
            val delta = mouseDrag.position.map { initialXy - it }

            world.dragCamera(offsetDelta = delta, tillStop = mouseDrag.tillEnd)
        }

    editor.editorMode.reactTillNext(tillDetach) { mode, tillNext ->
        when (mode) {
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
            is BasicInsertionMode -> setupBasicInsertionModeController(
                world = editor.world,
                insertionMode = mode,
                root = root,
                tillDetach = tillNext,
            )
            is WapObjectInsertionMode -> setupWapObjectInsertionModeController(
                world = editor.world,
                insertionMode = mode,
                root = root,
                tillDetach = tillNext,
            )
            is SelectMode -> setupSelectModeController(
                editor = editor,
                selectMode = mode,
                root = root,
                tillDetach = tillNext,
            )
        }
    }

    root.onKeyDown().reactTill(tillDetach) {
        when (it.key) {
            "s" -> editor.enterSelectMode()
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

        val wapObjectPreviewNode =
            editor.wapObjectInsertionMode.switchMapNotNull { insertionMode ->
                insertionMode.wapObjectPreview.mapNotNull {
                    WapObjectStemNode(
                        textureBank = textureBank,
                        wapObjectStem = it,
                        alpha = 0.4,
                    )
                }
            }

        val planeLayer = Layer(
            transform = viewTransform,
            nodes = DynamicSet.union(
                DynamicSet.of(
                    setOf(
                        DynamicSet.of(
                            setOf(
                                TileLayer(
                                    tileset = textureBank.tileset,
                                    tiles = world.tiles.contentDynamicView.map {
                                        OffsetTilesView(IntVec2.ZERO, it)
                                    },
                                ),
                            )
                        ),
                        world.wapObjects.map(
                            tag = "WorldView/planeUiLayer/ropes.map",
                        ) {
                            WapObjectStemNode(
                                textureBank = textureBank,
                                wapObjectStem = it.stem,
                            )
                        },
                        DynamicSet.ofSingle(
                            element = wapObjectPreviewNode,
                        ),
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
                                    world.wapObjects.map(
                                        tag = "WorldView/buildOverlayElements/ropes.map",
                                    ) {
                                        createWapObjectOverlayElement(
                                            editor = editor,
                                            svg = svg,
                                            viewport = this,
                                            viewTransform = viewTransform,
                                            wapObject = it,
                                            tillDetach = tillDetach,
                                        )
                                    },
                                    DynamicSet.ofSingle(
                                        editor.selectionMode.switchMapNotNull {
                                            it.areaSelectingMode.mapNotNull { areaSelectingMode ->
                                                createAreaSelectionOverlayElement(
                                                    svg = svg,
                                                    viewTransform = viewTransform,
                                                    areaSelectingMode = areaSelectingMode,
                                                    tillDetach = tillDetach,
                                                )
                                            }
                                        }
                                    ),
                                ),
                            ),
                        )
                    },
                )
            },
        )
    }
}

fun setupSelectModeController(
    editor: Editor,
    selectMode: SelectMode,
    root: HTMLElement,
    tillDetach: Till,
) {
    val world = editor.world

    fun calculateWorldPosition(clientPosition: IntVec2): IntVec2 {
        val viewportPosition =
            root.calculateRelativePosition(clientPosition)

        val worldPosition: IntVec2 =
            world.transformToWorld(cameraPoint = viewportPosition).sample()

        return worldPosition
    }

    val button = MouseButton.Primary

    root.onMouseDrag(button = button, till = tillDetach)
        .reactTill(tillDetach) { mouseDrag ->
            (selectMode.state.sample() as? SelectMode.IdleMode)?.selectArea(
                anchorWorldCoord = calculateWorldPosition(
                    clientPosition = mouseDrag.position.sample()
                ),
                targetWorldCoord = mouseDrag.position.map(::calculateWorldPosition),
                confirm = root.onMouseUp(button = button).units(),
                abort = Stream.never(), // FIXME?
            )
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

    root.onMouseDrag(button = MouseButton.Primary, till = tillDetach)
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

fun setupBasicInsertionModeController(
    world: World,
    insertionMode: BasicInsertionMode,
    root: HTMLElement,
    tillDetach: Till,
) {
    root.onMouseDown(button = MouseButton.Primary)
        .reactTill(tillDetach) { event ->
            val viewportPosition =
                root.calculateRelativePosition(event.clientPosition)

            val worldPosition: IntVec2 =
                world.transformToWorld(cameraPoint = viewportPosition).sample()

            insertionMode.insert(
                insertionWorldPoint = worldPosition,
            )
        }
}

fun setupWapObjectInsertionModeController(
    world: World,
    insertionMode: WapObjectInsertionMode,
    root: HTMLElement,
    tillDetach: Till,
) {
    fun calculateWorldPosition(clientPosition: IntVec2): IntVec2 {
        val viewportPosition =
            root.calculateRelativePosition(clientPosition)

        val worldPosition: IntVec2 =
            world.transformToWorld(cameraPoint = viewportPosition).sample()

        return worldPosition
    }

    root.trackMousePosition(till = tillDetach)
        .reactTillNext(tillAbort = tillDetach) { mousePosition: MousePosition, tillNext: Till ->
            if (mousePosition is MousePosition.Entered) {
                insertionMode.place(
                    placementWorldPoint = mousePosition.position.map {
                        calculateWorldPosition(it)
                    },
                    insert = root.onMouseDown(button = MouseButton.Primary).map { event ->
                        InsertWapObjectCommand(
                            insertionWorldPoint = calculateWorldPosition(
                                clientPosition = event.clientPosition,
                            ),
                        )
                    },
                    till = tillNext,
                )
            }
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
            button: MouseButton,
            tillAbort: Till,
        ): MouseDrag {
            val tillEnd = element.onMouseUp(button = button)
                .tillNext(tillAbort)

            val position = element.onMouseMove().map { it.clientPosition }
                .hold(initialPosition, till = tillEnd)

            return MouseDrag(
                position = position,
                tillEnd = tillEnd,
            )
        }
    }
}

