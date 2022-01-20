package icesword.ui.world_view.scene

import icesword.editor.Editor
import icesword.editor.entities.FloorSpikeRow
import icesword.frp.Till
import icesword.geometry.DynamicTransform
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement

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
        viewBoundingBox = viewTransform.transform(boundingBox),
        tillDetach = tillDetach,
    )
}

