package icesword.scene

import icesword.editor.Editor
import icesword.editor.StartPoint
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.mergeWith
import icesword.frp.units
import icesword.frp.values
import icesword.geometry.DynamicTransform
import icesword.geometry.IntRect
import icesword.html.DynamicStyleDeclaration
import icesword.html.createSvgCircle
import icesword.ui.EntityMoveDragController
import kotlinx.css.Color
import kotlinx.css.Cursor
import kotlinx.css.PointerEvents
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement


class StartPointUi(
    private val viewTransform: DynamicTransform,
    private val startPoint: StartPoint,
) : CanvasNode {
    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        ctx.save()

        val viewTransform = this.viewTransform.transform.sample()

        val position = startPoint.position.sample()

        ctx.setTransformT(viewTransform)

        ctx.fillStyle = "darkgray"
        ctx.strokeStyle = "black"
        ctx.lineWidth = 4.0

        drawCircle(
            ctx = ctx,
            center = position,
            radius = 32.0,
        )

        ctx.fillStyle = "gray"


        drawEquilateralTriangle(
            ctx = ctx,
            center = position,
            sideLength = 42,
        )

        ctx.restore()
    }

    override val onDirty: Stream<Unit> =
        viewTransform.transform.values().units()
            .mergeWith(startPoint.position.values().units())
}


fun createStartPointOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: DynamicTransform,
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

    val rootTranslate = DynamicTransform.translate(startPoint.position)

    val isSelected = editor.isEntitySelected(startPoint)

    val circle = createSvgCircle(
        svg = svg,
        radius = 32.0f,
        transform = viewTransform * rootTranslate,
        stroke = isSelected.map { if (it) Color.red else Color.gray },
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
