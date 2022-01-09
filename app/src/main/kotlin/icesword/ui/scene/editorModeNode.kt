package icesword.ui.scene

import icesword.editor.modes.EditPathElevatorMode
import icesword.editor.EditorMode
import icesword.editor.modes.EntitySelectMode
import icesword.editor.modes.KnotPaintMode
import icesword.editor.entities.knotRect
import icesword.editor.modes.ElasticInsertionMode
import icesword.editor.modes.FixtureInsertionMode
import icesword.editor.modes.WapObjectAlikeInsertionMode
import icesword.frp.Cell.Companion.constant
import icesword.frp.map
import icesword.frp.mapNested
import icesword.frp.switchMapNested
import icesword.html.DynamicStyleDeclaration
import icesword.html.createSvgRectR
import icesword.html.createSvgSwitch
import icesword.ui.asHybridNode
import kotlinx.css.Color
import kotlinx.css.PointerEvents
import org.w3c.dom.svg.SVGElement

fun createEditorModeModeNode(
    editorMode: EditorMode,
): HybridNode? = when (editorMode) {
    is EditPathElevatorMode -> EditPathElevatorModeNode(editorMode)
    is KnotPaintMode -> KnotPaintModeNode(editorMode)
    is EntitySelectMode -> EntitySelectModeNode(entitySelectMode = editorMode)
    // TODO: Display the preview WAP object in the same Z axis as existing WAP objects
    is WapObjectAlikeInsertionMode -> buildElasticInsertionModeNode(insertionMode = editorMode)
    is ElasticInsertionMode -> buildElasticInsertionModeNode(elasticInsertionMode = editorMode)
    is FixtureInsertionMode -> buildFixtureInsertionModeNode(fixtureInsertionMode = editorMode)
    else -> null
}

class EditPathElevatorModeNode(
    private val editMode: EditPathElevatorMode,
) : HybridNode() {
    override fun buildOverlayElement(context: OverlayBuildContext): SVGElement = context.run {
        createSvgSwitch(
            child = editMode.state.map { editModeState ->
                if (editModeState is EditPathElevatorMode.MoveStepMode) null
                else null
            },
            tillDetach = tillDetach,
        )
    }
}

class KnotPaintModeNode(
    private val knotPaintMode: KnotPaintMode,
) : HybridNode() {
    override fun buildOverlayElement(context: OverlayBuildContext): SVGElement = context.run {
        val brushElement = knotPaintMode.brushOverMode.mapNested { brushOverModeNow ->
            val brushViewRect = viewTransform.transform(
                rect = brushOverModeNow.brushCursor.map {
                    knotRect(globalKnotCoord = it.knotCoord)
                },
            )

            createSvgRectR(
                svg = svg,
                style = DynamicStyleDeclaration(
                    pointerEvents = constant(PointerEvents.none),
                ),
                rect = brushViewRect,
                fillOpacity = constant(0.0),
                stroke = constant(Color.black),
                strokeWidth = constant(2),
                tillDetach = tillDetach,
            )
        }

        createSvgSwitch(
            child = brushElement,
            tillDetach = tillDetach,
        )
    }
}

class EntitySelectModeNode(
    private val entitySelectMode: EntitySelectMode,
) : HybridNode() {
    override fun buildOverlayElement(context: OverlayBuildContext): SVGElement = context.run {
        val selectionAreaRect = entitySelectMode.selectingMode
            .switchMapNested { selectingModeNow ->
                selectingModeNow.selectionForm.map { it as? EntitySelectMode.AreaSelection }
            }
            .mapNested { areaSelection ->
                createAreaSelectionRectElement(
                    svg = svg,
                    viewTransform = viewTransform,
                    worldArea = constant(areaSelection.worldArea),
                    tillDetach = tillDetach,
                )
            }

        createSvgSwitch(
            child = selectionAreaRect,
            tillDetach = tillDetach,
        )
    }
}

fun buildElasticInsertionModeNode(
    insertionMode: WapObjectAlikeInsertionMode,
) = HybridNode.ofSingle(
    insertionMode.wapObjectPreview.mapNested {
        hybridCanvasNode { context ->
            WapSpriteNode(
                editorTextureBank = context.editorTextureBank,
                textureBank = context.textureBank,
                wapSprite = it,
                alpha = EntityStyle.previewAlpha,
            )
        }
    },
)

fun buildElasticInsertionModeNode(
    elasticInsertionMode: ElasticInsertionMode,
) = HybridNode.ofSingle(
    elasticInsertionMode.elasticPreview.mapNested {
        ElasticProductNode(
            elasticProduct = it.elasticProduct,
            alpha = EntityStyle.previewAlpha,
        )
    },
)

fun buildFixtureInsertionModeNode(
    fixtureInsertionMode: FixtureInsertionMode,
) = HybridNode.ofSingle(
    fixtureInsertionMode.preview.mapNested {
        FixtureProductNode(
            fixtureProduct = it.fixtureProduct,
            alpha = EntityStyle.previewAlpha,
        )
    },
)
