package icesword.editor.modes

import icesword.ImageSetId
import icesword.RezIndex
import icesword.editor.DynamicWapSprite
import icesword.editor.MetaTileLayerProduct
import icesword.editor.World
import icesword.editor.entities.Elastic
import icesword.editor.entities.ElasticProduct
import icesword.editor.entities.Elevator
import icesword.editor.entities.ElevatorPrototype
import icesword.editor.entities.Enemy
import icesword.editor.entities.Entity
import icesword.editor.entities.FloorSpikeRow
import icesword.editor.entities.FloorSpikeRow.FloorSpikeConfig
import icesword.editor.entities.HorizontalElevator
import icesword.editor.entities.HorizontalRange
import icesword.editor.entities.KnotMesh
import icesword.editor.entities.PathElevator
import icesword.editor.entities.PathElevatorStepData
import icesword.editor.entities.VerticalElevator
import icesword.editor.entities.VerticalRange
import icesword.editor.entities.WapObject
import icesword.editor.entities.WapObjectPropsData
import icesword.editor.entities.wap_object.prototype.WapObjectPrototype.FloorSpikePrototype
import icesword.editor.modes.InsertionPrototype.ElasticInsertionPrototype
import icesword.editor.modes.InsertionPrototype.FloorSpikeInsertionPrototype
import icesword.editor.modes.InsertionPrototype.HorizontalElevatorInsertionPrototype
import icesword.editor.modes.InsertionPrototype.KnotMeshInsertionPrototype
import icesword.editor.modes.InsertionPrototype.PathElevatorInsertionPrototype
import icesword.editor.modes.InsertionPrototype.VerticalElevatorInsertionPrototype
import icesword.editor.modes.InsertionPrototype.WapObjectAlikeInsertionPrototype
import icesword.editor.modes.InsertionPrototype.WapObjectInsertionPrototype
import icesword.editor.retails.Retail
import icesword.frp.Cell
import icesword.frp.CellLoop
import icesword.frp.CellSlot
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.mapNested
import icesword.frp.reactTill
import icesword.frp.staticSetOf
import icesword.frp.unionWith
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.tileAtPoint

abstract class StampInsertionMode<Preview>(
    private val world: World,
) : BasicInsertionMode {
    sealed interface InputState

    interface StampOverInputMode : InputState {
        val stampWorldPosition: Cell<IntVec2>
    }

    object StampOutInputMode : InputState

    private val inputStateLoop: CellLoop<InputState> =
        CellLoop(placeholderValue = StampOutInputMode)

    private val inputState: Cell<InputState> = inputStateLoop.asCell

    fun closeInputLoop(inputState: Cell<InputState>) {
        inputStateLoop.close(inputState)
    }

    val preview: Cell<Preview?> = inputState.map { inputStateNow ->
        when (inputStateNow) {
            is StampOverInputMode -> {
                buildPreview(inputMode = inputStateNow)
            }
            StampOutInputMode -> null
        }
    }

    abstract fun buildPreview(inputMode: StampOverInputMode): Preview

    final override fun insert(insertionWorldPoint: IntVec2) {
        val entity = buildEntity(
            insertionWorldPoint = insertionWorldPoint,
        )

        world.insertEntity(entity)
    }

    abstract fun buildEntity(insertionWorldPoint: IntVec2): Entity
}
