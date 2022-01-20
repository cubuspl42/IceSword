package icesword.ui.world_view


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
import icesword.ui.asHybridNode
import icesword.ui.world_view.scene.FloorSpikeRowNode
import icesword.ui.world_view.scene.KnotMeshUi
import icesword.ui.world_view.scene.Layer
import icesword.ui.world_view.scene.Scene
import icesword.ui.world_view.scene.StartPointUi
import icesword.ui.world_view.scene.TileLayer
import icesword.ui.world_view.scene.WapSpriteNode
import icesword.ui.world_view.scene.createAreaSelectionOverlayElement
import icesword.ui.world_view.scene.createBackFoilOverlayElement
import icesword.ui.world_view.scene.createEditorModeModeNode
import icesword.ui.world_view.scene.createEntityNode
import icesword.ui.world_view.scene.createFloorSpikeRowOverlayElement
import icesword.ui.world_view.scene.createHorizontalElevatorOverlayElement
import icesword.ui.world_view.scene.createKnotMeshOverlayElement
import icesword.ui.world_view.scene.createStartPointOverlayElement
import icesword.ui.world_view.scene.createVerticalElevatorOverlayElement
import icesword.ui.world_view.scene.createWapObjectOverlayElement
import icesword.ui.world_view.scene.base.hybridOverlayNode
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
            world.entities.internalOrder.mapNotNull {
                createEntityNode(
                    rezIndex = rezIndex,
                    textureBank = textureBank,
                    editorTextureBank = editorTextureBank,
                    editor = editor,
                    viewTransform = dynamicViewTransform,
                    entity = it
                )
            },
            // hybridNodes / editor mode
            DynamicList.ofSingle(
                editor.editorMode.map {
                    createEditorModeModeNode(editorMode = it)
                }
            ),
            staticListOf(
                hybridOverlayNode { svg ->
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
            // hybridNodes / editor mode
            DynamicList.ofSingle(
                editor.knotSelectMode.switchMapNotNull {
                    it.selectMode.areaSelectingMode.mapNested { areaSelectingMode ->
                        hybridOverlayNode { svg ->
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
            hybridOverlayNode { svg ->
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
