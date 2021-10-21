package icesword.scene

import icesword.editor.Editor
import icesword.editor.Rope
import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.units
import icesword.frp.values
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement

class RopeNode(
    private val texture: Texture,
    private val rope: Rope,
) : Node {
    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        val boundingBox = rope.boundingBox.sample()

        ctx.lineWidth = 4.0
        ctx.fillStyle = "brown"
        ctx.strokeStyle = "black"

        drawTexture(
            ctx,
            texture = texture,
            dv = boundingBox.topLeft,
        )
    }

    override val onDirty: Stream<Unit> =
        rope.boundingBox.values().units()
}

fun createRopeOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: Cell<IntVec2>,
    rope: Rope,
    tillDetach: Till,
): SVGElement {
    val rect = rope.boundingBox

    val translate = Cell.map2(
        viewTransform,
        rect.map { it.position },
    ) { vt, ep -> vt + ep }

    val box = createEntityFrameElement(
        editor = editor,
        svg = svg,
        outer = viewport,
        entity = rope,
        translate = translate,
        size = rect.map { it.size },
        tillDetach = tillDetach,
    )

    return box
}
