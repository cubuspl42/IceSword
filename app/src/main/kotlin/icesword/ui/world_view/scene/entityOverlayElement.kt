package icesword.ui.world_view.scene

import icesword.RezIndex
import icesword.RezTextureBank
import icesword.editor.Editor
import icesword.editor.entities.CrateStack
import icesword.editor.entities.CrumblingPeg
import icesword.editor.entities.Elastic
import icesword.editor.entities.Enemy
import icesword.editor.entities.Entity
import icesword.editor.entities.Fixture
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
import icesword.ui.world_view.buildHybridNode
import icesword.ui.world_view.scene.base.HybridNode

fun createEntityNode(
    rezIndex: RezIndex,
    textureBank: RezTextureBank,
    editor: Editor,
    viewTransform: DynamicTransform,
    entity: Entity,
): HybridNode? = when (entity) {
    is Elastic -> createElasticNode(
        elastic = entity,
    )
    is HorizontalElevator -> null
    is VerticalElevator -> null
    is Enemy -> createEnemyNode(
        enemy = entity,
    )
    is FloorSpikeRow -> null
    is KnotMesh -> null
    is StartPoint -> null
    is WapObject -> null
    is PathElevator -> createPathElevatorNode(
        pathElevator = entity,
    )
    is Rope -> createRopeNode(
        rope = entity,
    )
    is CrateStack -> createCrateStackNode(
        crateStack = entity,
    )
    is CrumblingPeg -> createCrumblingPegNode(
        crumblingPeg = entity,
    )
    is TogglePeg -> createTogglePegNode(
        togglePeg = entity,
    )
    is Warp -> createWarpNode(
        warp = entity,
    )
    is Fixture -> createFixtureNode(
        fixture = entity,
    )
}?.buildHybridNode(
    rezIndex = rezIndex,
    textureBank = textureBank,
    editor = editor,
    viewTransform = viewTransform,
)
