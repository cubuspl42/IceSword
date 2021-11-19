package icesword.scene

import TextureBank
import icesword.editor.Editor
import icesword.editor.EntitySelectMode
import icesword.editor.PathElevator
import icesword.editor.PathElevatorStep
import icesword.frp.Cell.Companion.constant
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.map
import icesword.html.DynamicStyleDeclaration
import icesword.html.createSvgRect
import icesword.html.createSvgSwitch
import icesword.ui.setupMoveController
import kotlinx.css.Cursor
import org.w3c.dom.svg.SVGElement

class PathElevatorNode(
    private val editor: Editor,
    private val pathElevator: PathElevator,
) : GroupNode(
    children = DynamicList.of(
        pathElevator.path.steps.map {
            PathElevatorStepNode(
                editor = editor,
                pathElevator = pathElevator,
                step = it,
            )
        },
    )
)

class PathElevatorStepNode(
    private val editor: Editor,
    private val pathElevator: PathElevator,
    private val step: PathElevatorStep,
) : HybridNode {
    override fun buildCanvasNode(
        textureBank: TextureBank,
    ): CanvasNode = WapSpriteNode(
        textureBank = textureBank,
        wapSprite = step.wapSprite,
    )

    override fun buildOverlayElement(
        context: HybridNode.OverlayBuildContext,
    ): SVGElement = context.run {
        val boundingBox = step.wapSprite.boundingBox

        createSvgSwitch(
            child = editor.editorMode.map { editorMode ->
                when (editorMode) {
                    is EntitySelectMode -> createEntityFrameElement(
                        editor = editor,
                        svg = svg,
                        outer = viewport,
                        entity = pathElevator,
                        boundingBox = viewTransform.transform(boundingBox),
                        tillDetach = tillDetach,
                    )
                    else -> createEditedStepOverlay(
                        context = context,
                        step = step,
                    )
                }
            },
            tillDetach = tillDetach,
        )
    }
}

private fun createEditedStepOverlay(
    context: HybridNode.OverlayBuildContext,
    step: PathElevatorStep,
): SVGElement = context.run {
    val viewRect = viewTransform.transform(step.wapSprite.boundingBox)

    createSvgRect(
        svg = svg,
        size = viewRect.map { it.size },
        translate = viewRect.map { it.topLeft },
        stroke = constant("blue"),
        style = DynamicStyleDeclaration(
            cursor = constant(Cursor.move),
        ),
        tillDetach = tillDetach,
    ).apply {
        setAttributeNS(null, "fill-opacity", "0")

        setupMoveController(
            viewTransform = viewTransform,
            outer = viewport,
            element = this,
            move = { positionDelta, till ->
                step.move(
                    positionDelta = positionDelta,
                    tillStop = till,
                )
            },
            till = tillDetach,
        )
    }
}
