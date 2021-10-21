package icesword.scene

import icesword.editor.Editor
import icesword.editor.CrumblingPeg
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

class CrumblingPegNode(
    private val texture: Texture,
    private val crumblingPeg: CrumblingPeg,
) : Node {
    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        val boundingBox = crumblingPeg.boundingBox.sample()

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
        crumblingPeg.boundingBox.values().units()
}

fun createCrumblingPegOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: Cell<IntVec2>,
    crumblingPeg: CrumblingPeg,
    tillDetach: Till,
): SVGElement {
    val rect = crumblingPeg.boundingBox

    val translate = Cell.map2(
        viewTransform,
        rect.map { it.position },
    ) { vt, ep -> vt + ep }

    val box = createEntityFrameElement(
        editor = editor,
        svg = svg,
        outer = viewport,
        entity = crumblingPeg,
        translate = translate,
        size = rect.map { it.size },
        tillDetach = tillDetach,
    )

    return box
}
