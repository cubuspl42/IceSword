package icesword.ui.world_view.scene

import icesword.editor.entities.FloorSpikeRow
import icesword.ui.CanvasNode
import icesword.ui.world_view.EntityNode
import icesword.ui.world_view.EntityNodeB
import icesword.ui.world_view.scene.base.HybridNode
import org.w3c.dom.svg.SVGElement

fun createFloorSpikeRowNode(
    floorSpikeRow: FloorSpikeRow,
): EntityNodeB = object : EntityNodeB {
    override fun build(context: EntityNodeB.BuildContext): EntityNode = context.run {
        EntityNode(
            hybridNode = object : HybridNode() {
                override fun buildCanvasNode(context: CanvasNodeBuildContext): CanvasNode =
                    FloorSpikeRowNode(
                        textureBank = textureBank,
                        floorSpikeRow = floorSpikeRow,
                    )

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
