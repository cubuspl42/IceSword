package icesword.scene

import html.DynamicStyleDeclaration
import html.MouseButton
import html.createSvgGroup
import html.createSvgRect
import html.onMouseDrag
import icesword.editor.Editor
import icesword.editor.Elevator
import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.reactTill
import icesword.geometry.DynamicTransform
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.geometry.Transform
import kotlinx.css.Cursor
import kotlinx.css.PointerEvents
import kotlinx.css.style
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement

fun createElevatorOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: Cell<IntVec2>,
    elevator: Elevator,
    tillDetach: Till,
): SVGElement {
    val dynamicViewTransform = DynamicTransform(
        transform = viewTransform.map { Transform(it) },
    )

    val boundingBox = elevator.wapObjectStem.boundingBox

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

    val movementRangeRect = dynamicViewTransform.transform(
        rect = Cell.map2(
            elevator.wapObjectStem.boundingBox.map { it.center },
            elevator.globalMovementRange,
        ) { bc, mr ->
            val sideLength = 64

            IntRect.fromDiagonal(
                pointA = IntVec2(mr.minX, bc.y - (sideLength / 2)),
                pointC = IntVec2(mr.maxX, bc.y + (sideLength / 2)),
            )
        },
    )

    val movementRangeFrame = createSvgRect(
        svg = svg,
        translate = movementRangeRect.map { it.topLeft },
        size = movementRangeRect.map { it.size },
        style = DynamicStyleDeclaration(
            pointerEvents = Cell.constant(PointerEvents.none),
        ),
        tillDetach = tillDetach,
    ).apply {
        setAttributeNS(null, "fill", "green")
        setAttributeNS(null, "fill-opacity", "0.4")
    }

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
            fill = Cell.constant("gray"),
            fillOpacity = Cell.constant(0.8),
            style = DynamicStyleDeclaration(
                cursor = Cell.constant(Cursor.ewResize),
            ),
            size = handleRect.map { it.size },
            tillDetach = tillDetach,
        )

        setupDeltaDragController(
            outer = viewport,
            viewTransform = dynamicViewTransform,
            element = handle,
            tillDetach = tillDetach,
        ) { worldDelta, tillStop ->
            resizeExtremum(
                worldDelta.map { it.x },
                tillStop,
            )
        }

        return handle
    }

    val leftHandle = createHandle(
        cornerA = { it.topLeft },
        cornerB = { it.bottomLeft },
        resizeExtremum = elevator::resizeMovementRangeMin,
    )

    val rightHandle = createHandle(
        cornerA = { it.topRight },
        cornerB = { it.bottomRight },
        resizeExtremum = elevator::resizeMovementRangeMax,
    )

    val group = createSvgGroup(
        svg = svg,
        translate = Cell.constant(IntVec2.ZERO),
        tillDetach = tillDetach,
    ).apply {
        appendChild(entityFrame)
        appendChild(movementRangeFrame)
        appendChild(leftHandle)
        appendChild(rightHandle)
    }

    return group
}

fun setupDeltaDragController(
    outer: HTMLElement,
    viewTransform: DynamicTransform,
    element: Element,
    tillDetach: Till,
    apply: (
        worldDelta: Cell<IntVec2>,
        tillStop: Till,
    ) -> Unit,
) {
    val reverseTransform = viewTransform.reversed

    element.onMouseDrag(
        button = MouseButton.Primary,
        outer = outer,
        till = tillDetach,
    ).reactTill(tillDetach) { mouseDrag ->
        val worldPointerPosition = reverseTransform.transform(
            point = mouseDrag.position,
        )

        val initialWorldPosition = worldPointerPosition.sample()

        val worldDelta = worldPointerPosition.map {
            it - initialWorldPosition
        }

        apply(
            worldDelta,
            mouseDrag.tillEnd,
        )
    }
}
