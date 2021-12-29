package icesword.scene

import icesword.RezTextureBank
import icesword.RezIndex
import icesword.editor.CrateStack
import icesword.editor.Editor
import icesword.editor.Elastic
import icesword.editor.Enemy
import icesword.editor.Entity
import icesword.editor.FloorSpikeRow
import icesword.editor.HorizontalElevator
import icesword.editor.KnotMesh
import icesword.editor.PathElevator
import icesword.editor.Rope
import icesword.editor.StartPoint
import icesword.editor.TileEntity
import icesword.editor.VerticalElevator
import icesword.editor.WapObject
import icesword.geometry.DynamicTransform

fun createEntityNode(
    rezIndex: RezIndex,
    textureBank: RezTextureBank,
    editor: Editor,
    viewTransform: DynamicTransform,
    entity: Entity,
): HybridNode? = when (entity) {
    is Elastic -> ElasticNode(
        rezIndex = rezIndex,
        editor = editor,
        elastic = entity,
        viewTransform = viewTransform,
    )
    is HorizontalElevator -> null
    is VerticalElevator -> null
    is Enemy -> EnemyNode(
        rezIndex = rezIndex,
        textureBank = textureBank,
        editor = editor,
        enemy = entity,
    )
    is FloorSpikeRow -> null
    is KnotMesh -> null
    is StartPoint -> null
    is TileEntity -> null
    is WapObject -> null
    is PathElevator -> PathElevatorNode(
        editor = editor,
        pathElevator = entity,
    )
    is Rope -> RopeNode(
        editor = editor,
        rope = entity,
    )
    is CrateStack -> CrateStackNode(
        editor = editor,
        crateStack = entity,
    )
}
