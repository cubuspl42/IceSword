package icesword.scene

import TextureBank
import icesword.editor.EditPathElevatorMode
import icesword.editor.Editor
import icesword.editor.PathElevator
import icesword.editor.PathElevatorEdge
import icesword.editor.PathElevatorStep
import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.DynamicSet
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.map
import icesword.geometry.DynamicTransform
import icesword.geometry.IntLineSeg
import icesword.geometry.IntVec2
import icesword.html.DynamicStyleDeclaration
import icesword.html.createSvgGroup
import icesword.html.createSvgPolygon
import icesword.html.createSvgRect
import icesword.html.createSvgSwitch
import icesword.ui.setupMoveController
import kotlinx.css.Color
import kotlinx.css.Cursor
import org.w3c.dom.svg.SVGElement
import kotlin.math.roundToInt

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
        val arrows = pathElevator.path.edges.map { edge ->
            createStepArrow(context = context, edge = edge)
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
    edge: PathElevatorEdge,
): SVGElement = context.run {
    createArrow(
        context = context,
        lineSeg = Cell.map2(
            edge.start,
            edge.end,
        ) { startPosition, endPosition ->
            IntLineSeg(
                pointA = startPosition,
                pointB = endPosition,
            ).shorten(44)
        },
        color = edge.isValid.map { if (it) Color.gray else Color.red },
    )
}

private fun createArrow(
    context: HybridNode.OverlayBuildContext,
    lineSeg: Cell<IntLineSeg>,
    color: Cell<Color>,
): SVGElement = context.run {
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

    val translate = DynamicTransform.translate(
        t = viewTransform.transform(lineSeg.map { it.pointA }),
    )

    val rotate = DynamicTransform.rotateOfDirection(
        direction = lineSeg.map { it.direction },
    )

    val alpha = 0.6

    createSvgPolygon(
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
