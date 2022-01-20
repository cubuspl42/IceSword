package icesword.ui.world_view.scene

import icesword.editor.entities.FloorSpikeRow
import icesword.frp.dynamic_list.map
import icesword.ui.world_view.EntityNode
import icesword.ui.world_view.EntityNodeB
import icesword.ui.world_view.WapNode
import icesword.ui.world_view.scene.base.HybridNode
import org.w3c.dom.svg.SVGElement

fun createFloorSpikeRowNode(
    floorSpikeRow: FloorSpikeRow,
): EntityNodeB = object : EntityNodeB {
    override fun build(context: EntityNodeB.BuildContext): EntityNode = context.run {
        EntityNode(
            wapNodes = floorSpikeRow.outputSpikes.map { outputSpike ->
                WapNode.fromWapSprite(
                    editorTextureBank = editorTextureBank,
                    textureBank = textureBank,
                    wapSprite = outputSpike.wapSprite,
                )
            },
            overlayNode = object : HybridNode() {
                override fun buildOverlayElement(context: OverlayBuildContext): SVGElement =
                    context.run {
                        createFloorSpikeRowOverlayElement(
                            editor = editor,
                            svg = svg,
                            viewport = viewport,
                            viewTransform = viewTransform,
                            floorSpikeRow = floorSpikeRow,
                            tillDetach = tillDetach,
                        )
                    }
            },
        )
    }
}
