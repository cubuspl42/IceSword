package icesword.ui


import icesword.RezIndex
import icesword.RezTextureBank
import icesword.buildTileset
import icesword.editor.Editor
import icesword.frp.Till
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.mapNotNull
import icesword.frp.dynamic_list.mapTillRemoved
import icesword.frp.dynamic_list.staticListOf
import icesword.frp.map
import icesword.frp.mapNested
import icesword.frp.switchMapNotNull
import icesword.ui.scene.EntityStyle
import icesword.ui.scene.FloorSpikeRowNode
import icesword.ui.scene.KnotMeshUi
import icesword.ui.scene.Layer
import icesword.ui.scene.Scene
import icesword.ui.scene.StartPointUi
import icesword.ui.scene.TileLayer
import icesword.ui.scene.WapSpriteNode
import icesword.ui.scene.createAreaSelectionOverlayElement
import icesword.ui.scene.createBackFoilOverlayElement
import icesword.ui.scene.createEditorModeModeNode
import icesword.ui.scene.createEntityNode
import icesword.ui.scene.createFloorSpikeRowOverlayElement
import icesword.ui.scene.createHorizontalElevatorOverlayElement
import icesword.ui.scene.createKnotMeshOverlayElement
import icesword.ui.scene.createStartPointOverlayElement
import icesword.ui.scene.createVerticalElevatorOverlayElement
import icesword.ui.scene.createWapObjectOverlayElement
import icesword.ui.scene.overlayNode
import org.w3c.dom.HTMLElement


