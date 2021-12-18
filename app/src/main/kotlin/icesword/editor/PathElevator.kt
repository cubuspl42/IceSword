@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.ImageMetadata
import icesword.RezIndex
import icesword.editor.retails.Retail
import icesword.editor.wap_object.prototype.WapObjectPrototype
import icesword.frp.Cell
import icesword.frp.Loop.Companion.looped
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.MutableDynamicList
import icesword.frp.dynamic_list.first
import icesword.frp.dynamic_list.getOrNull
import icesword.frp.dynamic_list.indexOf
import icesword.frp.dynamic_list.last
import icesword.frp.dynamic_list.withAppended
import icesword.frp.dynamic_list.zipWithNext
import icesword.frp.map
import icesword.frp.mapNested
import icesword.frp.reactTill
import icesword.frp.switch
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.Geometry
import icesword.wwd.Wwd
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.math.absoluteValue

enum class PathElevatorDirection {
    DownLeft,
    Down,
    DownRight,
    Left,
    None,
    Right,
    UpLeft,
    Up,
    UpRight;

    val code: Int
        get() = ordinal + 1
}

data class PathElevatorMoveAction(
    val direction: PathElevatorDirection,
    // Distance in pixels. For direction Right, it describes the  (distance, 0)
    // movement delta, for Left: (-distance, 0), for DownRight:
    // (distance, distance), etc.
    val distance: Int,
)

class PathElevatorStep(
    private val lazyPath: Lazy<PathElevatorPath>,
    imageMetadata: ImageMetadata,
    initialRelativePosition: IntVec2,
) {
    val path: PathElevatorPath
        get() = lazyPath.value

    private val _relativePosition = MutCell(initialRelativePosition)

    val relativePosition: Cell<IntVec2> = _relativePosition

    val position by lazy {
        Cell.map2(
            path.position,
            relativePosition,
        ) { pp, rp -> pp + rp }
    }

    val wapSprite by lazy {
        DynamicWapSprite.fromImageMetadata(
            imageMetadata = imageMetadata,
            position = position,
        )
    }

    val center by lazy { wapSprite.center }

    fun move(
        positionDelta: Cell<IntVec2>,
        tillStop: Till,
    ) {
        val initialRelativePosition = _relativePosition.sample()
        val targetRelativePosition = positionDelta.map { d -> initialRelativePosition + d }

        targetRelativePosition.reactTill(tillStop) {
            if (relativePosition.sample() != it) {
                _relativePosition.set(it)
            }
        }
    }

    fun moveTo(
        globalPosition: Cell<IntVec2>,
        tillStop: Till,
    ) {
        globalPosition.reactTill(tillStop) {
            if (position.sample() != it) {
                _relativePosition.set(it - path.position.sample())
            }
        }
    }

    fun toData(): PathElevatorStepData = PathElevatorStepData(
        relativePosition = relativePosition.sample(),
    )

    val previous: Cell<PathElevatorStep?> by lazy { path.getPrevious(this) }

    val next: Cell<PathElevatorStep?> by lazy { path.getNext(this) }
}

class PathElevatorEdge(
    startStep: PathElevatorStep,
    endStep: PathElevatorStep,
) {
    val start = startStep.center

    val end = endStep.center

    private val movementDelta = Cell.map2(
        start,
        end,
    ) { startPosition, endPosition ->
        endPosition - startPosition
    }

    val moveAction: Cell<PathElevatorMoveAction?> = movementDelta.map {
        when {
            it.x < 0 && it.y == -it.x -> PathElevatorMoveAction(
                direction = PathElevatorDirection.DownLeft,
                distance = it.x.absoluteValue,
            )
            it.x == 0 && it.y > 0 -> PathElevatorMoveAction(
                direction = PathElevatorDirection.Down,
                distance = it.y.absoluteValue,
            )
            it.x == it.y && it.y > 0 -> PathElevatorMoveAction(
                direction = PathElevatorDirection.DownRight,
                distance = it.x.absoluteValue,
            )
            it.x < 0 && it.y == 0 -> PathElevatorMoveAction(
                direction = PathElevatorDirection.Left,
                distance = it.x.absoluteValue,
            )
            // TODO: Support delay properly
            it.x == 0 && it.y == 0 -> PathElevatorMoveAction(
                direction = PathElevatorDirection.None,
                distance = 0,
            )
            it.x > 0 && it.y == 0 -> PathElevatorMoveAction(
                direction = PathElevatorDirection.Right,
                distance = it.x.absoluteValue,
            )
            it.x < 0 && it.y == it.x -> PathElevatorMoveAction(
                direction = PathElevatorDirection.UpLeft,
                distance = it.x.absoluteValue,
            )
            it.x == 0 && it.y < 0 -> PathElevatorMoveAction(
                direction = PathElevatorDirection.Up,
                distance = it.y.absoluteValue,
            )
            it.x > 0 && it.y == -it.x -> PathElevatorMoveAction(
                direction = PathElevatorDirection.UpRight,
                distance = it.x.absoluteValue,
            )
            else -> null
        }
    }

    val isValid = moveAction.map { it != null }
}

