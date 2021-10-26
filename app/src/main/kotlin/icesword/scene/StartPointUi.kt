package icesword.scene

import icesword.html.DynamicStyleDeclaration
import icesword.html.createSvgCircle
import icesword.editor.Editor
import icesword.editor.StartPoint
import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.mergeWith
import icesword.frp.units
import icesword.frp.values
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.ui.EntityMoveDragController
import kotlinx.css.Cursor
import kotlinx.css.PointerEvents
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement


class StartPointUi(
    private val viewTransform: Cell<IntVec2>,
    private val startPoint: StartPoint,
) : Node {
    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        val viewTransform = this.viewTransform.sample()

        val position = startPoint.position.sample()
        val viewPosition = position + viewTransform

        ctx.fillStyle = "darkgray"
        ctx.strokeStyle = "black"
        ctx.lineWidth = 4.0

        drawCircle(
            ctx = ctx,
            center = viewPosition,
            radius = 32.0,
        )

        ctx.fillStyle = "gray"


        drawEquilateralTriangle(
            ctx = ctx,
            center = viewPosition,
            sideLength = 42,
        )
    }

    override val onDirty: Stream<Unit> =
        viewTransform.values().units()
            .mergeWith(startPoint.position.values().units())
}


fun createStartPointOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: Cell<IntVec2>,
    startPoint: StartPoint,
    tillDetach: Till,
): SVGElement {
    val moveController = EntityMoveDragController.create(
        editor = editor,
        entity = startPoint,
    )

    val circleCursor = moveController.map {
        it?.let { Cursor.move }
    }

    val rootTranslate = Cell.map2(
        viewTransform,
        startPoint.position,
    ) { vt, ep -> vt + ep }

    val isSelected = editor.isEntitySelected(startPoint)

    val circle = createSvgCircle(
        svg = svg,
        radius = 32.0f,
        translate = rootTranslate,
        stroke = isSelected.map { if (it) "red" else "gray" },
        style = DynamicStyleDeclaration(
            cursor = circleCursor,
            pointerEvents = moveController.map {
                if (it != null) PointerEvents.auto else PointerEvents.none
            },
        ),
        tillDetach = tillDetach,
    ).apply {

        setAttributeNS(null, "fill-opacity", "0")
        setAttributeNS(null, "stroke-width", "8px")
    }

    EntityMoveDragController.linkDragHandler(
        element = circle,
        outer = viewport,
        controller = moveController,
        till = tillDetach,
    )

    return circle
}
