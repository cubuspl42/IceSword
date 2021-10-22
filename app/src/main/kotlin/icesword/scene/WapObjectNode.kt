package icesword.scene

import TextureBank
import icesword.editor.Editor
import icesword.editor.WapObject
import icesword.editor.WapObjectStem
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

class WapObjectStemNode(
    textureBank: TextureBank,
    private val wapObjectStem: WapObjectStem,
    private val alpha: Double = 1.0,
) : Node {
    private val texture = textureBank.getImageTexture(
        pidImagePath = wapObjectStem.imageMetadata.pidImagePath,
    )

    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        ctx.save()

        val boundingBox = wapObjectStem.boundingBox.sample()

        ctx.lineWidth = 4.0
        ctx.fillStyle = "brown"
        ctx.strokeStyle = "black"

        ctx.globalAlpha = alpha

        drawTexture(
            ctx,
            texture = texture,
            dv = boundingBox.topLeft,
        )

        ctx.restore()
    }

    override val onDirty: Stream<Unit> =
        wapObjectStem.boundingBox.values().units()
}

fun createWapObjectOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: Cell<IntVec2>,
    wapObject: WapObject,
    tillDetach: Till,
): SVGElement {
    val rect = wapObject.stem.boundingBox

    val translate = Cell.map2(
        viewTransform,
        rect.map { it.position },
    ) { vt, ep -> vt + ep }

    val box = createEntityFrameElement(
        editor = editor,
        svg = svg,
        outer = viewport,
        entity = wapObject,
        translate = translate,
        size = rect.map { it.size },
        tillDetach = tillDetach,
    )

    return box
}
