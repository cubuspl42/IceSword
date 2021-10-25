package icesword.scene

import html.createSvgGroup
import html.createSvgRect
import icesword.editor.Editor
import icesword.editor.Elevator
import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import icesword.geometry.DynamicTransform
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.geometry.Transform
import kotlinx.css.Contain
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
    val transform = DynamicTransform(
        transform = viewTransform.map { Transform(it) },
    )

    val boundingBox = elevator.wapObjectStem.boundingBox

    val entityFrameTranslate =
        transform.transform(
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

    val movementRangeRect = transform.transform(
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

    val movementRangeRectElement = createSvgRect(
        svg = svg,
        translate = movementRangeRect.map { it.topLeft },
        size = movementRangeRect.map { it.size },
        tillDetach = tillDetach,
    ).apply {
        setAttributeNS(null, "fill", "green")
        setAttributeNS(null, "fill-opacity", "0.4")
    }

    val group = createSvgGroup(
        svg = svg,
        translate = Cell.constant(IntVec2.ZERO),
        tillDetach = tillDetach,
    ).apply {
        appendChild(entityFrame)
        appendChild(movementRangeRectElement)
    }

    return group
}
