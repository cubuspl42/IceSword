package icesword


import TextureBank
import icesword.editor.BasicInsertionMode
import icesword.editor.Editor
import icesword.editor.InsertWapObjectCommand
import icesword.editor.OffsetTilesView
import icesword.editor.Tool
import icesword.editor.WapObjectAlikeInsertionMode
import icesword.frp.Cell
import icesword.frp.DynamicSet
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.contentDynamicView
import icesword.frp.hold
import icesword.frp.map
import icesword.frp.mapNested
import icesword.frp.mapNotNull
import icesword.frp.mapTillRemoved
import icesword.frp.reactIndefinitely
import icesword.frp.reactTill
import icesword.frp.reactTillNext
import icesword.frp.switchMapNotNull
import icesword.frp.tillNext
import icesword.frp.units
import icesword.frp.values
import icesword.geometry.DynamicTransform
import icesword.geometry.IntVec2
import icesword.html.MouseButton
import icesword.html.MousePosition
import icesword.html.calculateRelativePosition
import icesword.html.clientPosition
import icesword.html.createHTMLElementRaw
import icesword.html.onKeyDown
import icesword.html.onMouseDown
import icesword.html.onMouseDrag
import icesword.html.onMouseMove
import icesword.html.onMouseUp
import icesword.html.trackMousePosition
import icesword.scene.ElasticUi
import icesword.scene.FloorSpikeRowNode
import icesword.scene.KnotMeshUi
import icesword.scene.Layer
import icesword.scene.Scene
import icesword.scene.StartPointUi
import icesword.scene.TileLayer
import icesword.scene.WapSpriteNode
import icesword.scene.createAreaSelectionOverlayElement
import icesword.scene.createBackFoilOverlayElement
import icesword.scene.createEditorModeModeNode
import icesword.scene.createElasticOverlayElement
import icesword.scene.createEntityNode
import icesword.scene.createHorizontalElevatorOverlayElement
import icesword.scene.createFloorSpikeRowOverlayElement
import icesword.scene.createKnotMeshOverlayElement
import icesword.scene.createStartPointOverlayElement
import icesword.scene.createVerticalElevatorOverlayElement
import icesword.scene.createWapObjectOverlayElement
import icesword.scene.scene
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent
import kotlin.math.roundToInt