fun buildWorldViewScene(
    rezIndex: RezIndex,
    textureBank: RezTextureBank,
    editor: Editor,
    viewport: HTMLElement,
    tillDetach: Till,
): Scene {
    val editorTextureBank = editor.editorTextureBank

    val world = editor.world

    val dynamicViewTransform = editor.camera.transform

    val wapObjectPreviewNode =
        editor.wapObjectAlikeInsertionMode.switchMapNotNull { insertionMode ->
            insertionMode.wapObjectPreview.mapNested {
                WapSpriteNode(
                    editorTextureBank = editorTextureBank,
                    textureBank = textureBank,
                    wapSprite = it,
                    alpha = EntityStyle.previewAlpha,
                ).asHybridNode()
            }
        }

    val backFoil = createBackFoilOverlayElement(
        editor = editor,
        viewport = viewport,
        tillDetach = tillDetach,
    )

    val tileset = textureBank.buildTileset(
        rezIndex = editor.rezIndex,
        retail = editor.retail,
    )

    val editorTilesView = editor.buildEditorTilesView()

    val planeLayer = Layer(
        editorTextureBank = editor.editorTextureBank,
        textureBank = textureBank,
        viewTransform = dynamicViewTransform,
        backFoil = backFoil,
        hybridNodes = DynamicList.concat(
            staticListOf(
                TileLayer(
                    tileset = tileset,
                    tiles = editorTilesView,
                ).asHybridNode(),
            ),
            world.wapObjects.internalOrder.mapTillRemoved(tillAbort = tillDetach) { wapObject, _ ->
                WapSpriteNode(
                    editorTextureBank = editorTextureBank,
                    textureBank = textureBank,
                    wapSprite = wapObject.sprite,
                ).asHybridNode()
            },
            world.horizontalElevators.internalOrder.mapTillRemoved(tillAbort = tillDetach) { elevator, _ ->
                WapSpriteNode(
                    editorTextureBank = editorTextureBank,
                    textureBank = textureBank,
                    wapSprite = elevator.wapSprite,
                ).asHybridNode()
            },
            world.verticalElevators.internalOrder.mapTillRemoved(tillAbort = tillDetach) { elevator, _ ->
                WapSpriteNode(
                    editorTextureBank = editorTextureBank,
                    textureBank = textureBank,
                    wapSprite = elevator.wapSprite,
                ).asHybridNode()
            },
            world.floorSpikeRows.internalOrder.mapTillRemoved(tillAbort = tillDetach) { floorSpikeRow, _ ->
                FloorSpikeRowNode(
                    textureBank = textureBank,
                    floorSpikeRow = floorSpikeRow,
                ).asHybridNode()
            },
            DynamicList.ofSingle(
                element = wapObjectPreviewNode,
            ),
            world.entities.internalOrder.mapNotNull {
                createEntityNode(
                    rezIndex = rezIndex,
                    textureBank = textureBank,
                    editor = editor,
                    viewTransform = dynamicViewTransform,
                    entity = it
                )
            },
            DynamicList.ofSingle(
                editor.editorMode.map {
                    createEditorModeModeNode(editorMode = it)
                }
            ),
            staticListOf(
                overlayNode { svg ->
                    createStartPointOverlayElement(
                        editor = editor,
                        svg = svg,
                        startPoint = world.startPointEntity,
                        viewport = viewport,
                        viewTransform = dynamicViewTransform,
                        tillDetach = tillDetach,
                    )
                }
            ),
            world.wapObjects.internalOrder.mapTillRemoved(tillAbort = tillDetach) { wapObject, tillRemoved ->
                overlayNode { svg ->
                    createWapObjectOverlayElement(
                        editor = editor,
                        svg = svg,
                        viewport = viewport,
                        viewTransform = dynamicViewTransform,
                        wapObject = wapObject,
                        tillDetach = tillRemoved,
                    )
                }
            },
            world.horizontalElevators.internalOrder.mapTillRemoved(tillAbort = tillDetach) { elevator, tillRemoved ->
                overlayNode { svg ->
                    createHorizontalElevatorOverlayElement(
                        editor = editor,
                        svg = svg,
                        viewport = viewport,
                        viewTransform = dynamicViewTransform,
                        elevator = elevator,
                        tillDetach = tillRemoved,
                    )
                }
            },
            world.verticalElevators.internalOrder.mapTillRemoved(tillAbort = tillDetach) { elevator, tillRemoved ->
                overlayNode { svg ->
                    createVerticalElevatorOverlayElement(
                        editor = editor,
                        svg = svg,
                        viewport = viewport,
                        viewTransform = dynamicViewTransform,
                        elevator = elevator,
                        tillDetach = tillRemoved,
                    )
                }
            },
            world.floorSpikeRows.internalOrder.mapTillRemoved(tillAbort = tillDetach) { floorSpikeRow, tillRemoved ->
                overlayNode { svg ->
                    createFloorSpikeRowOverlayElement(
                        editor = editor,
                        svg = svg,
                        viewport = viewport,
                        viewTransform = dynamicViewTransform,
                        floorSpikeRow = floorSpikeRow,
                        tillDetach = tillRemoved,
                    )
                }
            },
            DynamicList.ofSingle(
                editor.knotSelectMode.switchMapNotNull {
                    it.selectMode.areaSelectingMode.mapNested { areaSelectingMode ->
                        overlayNode { svg ->
                            createAreaSelectionOverlayElement(
                                svg = svg,
                                viewTransform = dynamicViewTransform,
                                areaSelectingMode = areaSelectingMode,
                                tillDetach = tillDetach,
                            )
                        }
                    }
                }
            ),
        ),
        hybridViewportCanvasNodes = DynamicList.concat(
            staticListOf(
                StartPointUi(
                    viewTransform = dynamicViewTransform,
                    startPoint = world.startPointEntity,
                ).asHybridNode(),
            ),
            world.knotMeshes.internalOrder.mapTillRemoved(tillAbort = tillDetach) { knotMesh, _ ->
                KnotMeshUi(
                    editor = editor,
                    viewTransform = dynamicViewTransform,
                    knotMesh = knotMesh,
                ).asHybridNode()
            },
        ),
        hybridContentOverlayNodes = world.knotMeshes.internalOrder.mapTillRemoved(tillAbort = tillDetach) { knotMesh, tillRemoved ->
            overlayNode { svg ->
                createKnotMeshOverlayElement(
                    svg = svg,
                    editor = editor,
                    knotMesh = knotMesh,
                    viewport = viewport,
                    viewTransform = dynamicViewTransform,
                    tillDetach = tillRemoved,
                )
            }
        },
        tillDetach = tillDetach,
    )

    return Scene(
        layers = listOf(planeLayer),
    )
}
