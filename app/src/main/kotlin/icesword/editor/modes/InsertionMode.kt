package icesword.editor.modes

import icesword.ImageSetId
import icesword.RezIndex
import icesword.editor.DynamicWapSprite
import icesword.editor.EditorMode
import icesword.editor.MetaTileLayerProduct
import icesword.editor.PickupKind
import icesword.editor.World
import icesword.editor.entities.CrateStack
import icesword.editor.entities.CrateStackPrototype
import icesword.editor.entities.CrumblingPeg
import icesword.editor.entities.CrumblingPegPrototype
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
import icesword.editor.entities.KnotPrototype
import icesword.editor.entities.PathElevator
import icesword.editor.entities.PathElevatorStepData
import icesword.editor.entities.Rope
import icesword.editor.entities.RopePrototype
import icesword.editor.entities.TogglePeg
import icesword.editor.entities.TogglePegPrototype
import icesword.editor.entities.VerticalElevator
import icesword.editor.entities.VerticalRange
import icesword.editor.entities.WapObject
import icesword.editor.entities.WapObjectPropsData
import icesword.editor.entities.Warp
import icesword.editor.entities.WarpPrototype
import icesword.editor.entities.ZOrderedEntity
import icesword.editor.entities.elastic.prototype.ElasticPrototype
import icesword.editor.entities.fixture.prototypes.FixturePrototype
import icesword.editor.entities.wap_object.prototype.WapObjectPrototype
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

    data class FixtureInsertionPrototype(
        val fixturePrototype: FixturePrototype,
    ) : InsertionPrototype

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
            initialZOrder = ZOrderedEntity.defaultZOrder,
            initialCanRespawn = true,
        )
    }

    data class TogglePegInsertionPrototype(
        private val togglePegPrototype: TogglePegPrototype,
    ) : WapObjectAlikeInsertionPrototype {
        override val imageSetId: ImageSetId = togglePegPrototype.imageSetId

        override fun buildInserted(
            context: WapObjectAlikeInsertionPrototype.BuildContext,
        ): Entity = TogglePeg(
            rezIndex = context.rezIndex,
            retail = context.retail,
            prototype = togglePegPrototype,
            initialPosition = context.insertionWorldPoint,
            initialTimeOnMs = 1500,
            initialTimeOffMs = 1500,
            initialDelayMs = 0,
        )
    }

    data class WarpInsertionPrototype(
        private val warpPrototype: WarpPrototype,
    ) : WapObjectAlikeInsertionPrototype {
        override val imageSetId: ImageSetId = warpPrototype.imageSetId

        override fun buildInserted(
            context: WapObjectAlikeInsertionPrototype.BuildContext,
        ): Entity = Warp(
            rezIndex = context.rezIndex,
            prototype = warpPrototype,
            initialPosition = context.insertionWorldPoint,
            initialTargetPosition = context.insertionWorldPoint,
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

    sealed interface InputState

    interface CursorOverInputMode : InputState {
        val cursorWorldPosition: Cell<IntVec2>
    }

    object CursorOutInputMode : InputState

    inner class ElasticPreview(
        val elasticProduct: ElasticProduct,
    ) {
        private val metaTileClusters = world.metaTileLayer.metaTileClusters.unionWith(
            staticSetOf(elasticProduct.metaTileCluster),
        )

        val metaTileLayerProduct = MetaTileLayerProduct(
            tileGenerator = retail.tileGenerator,
            metaTileClusters = metaTileClusters,
            globalTileCoords = elasticProduct.metaTileCluster.globalTileCoords,
        )
    }

    private val elasticPrototype = insertionPrototype.elasticPrototype

    private val inputStateLoop: CellLoop<InputState> =
        CellLoop(placeholderValue = CursorOutInputMode)

    private val inputState: Cell<InputState> = inputStateLoop.asCell

    fun closeInputLoop(inputState: Cell<InputState>) {
        inputStateLoop.close(inputState)
    }

    val elasticPreview: Cell<ElasticPreview?> = inputState.map { inputStateNow ->
        when (inputStateNow) {
            is CursorOverInputMode -> {
                val generator = elasticPrototype.buildGenerator(
                    retail = retail,
                )

                val elasticProduct = ElasticProduct(
                    rezIndex = rezIndex,
                    retail = retail,
                    generator = generator,
                    tileBounds = inputStateNow.cursorWorldPosition.map {
                        val cursorTilePosition = tileAtPoint(it)
                        IntRect(
                            position = cursorTilePosition,
                            size = elasticPrototype.defaultSize,
                        )
                    }
                )

                ElasticPreview(
                    elasticProduct = elasticProduct,
                )
            }
            CursorOutInputMode -> null
        }
    }


    override fun insert(insertionWorldPoint: IntVec2) {
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
