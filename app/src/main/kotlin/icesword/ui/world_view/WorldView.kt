package icesword.ui.world_view


import icesword.RezIndex
import icesword.RezTextureBank
import icesword.TILE_SIZE
import icesword.editor.Editor
import icesword.editor.Tool
import icesword.editor.modes.BasicInsertionMode
import icesword.editor.modes.ElasticInsertionMode
import icesword.editor.modes.EntitySelectMode
import icesword.editor.modes.InsertWapObjectCommand
import icesword.editor.modes.KnotBrushMode
import icesword.editor.modes.KnotPaintMode
import icesword.editor.modes.StampInsertionMode
import icesword.editor.modes.WapObjectAlikeInsertionMode
import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.asStream
import icesword.frp.hold
import icesword.frp.map
import icesword.frp.mapNested
import icesword.frp.reactDynamicNotNullTill
import icesword.frp.reactTill
import icesword.frp.reactTillNext
import icesword.frp.switchMap
import icesword.frp.tillNext
import icesword.frp.units
import icesword.geometry.IntVec2
import icesword.html.KeyPressedState
import icesword.html.MouseButton
import icesword.html.MousePosition
import icesword.html.MousePressedGesture
import icesword.html.buildElement
import icesword.html.calculateRelativePosition
import icesword.html.clientPosition
import icesword.html.createHTMLElementRaw
import icesword.html.onKeyDown
import icesword.html.onMouseDown
import icesword.html.onMouseDrag
import icesword.html.onMouseMove
import icesword.html.onMouseUp
import icesword.html.onWheel
import icesword.html.trackKeyPressedState
import icesword.html.trackMousePosition
import icesword.html.trackMousePressed
import icesword.ui.world_view.scene.createScene
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement


fun worldView(
    rezIndex: RezIndex,
    textureBank: RezTextureBank,
    editor: Editor,
    tillDetach: Till,
): HTMLElement {
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
            val initialClientPosition = mouseDrag.clientPosition.sample()

            val offsetDelta = Cell.map2(
                mouseDrag.clientPosition,
                editor.camera.zoom,
            ) { clientPosition, zoom ->
                val clientPositionDelta = initialClientPosition - clientPosition
                clientPositionDelta.divRound(zoom)
            }

            editor.camera.drag(
                offsetDelta = offsetDelta,
                tillStop = mouseDrag.released,
            )
        }

    editor.editorMode.reactTillNext(tillDetach) { mode, tillNext ->
        when (mode) {
            is EntitySelectMode -> setupEntitySelectModeController(
                editor = editor,
                entitySelectMode = mode,
                root = root,
                tillDetach = tillNext,
            )
            Tool.MOVE -> setupMoveToolController(
                editor = editor,
                root = root,
                tillDetach = tillNext,
            )
            is KnotBrushMode -> setupKnotBrushToolController(
                editor = editor,
                knotBrushMode = mode,
                root = root,
                tillDetach = tillNext,
            )
            is KnotPaintMode -> setupKnotPaintModeController(
                editor = editor,
                knotPaintMode = mode,
                root = root,
                tillDetach = tillNext,
            )
            is StampInsertionMode<*> -> setupStampInsertionModeController(
                editor = editor,
                stampInsertionMode = mode,
                root = root,
                tillDetach = tillNext,
            )
            is ElasticInsertionMode -> setupElasticInsertionModeController(
                editor = editor,
                elasticInsertionMode = mode,
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
            "v" -> editor.enterMoveMode()
            "1" -> {
                val tileCoord = IntVec2(79, 92)
                val tileId = editor.world.tiles.volatileContentView[tileCoord]
                println("Tile ID @ $tileCoord = $tileId")
            }
            "Backspace" -> editor.deleteSelectedEntities()
            "Delete" -> editor.deleteSelectedEntities()
            "-" -> editor.camera.zoomOut(IntVec2.ZERO)
            "=" -> editor.camera.zoomIn(IntVec2.ZERO)
        }
    }

    root.onWheel().reactTill(tillDetach) { ev ->
        val viewportPoint = root.calculateRelativePosition(
            clientPosition = ev.clientPosition,
        )

        if (ev.deltaY > 0.0) {
            editor.camera.zoomIn(
                viewportPoint = viewportPoint,
            )
        } else {
            editor.camera.zoomOut(
                viewportPoint = viewportPoint,
            )
        }
    }

    return root.apply {
        val scene = buildWorldViewScene(
            rezIndex = rezIndex,
            textureBank = textureBank,
            editor = editor,
            viewport = this,
            tillDetach = tillDetach,
        )

        appendChild(
            createScene(
                viewport = this,
                tillDetach = tillDetach,
            ) {
                scene
            }.buildElement(tillDetach),
        )
    }
}

