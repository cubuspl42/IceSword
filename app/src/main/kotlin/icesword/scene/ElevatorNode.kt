package icesword.scene

import icesword.html.DynamicStyleDeclaration
import icesword.html.createSvgGroup
import icesword.html.createSvgRect
import icesword.editor.Editor
import icesword.editor.entities.Elevator
import icesword.editor.entities.EntityMovementRange
import icesword.editor.entities.HorizontalElevator
import icesword.editor.InsertionMode
import icesword.editor.entities.VerticalElevator
import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.Till
import icesword.frp.map
import icesword.geometry.DynamicTransform
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.geometry.Transform
import icesword.html.createSvgGroupDt
import icesword.html.createSvgRectR
import icesword.html.createSvgSwitch
import kotlinx.css.Color
import kotlinx.css.Cursor
import kotlinx.css.PointerEvents
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement
import kotlin.math.PI
import kotlin.math.roundToInt

fun createHorizontalElevatorOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: DynamicTransform,
    elevator: HorizontalElevator,
    tillDetach: Till,
): SVGElement = createElevatorOverlayElement(
    editor = editor,
    svg = svg,
    viewport = viewport,
    viewTransform = viewTransform,
    elevator = elevator,
    movementRangeOverlay = createHorizontalMovementRangeOverlay(
        svg = svg,
        viewport = viewport,
        viewTransform = viewTransform,
        editor = editor,
        entityMovementRange = elevator.movementRange,
        tillDetach = tillDetach,
    ),
    tillDetach = tillDetach,
)

fun createVerticalElevatorOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: DynamicTransform,
    elevator: VerticalElevator,
    tillDetach: Till,
): SVGElement = createElevatorOverlayElement(
    editor = editor,
    svg = svg,
    viewport = viewport,
    viewTransform = viewTransform,
    elevator = elevator,
    movementRangeOverlay = createVerticalMovementRangeOverlay(
        svg = svg,
        viewport = viewport,
        dynamicViewTransform = viewTransform,
        editor = editor,
        entityMovementRange = elevator.movementRange,
        tillDetach = tillDetach,
    ),
    tillDetach = tillDetach,
)

private fun createElevatorOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: DynamicTransform,
    elevator: Elevator<*>,
    movementRangeOverlay: SVGElement,
    tillDetach: Till,
): SVGElement {


    val boundingBox = elevator.wapSprite.boundingBox

    val entityFrame = createEntityFrameElement(
        editor = editor,
        svg = svg,
        outer = viewport,
        entity = elevator,
        boundingBox = viewTransform.transform(boundingBox),
        tillDetach = tillDetach,
    )


    val group = createSvgGroup(
        svg = svg,
        translate = constant(IntVec2.ZERO),
        tillDetach = tillDetach,
    ).apply {
        appendChild(entityFrame)
        appendChild(movementRangeOverlay)
    }

    return group
}

fun createHorizontalMovementRangeOverlay(
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: DynamicTransform,
    editor: Editor,
    entityMovementRange: EntityMovementRange<*>,
    tillDetach: Till,
): SVGElement = createMovementRangeOverlay(
    svg = svg,
    viewport = viewport,
    dynamicViewTransform = viewTransform,
    editor = editor,
    entityMovementRange = entityMovementRange,
    rotation = Transform.identity,
    handleCursor = Cursor.ewResize,
    extractInputCoord = { it.x },
    tillDetach = tillDetach,
)

fun createVerticalMovementRangeOverlay(
    svg: SVGSVGElement,
    viewport: HTMLElement,
    dynamicViewTransform: DynamicTransform,
    editor: Editor,
    entityMovementRange: EntityMovementRange<*>,
    tillDetach: Till,
): SVGElement = createMovementRangeOverlay(
    svg = svg,
    viewport = viewport,
    dynamicViewTransform = dynamicViewTransform,
    editor = editor,
    entityMovementRange = entityMovementRange,
    rotation = Transform.rotateOfAngle(PI / 2),
    handleCursor = Cursor.nsResize,
    extractInputCoord = { it.y },
    tillDetach = tillDetach,
)

private fun createMovementRangeOverlay(
    svg: SVGSVGElement,
    viewport: HTMLElement,
    dynamicViewTransform: DynamicTransform,
    editor: Editor,
    entityMovementRange: EntityMovementRange<*>,
    rotation: Transform,
    handleCursor: Cursor,
    extractInputCoord: (IntVec2) -> Int,
    tillDetach: Till,
): SVGElement = createSvgSwitch(
    editor.editorMode.map { mode ->
        if (mode is InsertionMode) null
        else {
            val viewMovementLine = dynamicViewTransform.transform(entityMovementRange.movementLine)

            val rootTranslate = viewMovementLine.map { it.pointA }

            val movementRangeRect: Cell<IntRect> = viewMovementLine.map { lineSeg ->
                val sideLength = 64

                IntRect.fromDiagonal(
                    pointA = IntVec2(0, -(sideLength / 2)),
                    pointC = IntVec2(lineSeg.length.roundToInt(), +(sideLength / 2)),
                )
            }

            val viewMovementRangeRect = movementRangeRect

            val movementRangeFrame: SVGElement = createSvgRectR(
                svg = svg,
                rect = viewMovementRangeRect,
                fill = constant(Color.gray),
                fillOpacity = constant(0.3),
                style = DynamicStyleDeclaration(
                    pointerEvents = constant(PointerEvents.none),
                ),
                tillDetach = tillDetach,
            )

            fun createHandle(
                cornerA: (IntRect) -> IntVec2,
                cornerB: (IntRect) -> IntVec2,
                resizeExtremum: (extremumDelta: Cell<Int>, tillStop: Till) -> Unit,
            ): SVGElement {
                val handleRect = viewMovementRangeRect.map { rangeRect ->
                    val padding = IntVec2(4, 4)
                    IntRect.fromDiagonal(
                        pointA = cornerA(rangeRect) - padding,
                        pointC = cornerB(rangeRect) + padding,
                    )
                }

                val handle = createSvgRect(
                    svg = svg,
                    translate = handleRect.map { it.topLeft },
                    size = handleRect.map { it.size },
                    fill = constant(Color.gray),
                    fillOpacity = constant(0.8),
                    style = DynamicStyleDeclaration(
                        cursor = constant(handleCursor),
                    ),
                    tillDetach = tillDetach,
                )

                setupDeltaDragController(
                    outer = viewport,
                    viewTransform = dynamicViewTransform,
                    element = handle,
                    tillDetach = tillDetach,
                ) { worldDelta, tillStop ->
                    resizeExtremum(
                        worldDelta.map(extractInputCoord),
                        tillStop,
                    )
                }

                return handle
            }

            val minHandle = createHandle(
                cornerA = { it.topLeft },
                cornerB = { it.bottomLeft },
                resizeExtremum = entityMovementRange::resizeMovementRangeMin,
            )

            val maxHandle = createHandle(
                cornerA = { it.topRight },
                cornerB = { it.bottomRight },
                resizeExtremum = entityMovementRange::resizeMovementRangeMax,
            )

            val group = createSvgGroupDt(
                svg = svg,
                transform = DynamicTransform.translate(rootTranslate) * rotation,
                tillDetach = tillDetach,
            ).apply {
                appendChild(movementRangeFrame)
                appendChild(minHandle)
                appendChild(maxHandle)
            }

            group
        }
    },
    tillDetach = tillDetach,
)
