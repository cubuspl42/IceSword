@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.ImageMetadata
import icesword.RezIndex
import icesword.editor.WapObjectPrototype.ElevatorPrototype
import icesword.frp.Cell
import icesword.frp.Loop.Companion.looped
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.reactTill
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.Wwd
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.math.absoluteValue

class PathElevatorStep(
    private val lazyPath: Lazy<PathElevatorPath>,
    imageMetadata: ImageMetadata,
    initialRelativePosition: IntVec2,
) {
    val path: PathElevatorPath
        get() = lazyPath.value

    private val _relativePosition = MutCell(initialRelativePosition)

    private val relativePosition: Cell<IntVec2> = _relativePosition

    val position by lazy {
        Cell.map2(
            path.position,
            relativePosition,
        ) { pp, rp -> pp + rp }
    }

    val wapSprite by lazy {
        WapSprite(
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

    val previous: PathElevatorStep? by lazy { path.getPrevious(this) }

    val next: PathElevatorStep? by lazy { path.getNext(this) }
}

class PathElevatorEdge(
    startStep: PathElevatorStep,
    endStep: PathElevatorStep,
) {
    val start = startStep.center

    val end = endStep.center

    val movementDelta = Cell.map2(
        start,
        end,
    ) { startPosition, endPosition ->
        endPosition - startPosition
    }

    val isValid = movementDelta.map {
        it.x.absoluteValue == it.y.absoluteValue || it.x == 0 || it.y == 0
    }
}

class PathElevatorPath(
    val position: Cell<IntVec2>,
    val steps: List<PathElevatorStep>,
) {
    companion object {
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
                position = position,
                steps = steps,
            )
        }
    }

    val edges by lazy {
        steps.zipWithNext { startStep, endStep ->
            PathElevatorEdge(
                startStep = startStep,
                endStep = endStep,
            )
        } + PathElevatorEdge(
            startStep = steps.last(),
            endStep = steps.first(),
        )
    }

    fun getPrevious(node: PathElevatorStep): PathElevatorStep? {
        val i = steps.indexOf(node)
        if (i < 0) throw IllegalArgumentException()
        return steps.getOrNull(i - 1)
    }

    fun getNext(node: PathElevatorStep): PathElevatorStep? {
        val i = steps.indexOf(node)
        if (i < 0) throw IllegalArgumentException()
        return steps.getOrNull(i + 1)
    }
}

class PathElevator(
    rezIndex: RezIndex,
    initialPosition: IntVec2,
    initialStepsConfig: List<PathElevatorStepData>,
) :
    Entity(),
    WapObjectExportable {

    companion object {
        fun load(
            rezIndex: RezIndex,
            data: PathElevatorData,
        ): PathElevator = PathElevator(
            rezIndex = rezIndex,
            initialPosition = data.position,
            initialStepsConfig = data.steps,
        )
    }

    private val elevatorImageMetadata = rezIndex.getImageMetadata(
        imageSetId = ElevatorPrototype.imageSetId,
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

    // TODO: Export
    // TODO: Add/remove step
    // TODO: Delays
    // TODO: Open path

    override fun isSelectableIn(area: IntRect): Boolean =
        path.steps.any {
            val stepBoundingBox = it.wapSprite.boundingBox.sample()
            stepBoundingBox.overlaps(area)
        }

    override fun toEntityData(): EntityData {
        return PathElevatorData(
            position = position.sample(),
            steps = path.steps.map { it.toData() },
        )
    }

    override fun exportWapObject(): Wwd.Object_ =
        ElevatorPrototype.wwdObjectPrototype
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