fun setupEntitySelectModeController(
    editor: Editor,
    entitySelectMode: EntitySelectMode,
    root: HTMLElement,
    tillDetach: Till,
) {
    entitySelectMode.closeInputLoop(
        object : EntitySelectMode.Input {
            override val cursorWorldPosition: Cell<IntVec2?> =
                root.trackMousePosition(tillDetach).switchMap { mousePosition: MousePosition ->
                    when (mousePosition) {
                        is MousePosition.Over -> editor.camera.transformToWorld(
                            cameraPoint = mousePosition.relativePosition,
                        )
                        MousePosition.Out -> Cell.constant(null)
                    }
                }

            override val enableAddModifier: Cell<Boolean> =
                root.trackKeyPressedState(key = "Control", tillDetach)
                    .map { it == KeyPressedState.Pressed }

            override val enableSubtractModifier: Cell<Boolean> =
                root.trackKeyPressedState(key = "Alt", tillDetach)
                    .map { it == KeyPressedState.Pressed }
        },
    )

    root.onMouseDrag(
        button = MouseButton.Primary,
        till = tillDetach
    ).reactDynamicNotNullTill(
        entitySelectMode.idleMode.mapNested { idleMode ->
            { mouseDrag ->
                val worldPosition = editor.camera.transformToWorld(
                    cameraPoint = mouseDrag.relativePosition,
                )
                idleMode.select(
                    worldAnchor = worldPosition.sample(),
                    worldTarget = worldPosition,
                    commit = mouseDrag.onReleased,
                )
            }
        },
        till = tillDetach,
    )
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
    knotBrushMode: KnotBrushMode,
    root: HTMLElement,
    tillDetach: Till,
) {
    root.onMouseDrag(button = MouseButton.Primary, till = tillDetach)
        .reactTill(tillDetach) { mouseDrag ->
            val viewportPosition =
                mouseDrag.clientPosition.map(root::calculateRelativePosition)

            val knotCoord: Cell<IntVec2> =
                editor.camera.transformToWorld(viewportPosition)

            knotBrushMode.paintKnots(
                knotCoord = knotCoord,
                till = mouseDrag.released,
            )
        }
}

fun setupKnotPaintModeController(
    editor: Editor,
    knotPaintMode: KnotPaintMode,
    root: HTMLElement,
    tillDetach: Till,
) {
    knotPaintMode.closeInputLoop(
        inputState = root.trackMousePosition(tillDetach).map { mousePosition ->
            when (mousePosition) {
                is MousePosition.Over -> object : KnotPaintMode.BrushOverInputMode {
                    private val viewportPosition =
                        mousePosition.clientPosition.map(root::calculateRelativePosition)

                    override val brushPosition: Cell<IntVec2> =
                        editor.camera.transformToWorld(viewportPosition)
                }
                MousePosition.Out -> KnotPaintMode.BrushOutInputMode
            }
        }
    )

    val onMousePressed = root.trackMousePressed(
        button = MouseButton.Primary,
        till = tillDetach,
    )

    onMousePressed.reactDynamicNotNullTill(
        knotPaintMode.paintReadyMode.mapNested { paintReadyModeNow ->
            fun(mousePressedGesture: MousePressedGesture) {
                paintReadyModeNow.paintKnots(
                    stop = mousePressedGesture.released.asStream(),
                )
            }
        },
        till = tillDetach,
    )
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
            if (mousePosition is MousePosition.Over) {
                insertionMode.place(
                    placementWorldPoint = mousePosition.clientPosition.map {
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

fun setupElasticInsertionModeController(
    editor: Editor,
    elasticInsertionMode: ElasticInsertionMode,
    root: HTMLElement,
    tillDetach: Till,
) {
    elasticInsertionMode.closeInputLoop(
        inputState = root.trackMousePosition(tillDetach).map { mousePosition ->
            when (mousePosition) {
                is MousePosition.Over -> object : ElasticInsertionMode.CursorOverInputMode {
                    override val cursorWorldPosition: Cell<IntVec2> =
                        editor.camera.transformToWorld(mousePosition.relativePosition)
                }
                MousePosition.Out -> ElasticInsertionMode.CursorOutInputMode
            }
        }
    )

    setupBasicInsertionModeController(
        editor = editor,
        insertionMode = elasticInsertionMode,
        root = root,
        tillDetach = tillDetach,
    )
}

fun setupStampInsertionModeController(
    editor: Editor,
    stampInsertionMode: StampInsertionMode<*>,
    root: HTMLElement,
    tillDetach: Till,
) {
    stampInsertionMode.closeInputLoop(
        inputState = root.trackMousePosition(tillDetach).map { mousePosition ->
            when (mousePosition) {
                is MousePosition.Over -> object : StampInsertionMode.StampOverInputMode {
                    override val stampWorldPosition: Cell<IntVec2> =
                        editor.camera.transformToWorld(mousePosition.relativePosition)
                }
                MousePosition.Out -> StampInsertionMode.StampOutInputMode
            }
        }
    )

    setupBasicInsertionModeController(
        editor = editor,
        insertionMode = stampInsertionMode,
        root = root,
        tillDetach = tillDetach,
    )
}

class MouseDrag(
    val clientPosition: Cell<IntVec2>,
    val relativePosition: Cell<IntVec2>,
    val onReleased: Stream<Unit>,
    val released: Till,
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

            val clientPosition = element.onMouseMove().map { it.clientPosition }
                .hold(initialPosition, till = tillEnd)

            val relativePosition = clientPosition.map(element::calculateRelativePosition)

            return MouseDrag(
                clientPosition = clientPosition,
                relativePosition = relativePosition,
                onReleased = onEnd,
                released = tillEnd,
            )
        }
    }
}
