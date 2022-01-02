package icesword.ui.scene

import icesword.RezTextureBank
import icesword.RezIndex
import icesword.editor.entities.CrateStack
import icesword.editor.entities.CrumblingPeg
import icesword.editor.Editor
import icesword.editor.entities.Elastic
import icesword.editor.entities.Enemy
import icesword.editor.entities.Entity
import icesword.editor.entities.FloorSpikeRow
import icesword.editor.entities.HorizontalElevator
import icesword.editor.entities.KnotMesh
import icesword.editor.entities.PathElevator
import icesword.editor.entities.Rope
import icesword.editor.entities.StartPoint
import icesword.editor.entities.TogglePeg
import icesword.editor.entities.VerticalElevator
import icesword.editor.entities.WapObject
import icesword.editor.entities.Warp
import icesword.geometry.DynamicTransform

fun createEntityNode(
    rezIndex: RezIndex,
    textureBank: RezTextureBank,
    editor: Editor,
    viewTransform: DynamicTransform,
    entity: Entity,
): HybridNode? = when (entity) {
    is Elastic -> ElasticNode(
        editor = editor,
        elastic = entity,
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
    is CrumblingPeg -> CrumblingPegNode(
        editor = editor,
        crumblingPeg = entity,
    )
    is TogglePeg -> TogglePegNode(
        editor = editor,
        togglePeg = entity,
    )
    is Warp -> EntityWapSpriteNode(
        editor = editor,
        entity = entity,
        wapSprite = entity.wapSprite,
    )
}
