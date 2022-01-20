package icesword.ui.world_view


import icesword.RezIndex
import icesword.RezTextureBank
import icesword.buildTileset
import icesword.editor.Editor
import icesword.frp.Till
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.concat
import icesword.frp.dynamic_list.concatOf
import icesword.frp.dynamic_list.concatWith
import icesword.frp.dynamic_list.map
import icesword.frp.dynamic_list.mapNotNull
import icesword.frp.dynamic_list.processOf
import icesword.frp.dynamic_list.sortedByDynamic
import icesword.frp.dynamic_list.staticListOf
import icesword.frp.map
import icesword.frp.mapNested
import icesword.frp.switchMapNotNull
import icesword.ui.asHybridNode
import icesword.ui.world_view.scene.Layer
import icesword.ui.world_view.scene.Scene
import icesword.ui.world_view.scene.TileLayer
import icesword.ui.world_view.scene.base.hybridOverlayNode
import icesword.ui.world_view.scene.createAreaSelectionOverlayElement
import icesword.ui.world_view.scene.createBackFoilOverlayElement
import icesword.ui.world_view.scene.createEditorModeModeNode
import icesword.ui.world_view.scene.createEntityNode
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

    val viewTransform = editor.camera.transform

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

    val entityNodes: DynamicList<EntityNode> =
        world.entities.internalOrder
            .concatWith(staticListOf(world.startPointEntity))
            .processOf(tillDetach) {
                createEntityNode(entity = it).build(
                    EntityNodeB.BuildContext(
                        rezIndex = rezIndex,
                        textureBank = textureBank,
                        editorTextureBank = editorTextureBank,
                        editor = editor,
                        viewTransform = viewTransform,
                    ),
                )
            }

    val hybridWapNodes = entityNodes.concatOf { it.wapNodes }
        .sortedByDynamic { it.effectiveZOrder }
        .map { it.canvasNode.asHybridNode() }

    val planeLayer = Layer(
        editorTextureBank = editor.editorTextureBank,
        textureBank = textureBank,
        viewTransform = viewTransform,
        backFoil = backFoil,
        hybridNodes = DynamicList.concat(
            staticListOf(
                TileLayer(
                    tileset = tileset,
                    tiles = editorTilesView,
                ).asHybridNode(),
            ),
            hybridWapNodes,
            entityNodes.mapNotNull { it.overlayNode ?: it.hybridNode },
            DynamicList.ofSingle(
                editor.editorMode.map {
                    createEditorModeModeNode(editorMode = it)
                }
            ),
            DynamicList.ofSingle(
                editor.knotSelectMode.switchMapNotNull {
                    it.selectMode.areaSelectingMode.mapNested { areaSelectingMode ->
                        hybridOverlayNode { svg ->
                            createAreaSelectionOverlayElement(
                                svg = svg,
                                viewTransform = viewTransform,
                                areaSelectingMode = areaSelectingMode,
                                tillDetach = tillDetach,
                            )
                        }
                    }
                }
            ),
        ),
        hybridViewportCanvasNodes = entityNodes.mapNotNull { it.hybridViewportCanvasNode },
        hybridContentOverlayNodes = entityNodes.mapNotNull { it.hybridContentOverlayNode },
        tillDetach = tillDetach,
    )

    return Scene(
        layers = listOf(planeLayer),
    )
}