fun worldView(
    rezIndex: RezIndex,
    textureBank: TextureBank,
    dialogOverlay: DialogOverlay,
    editor: Editor,
    tillDetach: Till,
): HTMLElement {
    val world = editor.world

    val root = createHTMLElementRaw("div").apply {
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
            val initialClientPosition = mouseDrag.position.sample()

            val offsetDelta = Cell.map2(
                mouseDrag.position,
                editor.camera.zoom,
            ) { clientPosition, zoom ->
                val clientPositionDelta = initialClientPosition - clientPosition
                clientPositionDelta.divRound(zoom)
            }

            editor.camera.drag(
                offsetDelta = offsetDelta,
                tillStop = mouseDrag.tillEnd,
            )
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
                editor = editor,
                insertionMode = mode,
                root = root,
                tillDetach = tillNext,
            )
            is WapObjectAlikeInsertionMode -> setupWapObjectAlikeInsertionModeController(
                editor = editor,
                insertionMode = mode,
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
            "Backspace" -> editor.deleteSelectedEntities()
            "Delete" -> editor.deleteSelectedEntities()
        }
    }

    return root.apply {
//        val viewTransform = editor.camera.focusPoint.map { -it }

        val dynamicViewTransform = editor.camera.transform

        val wapObjectPreviewNode =
            editor.wapObjectAlikeInsertionMode.switchMapNotNull { insertionMode ->
                insertionMode.wapObjectPreview.mapNested {
                    WapSpriteNode(
                        textureBank = textureBank,
                        wapSprite = it,
                        alpha = 0.4,
                    )
                }
            }

        val backFoilLayer = Layer(
            textureBank = textureBank,
            viewTransform = DynamicTransform.identity,
            nodes = DynamicSet.empty(),
            buildOverlayElements = {
                DynamicSet.of(
                    setOf(
                        createBackFoilOverlayElement(
                            editor = editor,
                            viewport = this,
                            tillDetach = tillDetach,
                        ),
                    )
                )
            },
            tillDetach = tillDetach,
        )

        val planeLayer = Layer(
            textureBank = textureBank,
            viewTransform = dynamicViewTransform,
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
                        world.wapObjects.mapTillRemoved(tillAbort = tillDetach) { wapObject, _ ->
                            WapSpriteNode(
                                textureBank = textureBank,
                                wapSprite = wapObject.sprite,
                            )
                        },
                        world.horizontalElevators.mapTillRemoved(tillAbort = tillDetach) { elevator, _ ->
                            WapSpriteNode(
                                textureBank = textureBank,
                                wapSprite = elevator.wapSprite,
                            )
                        },
                        world.verticalElevators.mapTillRemoved(tillAbort = tillDetach) { elevator, _ ->
                            WapSpriteNode(
                                textureBank = textureBank,
                                wapSprite = elevator.wapSprite,
                            )
                        },
                        world.floorSpikeRows.mapTillRemoved(tillAbort = tillDetach) { floorSpikeRow, _ ->
                            FloorSpikeRowNode(
                                textureBank = textureBank,
                                floorSpikeRow = floorSpikeRow,
                            )
                        },
                        DynamicSet.ofSingle(
                            element = wapObjectPreviewNode,
                        ),
                    ),
                ),
            ),
            buildOverlayElements = { svg ->
                world.knotMeshes.mapTillRemoved(tillAbort = tillDetach) { knotMesh, tillRemoved ->
                    createKnotMeshOverlayElement(
                        svg = svg,
                        editor = editor,
                        knotMesh = knotMesh,
                        viewport = this,
                        viewTransform = dynamicViewTransform,
                        tillDetach = tillRemoved,
                    )
                }
            },
            hybridNodes = DynamicSet.union2(
                world.entities.mapNotNull {
                    createEntityNode(
                        rezIndex = rezIndex,
                        textureBank = textureBank,
                        editor = editor,
                        entity = it
                    )
                },
                DynamicSet.ofSingle(
                    editor.editorMode.map {
                        createEditorModeModeNode(editorMode = it)
                    }
                )
            ),
