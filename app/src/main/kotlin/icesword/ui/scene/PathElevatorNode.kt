package icesword.ui.scene

import icesword.editor.modes.EditPathElevatorMode
import icesword.editor.Editor
import icesword.editor.entities.PathElevator
import icesword.editor.entities.PathElevatorEdge
import icesword.editor.entities.PathElevatorStep
import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.DynamicSet
import icesword.frp.Till
import icesword.frp.dynamic_list.map
import icesword.frp.dynamic_list.toSet
import icesword.frp.map
import icesword.frp.unionWith
import icesword.geometry.DynamicTransform
import icesword.geometry.IntLineSeg
import icesword.geometry.IntVec2
import icesword.html.DynamicStyleDeclaration
import icesword.html.createSvgGroup
import icesword.html.createSvgPolygon
import icesword.html.createSvgRect
import icesword.html.createSvgSwitch
import icesword.ui.CanvasNode
import icesword.ui.scene.base.HybridNode
import icesword.ui.setupMoveController
import kotlinx.css.Color
import kotlinx.css.Cursor
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement
import kotlin.math.roundToInt

class PathElevatorNode(
    private val editor: Editor,
    private val pathElevator: PathElevator,
) : GroupNode(
    children = pathElevator.path.steps.map {
        PathElevatorStepNode(
            editor = editor,
            pathElevator = pathElevator,
            step = it,
        )
    },
) {
    override fun buildOverlayElement(
        context: HybridNode.OverlayBuildContext,
    ): SVGElement = context.run {
        val elements = DynamicSet.of(setOf(super.buildOverlayElement(context)))

        val arrows = pathElevator.path.edges.map { edge ->
            createStepArrow(context = context, edge = edge)
        }.toSet()

        createSvgGroup(
            svg = svg,
            children = elements.unionWith(arrows),
            tillDetach = tillDetach,
        )
    }
}


private fun createStepArrow(
    context: HybridNode.OverlayBuildContext,
    edge: PathElevatorEdge,
): SVGElement = context.run {
    val lineSeg = Cell.map2(
        edge.start,
        edge.end,
    ) { startPosition, endPosition ->
        IntLineSeg(
            pointA = startPosition,
            pointB = endPosition,
        ).shorten(44)
    }

    val viewLineSeg = viewTransform.transform(lineSeg)

    createArrow(
        svg = svg,
        lineSeg = viewLineSeg,
        color = edge.isValid.map { if (it) Color.gray else Color.red },
        tillDetach = tillDetach,
    )
}

private fun createArrow(
    svg: SVGSVGElement,
    lineSeg: Cell<IntLineSeg>,
    color: Cell<Color>,
    tillDetach: Till,
): SVGElement {
    fun buildArrowPoints(length: Double): List<IntVec2> {
        val h0 = 8
        val h1 = 4
        val l1 = 12
        val l0 = (length - l1).roundToInt().coerceAtLeast(0)

        val p0 = IntVec2(0, h0)
        val p1 = IntVec2(l0, h0)
        val p2 = IntVec2(l0, h0 + h1)

        // If the given line segment is very short, the arrow may be longer then
        // requested (consisting of just the head)
        val p3 = IntVec2(l0 + l1, 0)

        return listOf(
            p0,
            p1,
            p2,
            p3,
            p2.negY(),
            p1.negY(),
            p0.negY(),
        )
    }

    val translate = DynamicTransform.translate(lineSeg.map { it.pointA })

    val rotate = DynamicTransform.rotateOfDirection(
        direction = lineSeg.map { it.direction },
    )

    val alpha = 0.6

    return createSvgPolygon(
        svg = svg,
        transform = translate * rotate,
        points = lineSeg.map { lineSeg ->
            buildArrowPoints(length = lineSeg.length)
        },
        stroke = color.map { it.darken(30).changeAlpha(alpha) },
        fill = color.map { it.changeAlpha(alpha) },
        tillDetach = tillDetach,
    )
}

class PathElevatorStepNode(
    private val editor: Editor,
    private val pathElevator: PathElevator,
    private val step: PathElevatorStep,
) : HybridNode() {
    override fun buildCanvasNode(
        context: CanvasNodeBuildContext,
    ): CanvasNode = WapSpriteNode(
        editorTextureBank = editor.editorTextureBank,
        textureBank = context.textureBank,
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
                        viewBoundingBox = viewTransform.transform(boundingBox),
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

    val isStepSelected = editMode.selectedStep.map { it == step }

    createSvgRect(
        svg = svg,
        size = viewRect.map { it.size },
        translate = viewRect.map { it.topLeft },
        stroke = isStepSelected.map { if (it) Color.darkBlue else Color.blue },
        strokeWidth = isStepSelected.map { if (it) 2 else 1 },
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