class PathElevatorPath(
    private val imageMetadata: ImageMetadata,
    val position: Cell<IntVec2>,
    initialSteps: List<PathElevatorStep>,
) {
    companion object {
        const val stepCountLimit = 8

        fun create(
            imageMetadata: ImageMetadata,
            position: Cell<IntVec2>,
            initialStepsConfig: List<PathElevatorStepData>,
        ): PathElevatorPath = looped { path ->
            val steps = initialStepsConfig.map { config ->
                PathElevatorStep(
                    lazyPath = path,
                    imageMetadata = imageMetadata,
                    initialRelativePosition = config.relativePosition,
                )
            }

            PathElevatorPath(
                imageMetadata = imageMetadata,
                position = position,
                initialSteps = steps,
            )
        }
    }

    private val _steps = MutableDynamicList(initialContent = initialSteps)

    val steps: DynamicList<PathElevatorStep>
        get() = _steps

    fun removeStep(step: PathElevatorStep) {
        _steps.remove(step)
    }

    fun insertStepAfter(step: PathElevatorStep) {
        val stepIndex = steps.volatileContentView.indexOf(step)

        step.next.sample()?.let { nextStep ->
            val stepPosition = step.relativePosition.sample()
            val nextStepPosition = nextStep.relativePosition.sample()

            val newStepPosition = stepPosition + (nextStepPosition - stepPosition) / 2

            val newStep = PathElevatorStep(
                lazyPath = lazyOf(this),
                imageMetadata = imageMetadata,
                initialRelativePosition = newStepPosition,
            )

            _steps.insert(stepIndex + 1, newStep)
        }
    }

    val edges: DynamicList<PathElevatorEdge> by lazy {
        steps.zipWithNext { startStep, endStep ->
            PathElevatorEdge(
                startStep = startStep,
                endStep = endStep,
            )
        }.withAppended(
            Cell.map2(
                steps.first(),
                steps.last(),
            ) { first, last ->
                PathElevatorEdge(
                    startStep = last,
                    endStep = first,
                )
            }
        )
    }

    fun getPrevious(node: PathElevatorStep): Cell<PathElevatorStep?> =
        steps.indexOf(node).mapNested { index -> steps.getOrNull(index - 1) }.switch()

    fun getNext(node: PathElevatorStep): Cell<PathElevatorStep?> =
        steps.indexOf(node).mapNested { index -> steps.getOrNull(index + 1) }.switch()
}

class PathElevator(
    rezIndex: RezIndex,
    prototype: ElevatorPrototype,
    initialPosition: IntVec2,
    initialStepsConfig: List<PathElevatorStepData>,
) :
    Entity(),
    WapObjectExportable {

    companion object {
        fun load(
            rezIndex: RezIndex,
            retail: Retail,
            data: PathElevatorData,
        ): PathElevator = PathElevator(
            rezIndex = rezIndex,
            prototype = retail.elevatorPrototype,
            initialPosition = data.position,
            initialStepsConfig = data.steps,
        )
    }

    private val elevatorImageMetadata = rezIndex.getImageMetadata(
        imageSetId = prototype.elevatorImageSetId,
        i = -1,
    )!!

    override val entityPosition: EntityPosition =
        EntityPixelPosition(
            initialPosition = initialPosition,
        )

    val path = PathElevatorPath.create(
        imageMetadata = elevatorImageMetadata,
        position = entityPosition.position,
        initialStepsConfig = initialStepsConfig,
    )

    // TODO: Delays
    // TODO: Open path

    override fun isSelectableIn(area: IntRect): Boolean =
        path.steps.volatileContentView.any {
            val stepBoundingBox = it.wapSprite.boundingBox.sample()
            stepBoundingBox.overlaps(area)
        }

    override fun toEntityData(): EntityData = PathElevatorData(
        position = position.sample(),
        steps = path.steps.volatileContentView.map { it.toData() },
    )

    override fun exportWapObject(): Wwd.Object_ {
        fun encodeActions(
            moveAction1: PathElevatorMoveAction?,
            moveAction2: PathElevatorMoveAction?,
        ): Geometry.Rectangle = Geometry.Rectangle(
            left = moveAction1?.direction?.code ?: 0,
            top = moveAction1?.distance ?: 0,
            right = moveAction2?.direction?.code ?: 0,
            bottom = moveAction2?.distance ?: 0,
        )

        val firstStep = path.steps.first().sample()

        val position = firstStep.position.sample()

        val moveActions = path.edges.volatileContentView.map { it.moveAction.sample() }

        return WapObjectPrototype.PathElevatorPrototype.wwdObjectPrototype.copy(
            x = position.x,
            y = position.y,
            moveRect = encodeActions(
                moveActions.getOrNull(0),
                moveActions.getOrNull(1),
            ),
            hitRect = encodeActions(
                moveActions.getOrNull(2),
                moveActions.getOrNull(3),
            ),
            attackRect = encodeActions(
                moveActions.getOrNull(4),
                moveActions.getOrNull(5),
            ),
            clipRect = encodeActions(
                moveActions.getOrNull(6),
                moveActions.getOrNull(7),
            ),
        )
    }
}

@Serializable
@SerialName("PathElevator")
data class PathElevatorStepData(
    val relativePosition: IntVec2,
)

@Serializable
@SerialName("PathElevator")
data class PathElevatorData(
    val position: IntVec2,
    val steps: List<PathElevatorStepData>,
) : EntityData()
