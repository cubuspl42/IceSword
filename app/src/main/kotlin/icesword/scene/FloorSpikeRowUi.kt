package icesword.scene

import TextureBank
import icesword.editor.Editor
import icesword.editor.FloorSpikeRow
import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.units
import icesword.frp.values
import icesword.geometry.DynamicTransform
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.geometry.Transform
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement

class FloorSpikeRowNode(
    textureBank: TextureBank,
    private val floorSpikeRow: FloorSpikeRow,
) : Node {
    private val spikeTexture = textureBank.getImageTexture(
        imageMetadata = floorSpikeRow.spikeImageMetadata,
    )!!

    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        ctx.save()

        floorSpikeRow.spikes.sample().forEach {
            drawWapSprite(
                ctx,
                texture = spikeTexture,
                bounds = it.bounds,
            )
        }

        ctx.restore()
    }

    override val onDirty: Stream<Unit> =
        floorSpikeRow.spikes.values().units()
}

fun createFloorSpikeRowOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: Cell<IntVec2>,
    floorSpikeRow: FloorSpikeRow,
    tillDetach: Till,
): SVGElement {
    val dynamicViewTransform = DynamicTransform(
        transform = viewTransform.map { Transform(it) },
    )

    val boundingBox = floorSpikeRow.boundingBox

    val entityFrameTranslate =
        dynamicViewTransform.transform(
            point = boundingBox.map { it.position },
        )

    return createEntityFrameElement(
        editor = editor,
        svg = svg,
        outer = viewport,
        entity = floorSpikeRow,
        translate = entityFrameTranslate,
        size = boundingBox.map { it.size },
        tillDetach = tillDetach,
    )
}
