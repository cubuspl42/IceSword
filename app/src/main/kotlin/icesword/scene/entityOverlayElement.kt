package icesword.scene

import icesword.editor.Editor
import icesword.editor.Elastic
import icesword.editor.Elevator
import icesword.editor.Enemy
import icesword.editor.Entity
import icesword.editor.FloorSpikeRow
import icesword.editor.HorizontalElevator
import icesword.editor.KnotMesh
import icesword.editor.StartPoint
import icesword.editor.TileEntity
import icesword.editor.VerticalElevator
import icesword.editor.WapObject
import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import icesword.geometry.DynamicTransform
import icesword.geometry.IntVec2
import icesword.geometry.Transform
import icesword.scene.createEntityFrameElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement

fun createEntityOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: DynamicTransform,
    entity: Entity,
    tillDetach: Till,
): SVGElement? {
    return when (entity) {
        is Elastic -> null
        is HorizontalElevator -> null
        is VerticalElevator -> null
        is Enemy -> null
        is FloorSpikeRow -> null
        is KnotMesh -> null
        is StartPoint -> null
        is TileEntity -> null
        is WapObject -> null
    }
}

fun createEntityNode(
    editor: Editor,
    entity: Entity,
): HybridNode? {
    return when (entity) {
        is Elastic -> null
        is HorizontalElevator -> null
        is VerticalElevator -> null
        is Enemy -> EnemyNode(
            editor = editor,
            enemy = entity,
        )
        is FloorSpikeRow -> null
        is KnotMesh -> null
        is StartPoint -> null
        is TileEntity -> null
        is WapObject -> null
    }
}
