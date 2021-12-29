package icesword.editor

import icesword.ImageSetId
import icesword.RezIndex
import icesword.editor.FloorSpikeRow.FloorSpikeConfig
import icesword.editor.InsertionPrototype.ElasticInsertionPrototype
import icesword.editor.InsertionPrototype.FloorSpikeInsertionPrototype
import icesword.editor.InsertionPrototype.HorizontalElevatorInsertionPrototype
import icesword.editor.InsertionPrototype.KnotMeshInsertionPrototype
import icesword.editor.InsertionPrototype.PathElevatorInsertionPrototype
import icesword.editor.InsertionPrototype.VerticalElevatorInsertionPrototype
import icesword.editor.InsertionPrototype.WapObjectAlikeInsertionPrototype
import icesword.editor.InsertionPrototype.WapObjectInsertionPrototype
import icesword.editor.elastic.prototype.ElasticPrototype
import icesword.editor.retails.Retail
import icesword.editor.wap_object.prototype.WapObjectPrototype
import icesword.editor.wap_object.prototype.WapObjectPrototype.FloorSpikePrototype
import icesword.frp.Cell
import icesword.frp.CellSlot
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.mapNested
import icesword.frp.reactTill
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.tileAtPoint

sealed interface InsertionPrototype {
    data class ElasticInsertionPrototype(
        val elasticPrototype: ElasticPrototype,
        val retail: Retail,
    ) : InsertionPrototype

    value class KnotMeshInsertionPrototype(
        val knotPrototype: KnotPrototype,
    ) : InsertionPrototype

    value class WapObjectInsertionPrototype(
        val wapObjectPrototype: WapObjectPrototype,
    ) : InsertionPrototype {
        companion object {
            val Empty = WapObjectInsertionPrototype(
                wapObjectPrototype = WapObjectPrototype.EmptyPrototype,
            )
        }
    }

    sealed interface WapObjectAlikeInsertionPrototype : InsertionPrototype {
        data class BuildContext(
            val rezIndex: RezIndex,
            val retail: Retail,
            val insertionWorldPoint: IntVec2,
        )

        val imageSetId: ImageSetId

        fun buildInserted(
            context: BuildContext,
        ): Entity
    }

    value class HorizontalElevatorInsertionPrototype(val elevatorPrototype: ElevatorPrototype) : InsertionPrototype

    value class VerticalElevatorInsertionPrototype(val elevatorPrototype: ElevatorPrototype) : InsertionPrototype

    value class PathElevatorInsertionPrototype(val elevatorPrototype: ElevatorPrototype) : InsertionPrototype

    object FloorSpikeInsertionPrototype : InsertionPrototype

    data class RopeInsertionPrototype(
        private val ropePrototype: RopePrototype,
    ) : WapObjectAlikeInsertionPrototype {
        override val imageSetId: ImageSetId = ropePrototype.imageSetId

        override fun buildInserted(
            context: WapObjectAlikeInsertionPrototype.BuildContext,
        ): Entity = Rope(
            rezIndex = context.rezIndex,
            prototype = ropePrototype,
            initialPosition = context.insertionWorldPoint,
            initialSwingDurationMs = 1500,
        )
    }

    data class CrateStackInsertionPrototype(
        private val crateStackPrototype: CrateStackPrototype,
    ) : WapObjectAlikeInsertionPrototype {
        override val imageSetId: ImageSetId = crateStackPrototype.crateImageSetId

        override fun buildInserted(
            context: WapObjectAlikeInsertionPrototype.BuildContext,
        ): Entity = CrateStack(
            rezIndex = context.rezIndex,
            prototype = crateStackPrototype,
            initialPosition = context.insertionWorldPoint,
            initialPickups = listOf(PickupKind.TreasureCoins),
        )
    }

    data class CrumblingPegInsertionPrototype(
        private val crumblingPegPrototype: CrumblingPegPrototype,
    ) : WapObjectAlikeInsertionPrototype {
        override val imageSetId: ImageSetId = crumblingPegPrototype.imageSetId

        override fun buildInserted(
            context: WapObjectAlikeInsertionPrototype.BuildContext,
        ): Entity = CrumblingPeg(
            rezIndex = context.rezIndex,
            retail = context.retail,
            prototype = crumblingPegPrototype,
            initialPosition = context.insertionWorldPoint,
            initialCanRespawn = true,
        )
    }