//            createEditorModeModeNode
            tillDetach = tillDetach,
        )

        val planeUiLayer = Layer(
            textureBank = textureBank,
            viewTransform = DynamicTransform.identity,
            nodes = DynamicSet.union(
                DynamicSet.of(
                    setOf(
                        DynamicSet.of(
                            setOf(
                                StartPointUi(
                                    viewTransform = dynamicViewTransform,
                                    startPoint = world.startPointEntity,
                                ),
                            )
                        ),
                        world.knotMeshes.mapTillRemoved(tillAbort = tillDetach) { knotMesh, _ ->
                            KnotMeshUi(
                                editor = editor,
                                viewTransform = dynamicViewTransform,
                                knotMesh = knotMesh,
                            )
                        },
                        world.elastics.mapTillRemoved(tillAbort = tillDetach) { elastic, _ ->
                            ElasticUi(
                                editor = editor,
                                viewTransform = dynamicViewTransform,
                                elastic = elastic,
                            )
                        },
                    ),
                ),
            ),
            tillDetach = tillDetach,
        )

        appendChild(
            scene(
                viewport = this,
                tillDetach = tillDetach,
            ) {
                Scene(
                    layers = listOf(
                        backFoilLayer,
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
                                                viewTransform = dynamicViewTransform,
                                                tillDetach = tillDetach,
                                            ),
                                        ),
                                    ),
                                    world.elastics.mapTillRemoved(tillAbort = tillDetach) { elastic, tillRemoved ->
                                        createElasticOverlayElement(
                                            editor = editor,
                                            svg = svg,
                                            elastic = elastic,
                                            viewport = this,
                                            viewTransform = dynamicViewTransform,
                                            tillDetach = tillRemoved,
                                        )
                                    },
                                    world.wapObjects.mapTillRemoved(tillAbort = tillDetach) { wapObject, tillRemoved ->
                                        createWapObjectOverlayElement(
                                            editor = editor,
                                            svg = svg,
                                            viewport = this,
                                            viewTransform = dynamicViewTransform,
                                            wapObject = wapObject,
                                            tillDetach = tillRemoved,
                                        )
                                    },
                                    world.horizontalElevators.mapTillRemoved(tillAbort = tillDetach) { elevator, tillRemoved ->
                                        createHorizontalElevatorOverlayElement(
                                            editor = editor,
                                            svg = svg,
                                            viewport = this,
                                            viewTransform = dynamicViewTransform,
                                            elevator = elevator,
                                            tillDetach = tillRemoved,
                                        )
                                    },
                                    world.verticalElevators.mapTillRemoved(tillAbort = tillDetach) { elevator, tillRemoved ->
                                        createVerticalElevatorOverlayElement(
                                            editor = editor,
                                            svg = svg,
                                            viewport = this,
                                            viewTransform = dynamicViewTransform,
                                            elevator = elevator,
                                            tillDetach = tillRemoved,
                                        )
                                    },
                                    world.floorSpikeRows.mapTillRemoved(tillAbort = tillDetach) { floorSpikeRow, tillRemoved ->
                                        createFloorSpikeRowOverlayElement(
                                            editor = editor,
                                            svg = svg,
                                            viewport = this,
                                            viewTransform = dynamicViewTransform,
                                            floorSpikeRow = floorSpikeRow,
                                            tillDetach = tillRemoved,
                                        )
                                    },
                                    DynamicSet.ofSingle(
                                        editor.entitySelectMode.switchMapNotNull {
                                            it.areaSelectingMode.mapNested { areaSelectingMode ->
                                                createAreaSelectionOverlayElement(
                                                    svg = svg,
                                                    viewTransform = dynamicViewTransform,
                                                    areaSelectingMode = areaSelectingMode,
                                                    tillDetach = tillDetach,
                                                )
                                            }
                                        }
                                    ),
                                    DynamicSet.ofSingle(
                                        editor.knotSelectMode.switchMapNotNull {
                                            it.selectMode.areaSelectingMode.mapNested { areaSelectingMode ->
                                                createAreaSelectionOverlayElement(
                                                    svg = svg,
                                                    viewTransform = dynamicViewTransform,
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
    root.onMouseDrag(button = MouseButton.Primary, till = tillDetach)
        .reactTill(tillDetach) { mouseDrag ->
            val viewportPosition =
                mouseDrag.position.map(root::calculateRelativePosition)

            val knotCoord: Cell<IntVec2> =
                editor.camera.transformToWorld(viewportPosition)

            editor.paintKnots(
                knotCoord = knotCoord,
                till = mouseDrag.tillEnd,
            )
        }
}

fun setupBasicInsertionModeController(
    editor: Editor,
    insertionMode: BasicInsertionMode,
    root: HTMLElement,
    tillDetach: Till,
) {
    root.onMouseDown(button = MouseButton.Primary)
        .reactTill(tillDetach) { event ->
            val viewportPosition =
                root.calculateRelativePosition(event.clientPosition)

            val worldPosition: IntVec2 =
                editor.camera.transformToWorld(cameraPoint = viewportPosition).sample()

            insertionMode.insert(
                insertionWorldPoint = worldPosition,
            )
        }
}

fun setupWapObjectAlikeInsertionModeController(
    editor: Editor,
    insertionMode: WapObjectAlikeInsertionMode,
    root: HTMLElement,
    tillDetach: Till,
) {
    fun calculateWorldPosition(clientPosition: IntVec2): IntVec2 {
        val viewportPosition =
            root.calculateRelativePosition(clientPosition)

        val worldPosition: IntVec2 =
            editor.camera.transformToWorld(cameraPoint = viewportPosition).sample()

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


class MouseDrag(
    val position: Cell<IntVec2>,
    val onEnd: Stream<Unit>,
    val tillEnd: Till,
) {
    companion object {
        fun start(
            element: Element,
            initialPosition: IntVec2,
            button: MouseButton,
            tillAbort: Till,
        ): MouseDrag {
            val onEnd = element.onMouseUp(button = button).units()

            val tillEnd = onEnd.tillNext(tillAbort)

            val position = element.onMouseMove().map { it.clientPosition }
                .hold(initialPosition, till = tillEnd)

            return MouseDrag(
                position = position,
                onEnd = onEnd,
                tillEnd = tillEnd,
            )
        }
    }
}

