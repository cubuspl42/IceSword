package icesword.scene

import icesword.RezTextureBank
import icesword.editor.Editor
import icesword.editor.FloorSpikeRow
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.units
import icesword.frp.values
import icesword.geometry.DynamicTransform
import icesword.geometry.IntRect
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement

class FloorSpikeRowNode(
    textureBank: RezTextureBank,
    private val floorSpikeRow: FloorSpikeRow,
) : CanvasNode {
    private val spikeTexture = textureBank.getImageTexture(
        floorSpikeRow.spikeImageMetadata.pidPath,
    )!!

    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        ctx.save()

        val outputRow = floorSpikeRow.outputRow.sample()

        outputRow.spikes.forEach {
            drawWapSprite(
                ctx,
                texture = spikeTexture,
                bounds = it.bounds,
            )
        }

        ctx.restore()
    }

    override val onDirty: Stream<Unit> =
        floorSpikeRow.outputRow.values().units()
}

fun createFloorSpikeRowOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: DynamicTransform,
    floorSpikeRow: FloorSpikeRow,
    tillDetach: Till,
): SVGElement {
    val boundingBox = floorSpikeRow.boundingBox

    return createEntityFrameElement(
        editor = editor,
        svg = svg,
        outer = viewport,
        entity = floorSpikeRow,
        boundingBox = viewTransform.transform(boundingBox),
        tillDetach = tillDetach,
    )
}