    value class EnemyInsertionPrototype(
        val wapObjectPrototype: WapObjectPrototype,
    ) : InsertionPrototype
}

sealed interface InsertionMode : EditorMode {
    val insertionPrototype: InsertionPrototype
}

sealed interface BasicInsertionMode : InsertionMode {
    fun insert(insertionWorldPoint: IntVec2)
}

class ElasticInsertionMode(
    private val rezIndex: RezIndex,
    private val retail: Retail,
    private val world: World,
    override val insertionPrototype: ElasticInsertionPrototype,
) : BasicInsertionMode {
    override fun insert(insertionWorldPoint: IntVec2) {
        val elasticPrototype = insertionPrototype.elasticPrototype

        val elastic = Elastic(
            rezIndex = rezIndex,
            retail = retail,
            prototype = elasticPrototype,
            generator = elasticPrototype.buildGenerator(
                retail = insertionPrototype.retail,
            ),
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
    private val imageSetId: ImageSetId,
) : InsertionMode {
    private val _placementWorldPointSlot = CellSlot<IntVec2>()

    val wapObjectPreview: Cell<DynamicWapSprite?> =
        _placementWorldPointSlot.linkedCell.mapNested { placementPosition ->
            DynamicWapSprite.fromImageSet(
                rezIndex = rezIndex,
                imageSetId = imageSetId,
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
    private val retail: Retail,
    private val world: World,
    override val insertionPrototype: WapObjectInsertionPrototype,
) : WapObjectAlikeInsertionMode(
    rezIndex = rezIndex,
    imageSetId = insertionPrototype.wapObjectPrototype.imageSetId,
) {
    override fun insert(insertionWorldPoint: IntVec2) {
        world.insertWapObject(
            WapObject(
                rezIndex = rezIndex,
                retail = retail,
                initialProps = WapObjectPropsData.fromWwdObject(
                    wwdObject = insertionPrototype.wapObjectPrototype.wwdObjectPrototype,
                ),
                initialPosition = insertionWorldPoint,
            )
        )
    }
}

abstract class ElevatorInsertionMode(
    rezIndex: RezIndex,
    private val world: World,
    elevatorPrototype: ElevatorPrototype,
) : WapObjectAlikeInsertionMode(
    rezIndex = rezIndex,
    imageSetId = elevatorPrototype.elevatorImageSetId,
) {
    override fun insert(insertionWorldPoint: IntVec2) {
        val rangeRadius = 48

        world.insertElevator(
            elevator = createElevator(
                insertionWorldPoint = insertionWorldPoint,
                rangeRadius = rangeRadius,
            )
        )
    }

    abstract fun createElevator(
        insertionWorldPoint: IntVec2,
        rangeRadius: Int,
    ): Elevator<*>
}

class HorizontalElevatorInsertionMode(
    private val rezIndex: RezIndex,
    world: World,
    override val insertionPrototype: HorizontalElevatorInsertionPrototype,
) : ElevatorInsertionMode(
    rezIndex = rezIndex,
    world = world,
    elevatorPrototype = insertionPrototype.elevatorPrototype,
) {

    override fun createElevator(
        insertionWorldPoint: IntVec2,
        rangeRadius: Int,
    ): Elevator<*> = HorizontalElevator(
        rezIndex = rezIndex,
        prototype = insertionPrototype.elevatorPrototype,
        initialPosition = insertionWorldPoint,
        initialRelativeMovementRange = HorizontalRange(
            minX = -rangeRadius,
            maxX = +rangeRadius,
        )
    )
}

class PathElevatorInsertionMode(
    private val world: World,
    private val rezIndex: RezIndex,
    override val insertionPrototype: PathElevatorInsertionPrototype,
) : WapObjectAlikeInsertionMode(
    rezIndex = rezIndex,
    imageSetId = insertionPrototype.elevatorPrototype.elevatorImageSetId,
) {
    override fun insert(insertionWorldPoint: IntVec2) {
        world.insertEntity(
            PathElevator(
                rezIndex = rezIndex,
                prototype = insertionPrototype.elevatorPrototype,
                initialPosition = insertionWorldPoint,
                initialStepsConfig = (0 until 8).map {
                    PathElevatorStepData(
                        relativePosition = IntVec2(64 * it, 0),
                    )
                },
            )
        )
    }
}

class VerticalElevatorInsertionMode(
    private val rezIndex: RezIndex,
    world: World,
    override val insertionPrototype: VerticalElevatorInsertionPrototype,
) : ElevatorInsertionMode(
    rezIndex = rezIndex,
    world = world,
    elevatorPrototype = insertionPrototype.elevatorPrototype,
) {
    override fun createElevator(
        insertionWorldPoint: IntVec2,
        rangeRadius: Int,
    ): Elevator<*> = VerticalElevator(
        rezIndex = rezIndex,
        prototype = insertionPrototype.elevatorPrototype,
        initialPosition = insertionWorldPoint,
        initialRelativeMovementRange = VerticalRange(
            minY = -rangeRadius,
            maxY = +rangeRadius,
        )
    )
}

class FloorSpikeInsertionMode(
    private val world: World,
    private val rezIndex: RezIndex,
) : WapObjectAlikeInsertionMode(
    rezIndex = rezIndex,
    imageSetId = FloorSpikePrototype.imageSetId,
) {
    override val insertionPrototype = FloorSpikeInsertionPrototype

    override fun insert(insertionWorldPoint: IntVec2) {
        world.insertFloorSpikeRow(
            floorSpikeRow = FloorSpikeRow(
                rezIndex = rezIndex,
                initialPosition = insertionWorldPoint,
                initialSpikeConfigs = listOf(
                    FloorSpikeConfig(
                        initialStartDelayMillis = 0,
                        initialTimeOffMillis = 1500,
                        initialTimeOnMillis = 1500,
                    ),
                    FloorSpikeConfig(
                        initialStartDelayMillis = 750,
                        initialTimeOffMillis = 1500,
                        initialTimeOnMillis = 1500,
                    ),
                    FloorSpikeConfig(
                        initialStartDelayMillis = 1500,
                        initialTimeOffMillis = 1500,
                        initialTimeOnMillis = 1500,
                    ),
                    FloorSpikeConfig(
                        initialStartDelayMillis = 2250,
                        initialTimeOffMillis = 1500,
                        initialTimeOnMillis = 1500,
                    ),
                )
            )
        )
    }
}

class EnemyInsertionMode(
    private val world: World,
    private val rezIndex: RezIndex,
    override val insertionPrototype: InsertionPrototype.EnemyInsertionPrototype,
) : WapObjectAlikeInsertionMode(
    rezIndex = rezIndex,
    imageSetId = insertionPrototype.wapObjectPrototype.imageSetId,
) {
    override fun insert(insertionWorldPoint: IntVec2) {
        world.insertEntity(
            Enemy(
                rezIndex = rezIndex,
                wapObjectPrototype = insertionPrototype.wapObjectPrototype,
                initialPosition = insertionWorldPoint,
                initialRelativeMovementRange = HorizontalRange(-128, 128),
                initialPickups = emptyList(),
            )
        )
    }
}

class SimpleWapObjectAlikeInsertionMode(
    private val rezIndex: RezIndex,
    private val retail: Retail,
    private val world: World,
    override val insertionPrototype: WapObjectAlikeInsertionPrototype,
) : WapObjectAlikeInsertionMode(
    rezIndex = rezIndex,
    imageSetId = insertionPrototype.imageSetId,
) {
    override fun insert(insertionWorldPoint: IntVec2) {
        val entity = insertionPrototype.buildInserted(
            context = WapObjectAlikeInsertionPrototype.BuildContext(
                rezIndex = rezIndex,
                retail = retail,
                insertionWorldPoint = insertionWorldPoint,
            ),
        )

        world.insertEntity(entity)
    }
}
