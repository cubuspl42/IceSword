package icesword.ui.world_view.scene

import icesword.editor.entities.KnotMesh
import icesword.ui.asHybridNode
import icesword.ui.world_view.EntityNode
import icesword.ui.world_view.EntityNodeB
import icesword.ui.world_view.scene.base.HybridNode
import org.w3c.dom.svg.SVGElement

fun createKnotMeshNode(
    knotMesh: KnotMesh,
): EntityNodeB = object : EntityNodeB {
    override fun build(context: EntityNodeB.BuildContext): EntityNode = context.run {
        EntityNode(
            hybridViewportCanvasNode = KnotMeshUi(
                editor = editor,
                viewTransform = viewTransform,
                knotMesh = knotMesh,
            ).asHybridNode(),
            hybridContentOverlayNode = object : HybridNode() {
                override fun buildOverlayElement(context: OverlayBuildContext): SVGElement =
                    context.run {
                        createKnotMeshOverlayElement(
                            svg = svg,
                            editor = editor,
                            knotMesh = knotMesh,
                            viewport = viewport,
                            viewTransform = viewTransform,
                            tillDetach = tillDetach,
                        )
                    }
            },
        )
    }
}
