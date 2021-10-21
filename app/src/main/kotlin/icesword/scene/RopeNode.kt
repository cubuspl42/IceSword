package icesword.scene

import html.DynamicStyleDeclaration
import html.createSvgCircle
import icesword.editor.Editor
import icesword.editor.Rope
import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.units
import icesword.frp.values
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement

class RopeNode(
    private val rope: Rope,
) : Node {
    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        val position = rope.position.sample()

        ctx.lineWidth = 4.0
        ctx.fillStyle = "brown"
        ctx.strokeStyle = "black"

        drawCircle(
            ctx,
            center = position,
            radius = Rope.radius.toDouble(),
        )
    }

    override val onDirty: Stream<Unit> =
        rope.position.values().units()
}

fun createRopeOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: Cell<IntVec2>,
    rope: Rope,
    tillDetach: Till,
): SVGElement {
    return createDraggableOverlayElement(
        editor = editor,
        entity = rope,
        outer = viewport,
        till = tillDetach,
    ) { cursor ->
        val rootTranslate = Cell.map2(
            viewTransform,
            rope.position,
        ) { vt, ep -> vt + ep }

        val circle = createSvgCircle(
            svg = svg,
            radius = Rope.radius.toFloat(),
            translate = rootTranslate,
            style = DynamicStyleDeclaration(
                cursor = cursor,
            ),
            tillDetach = tillDetach,
        ).apply {
            setAttributeNS(null, "fill-opacity", "0.3")
        }

        circle
    }
}
