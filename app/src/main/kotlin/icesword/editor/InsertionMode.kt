package icesword.editor

import icesword.RezIndex
import icesword.editor.InsertionPrototype.ElasticInsertionPrototype
import icesword.editor.InsertionPrototype.ElevatorInsertionPrototype
import icesword.editor.InsertionPrototype.FloorSpikeInsertionPrototype
import icesword.editor.InsertionPrototype.KnotMeshInsertionPrototype
import icesword.editor.InsertionPrototype.WapObjectInsertionPrototype
import icesword.editor.WapObjectPrototype.ElevatorPrototype
import icesword.editor.WapObjectPrototype.FloorSpikePrototype
import icesword.frp.Cell
import icesword.frp.CellSlot
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.mapNotNull
import icesword.frp.reactTill
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.tileAtPoint

sealed interface InsertionPrototype {
    value class ElasticInsertionPrototype(
        val elasticPrototype: ElasticPrototype,
    ) : InsertionPrototype

    value class KnotMeshInsertionPrototype(
        val knotPrototype: KnotPrototype,
    ) : InsertionPrototype

    value class WapObjectInsertionPrototype(
        val wapObjectPrototype: WapObjectPrototype,
    ) : InsertionPrototype

    object ElevatorInsertionPrototype : InsertionPrototype

    object FloorSpikeInsertionPrototype : InsertionPrototype
}

sealed interface InsertionMode : EditorMode {
    val insertionPrototype: InsertionPrototype
}

sealed interface BasicInsertionMode : InsertionMode {
    fun insert(insertionWorldPoint: IntVec2)
}

class ElasticInsertionMode(
    private val world: World,
    override val insertionPrototype: ElasticInsertionPrototype,
) : BasicInsertionMode {
    override fun insert(insertionWorldPoint: IntVec2) {
        val elasticPrototype = insertionPrototype.elasticPrototype

        val elastic = Elastic(
            prototype = elasticPrototype,
            initialBounds = IntRect(
                position = tileAtPoint(insertionWorldPoint),
                size = elasticPrototype.defaultSize,
            ),
        )

        world.insertElastic(elastic)
    }
}

class KnotMeshInsertionMode(
    private val world: World,
    override val insertionPrototype: KnotMeshInsertionPrototype,
) : BasicInsertionMode {
    override fun insert(insertionWorldPoint: IntVec2) {
        val knotMesh = KnotMesh.createSquare(
            initialTileOffset = tileAtPoint(insertionWorldPoint),
            knotPrototype = insertionPrototype.knotPrototype,
            initialSideLength = 2,
        )

        world.insertKnotMesh(knotMesh)
    }
}

data class InsertWapObjectCommand(
    val insertionWorldPoint: IntVec2,
)

abstract class WapObjectAlikeInsertionMode(
    private val rezIndex: RezIndex,
    private val wapObjectPrototype: WapObjectPrototype,
) : InsertionMode {
    private val _placementWorldPointSlot = CellSlot<IntVec2>()

    val wapObjectPreview: Cell<WapSprite?> =
        _placementWorldPointSlot.linkedCell.mapNotNull { placementPosition ->
            WapSprite(
                rezIndex = rezIndex,
                imageSetId = wapObjectPrototype.imageSetId,
                position = placementPosition,
            )
        }

    fun place(
        placementWorldPoint: Cell<IntVec2>,
        insert: Stream<InsertWapObjectCommand>,
        till: Till,
    ) {
        _placementWorldPointSlot.link(
            cell = placementWorldPoint,
            till = till,
        )

        insert.reactTill(till = till) {
            this.insert(insertionWorldPoint = it.insertionWorldPoint)
        }
    }

    protected abstract fun insert(insertionWorldPoint: IntVec2)
}

open class WapObjectInsertionMode(
    private val rezIndex: RezIndex,
    private val world: World,
    override val insertionPrototype: WapObjectInsertionPrototype,
) : WapObjectAlikeInsertionMode(
    rezIndex = rezIndex,
    wapObjectPrototype = insertionPrototype.wapObjectPrototype,
) {
    override fun insert(insertionWorldPoint: IntVec2) {
        world.insertWapObject(
            WapObject(
                rezIndex = rezIndex,
                wapObjectPrototype = insertionPrototype.wapObjectPrototype,
                initialPosition = insertionWorldPoint,
            )
        )
    }
}

class ElevatorInsertionMode(
    private val world: World,
    private val rezIndex: RezIndex,
) : WapObjectAlikeInsertionMode(
    rezIndex = rezIndex,
    wapObjectPrototype = ElevatorPrototype,
) {
    override val insertionPrototype = ElevatorInsertionPrototype

    override fun insert(insertionWorldPoint: IntVec2) {
        val rangeRadius = 48

        world.insertElevator(
            elevator = Elevator(
                rezIndex = rezIndex,
                initialPosition = insertionWorldPoint,
                initialRelativeMovementRange = VerticalRange(
                    minX = -rangeRadius,
                    maxX = +rangeRadius,
                )
            )
        )
    }
}


class FloorSpikeInsertionMode(
    private val world: World,
    private val rezIndex: RezIndex,
) : WapObjectAlikeInsertionMode(
    rezIndex = rezIndex,
    wapObjectPrototype = FloorSpikePrototype,
) {
    override val insertionPrototype = FloorSpikeInsertionPrototype

    override fun insert(insertionWorldPoint: IntVec2) {
        world.insertFloorSpikeRow(
            floorSpikeRow = FloorSpikeRow(
                rezIndex = rezIndex,
                initialPosition = insertionWorldPoint,
            )
        )
    }
}
