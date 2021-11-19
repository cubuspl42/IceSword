package icesword.scene

import TextureBank
import icesword.editor.EditPathElevatorMode
import icesword.editor.Editor
import icesword.editor.PathElevator
import icesword.editor.PathElevatorStep
import icesword.frp.Cell.Companion.constant
import icesword.frp.DynamicSet
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.map
import icesword.html.DynamicStyleDeclaration
import icesword.html.createSvgGroup
import icesword.html.createSvgLine
import icesword.html.createSvgRect
import icesword.html.createSvgSwitch
import icesword.ui.setupMoveController
import kotlinx.css.Color
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
) {
    override fun buildOverlayElement(
        context: HybridNode.OverlayBuildContext,
    ): SVGElement = context.run {
        val arrows = pathElevator.path.steps.zipWithNext { originStep, targetStep ->
            createStepArrow(
                context = context,
                originStep = originStep,
                targetStep = targetStep,
            )
        }.toSet()

        createSvgGroup(
            svg = svg,
            children = DynamicSet.of(
                setOf(super.buildOverlayElement(context)) + arrows,
            ),
            tillDetach = tillDetach,
        )
    }
}


private fun createStepArrow(
    context: HybridNode.OverlayBuildContext,
    originStep: PathElevatorStep,
    targetStep: PathElevatorStep,
): SVGElement = context.run {
    createSvgLine(
        pointA = viewTransform.transform(originStep.position),
        pointB = viewTransform.transform(targetStep.position),
        stroke = constant(Color.gray),
        tillDetach = tillDetach,
    )
}

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
                when {
                    editorMode is EditPathElevatorMode && editorMode.pathElevator == pathElevator ->
                        createEditedStepOverlay(
                            context = context,
                            editMode = editorMode,
                            step = step,
                        )
                    else -> createEntityFrameElement(
                        editor = editor,
                        svg = svg,
                        outer = viewport,
                        entity = pathElevator,
                        boundingBox = viewTransform.transform(boundingBox),
                        tillDetach = tillDetach,
                    )
                }
            },
            tillDetach = tillDetach,
        )
    }
}


private fun createEditedStepOverlay(
    context: HybridNode.OverlayBuildContext,
    editMode: EditPathElevatorMode,
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
                val idleMode = editMode.state.sample() as? EditPathElevatorMode.IdleMode

                idleMode?.moveStep(
                    step = step,
                    positionDelta = positionDelta,
                    tillStop = till,
                )
            },
            till = tillDetach,
        )
    }
}
