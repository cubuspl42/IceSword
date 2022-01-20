package icesword.ui.world_view.scene

import icesword.editor.entities.StartPoint
import icesword.ui.asHybridNode
import icesword.ui.world_view.EntityNode
import icesword.ui.world_view.EntityNodeB
import icesword.ui.world_view.scene.base.HybridNode
import org.w3c.dom.svg.SVGElement

fun createStartPointNode(
    startPoint: StartPoint,
): EntityNodeB = object : EntityNodeB {
    override fun build(context: EntityNodeB.BuildContext): EntityNode = context.run {
        EntityNode(
            overlayNode = object : HybridNode() {
                override fun buildOverlayElement(context: OverlayBuildContext): SVGElement =
                    context.run {
                        createStartPointOverlayElement(
                            editor = editor,
                            svg = svg,
                            startPoint = startPoint,
                            viewport = viewport,
                            viewTransform = viewTransform,
                            tillDetach = tillDetach,
                        )
                    }
            },
            hybridViewportCanvasNode = StartPointUi(
                viewTransform = viewTransform,
                startPoint = startPoint,
            ).asHybridNode(),
        )
    }
}
