package icesword.scene

import icesword.editor.AxisRange
import icesword.html.DynamicStyleDeclaration
import icesword.html.createSvgGroup
import icesword.html.createSvgRect
import icesword.editor.Editor
import icesword.editor.Elevator
import icesword.editor.HorizontalElevator
import icesword.editor.VerticalElevator
import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.Till
import icesword.frp.map
import icesword.geometry.DynamicTransform
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.geometry.Transform
import icesword.html.createSvgGroupDt
import kotlinx.css.Color
import kotlinx.css.Cursor
import kotlinx.css.PointerEvents
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement
import kotlin.math.PI

fun createHorizontalElevatorOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: Cell<IntVec2>,
    elevator: HorizontalElevator,
    tillDetach: Till,
): SVGElement = createElevatorOverlayElement(
    editor = editor,
    svg = svg,
    viewport = viewport,
    viewTransform = viewTransform,
    elevator = elevator,
    movementRangeOverlayRotation = Transform.identity,
    handleCursor = Cursor.ewResize,
    extractInputCoord = { it.x },
    tillDetach = tillDetach,
)

fun createVerticalElevatorOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: Cell<IntVec2>,
    elevator: VerticalElevator,
    tillDetach: Till,
): SVGElement = createElevatorOverlayElement(
    editor = editor,
    svg = svg,
    viewport = viewport,
    viewTransform = viewTransform,
    elevator = elevator,
    movementRangeOverlayRotation = Transform.rotate(PI / 2),
    handleCursor = Cursor.nsResize,
    extractInputCoord = { it.y },
    tillDetach = tillDetach,
)

private fun createElevatorOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: Cell<IntVec2>,
    elevator: Elevator<*>,
    extractInputCoord: (IntVec2) -> Int,
    movementRangeOverlayRotation: Transform,
    handleCursor: Cursor,
    tillDetach: Till,
): SVGElement {
    val dynamicViewTransform = DynamicTransform(
        transform = viewTransform.map { Transform.translate(it) },
    )

    val boundingBox = elevator.wapSprite.boundingBox

    val entityFrameTranslate =
        dynamicViewTransform.transform(
            point = boundingBox.map { it.position },
        )

    val entityFrame = createEntityFrameElement(
        editor = editor,
        svg = svg,
        outer = viewport,
        entity = elevator,
        translate = entityFrameTranslate,
        size = boundingBox.map { it.size },
        tillDetach = tillDetach,
    )

    val movementRangeOverlay = createMovementRangeOverlay(
        svg = svg,
        viewport = viewport,
        dynamicViewTransform = dynamicViewTransform,
        elevator = elevator,
        rotation = movementRangeOverlayRotation,
        handleCursor = handleCursor,
        extractInputCoord = extractInputCoord,
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

fun createMovementRangeOverlay(
    svg: SVGSVGElement,
    viewport: HTMLElement,
    dynamicViewTransform: DynamicTransform,
    elevator: Elevator<*>,
    rotation: Transform,
    handleCursor: Cursor,
    extractInputCoord: (IntVec2) -> Int,
    tillDetach: Till,
): SVGElement {
    val center = dynamicViewTransform.transform(
        point = elevator.wapSprite.boundingBox.map { it.center },
    )

    // Note: this does not support scaling (zoom)
    val movementRangeRect: Cell<IntRect> = elevator.relativeMovementRange.map {
        val sideLength = 64
        val mr: AxisRange<*> = it

        IntRect.fromDiagonal(
            pointA = IntVec2(mr.min, -(sideLength / 2)),
            pointC = IntVec2(mr.max, +(sideLength / 2)),
        )
    }

    val movementRangeFrame: SVGElement = createSvgRect(
        svg = svg,
        translate = movementRangeRect.map { it.topLeft },
        size = movementRangeRect.map { it.size },
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
        val handleRect = movementRangeRect.map { rangeRect ->
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
        resizeExtremum = elevator::resizeMovementRangeMin,
    )

    val maxHandle = createHandle(
        cornerA = { it.topRight },
        cornerB = { it.bottomRight },
        resizeExtremum = elevator::resizeMovementRangeMax,
    )

    val group = createSvgGroupDt(
        svg = svg,
        transform = DynamicTransform.translate(center) * rotation,
        tillDetach = tillDetach,
    ).apply {
        appendChild(movementRangeFrame)
        appendChild(minHandle)
        appendChild(maxHandle)
    }

    return group
}
