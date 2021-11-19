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
import kotlinx.serialization.UseSerializers

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

    val previous: PathElevatorStep? by lazy { path.getPrevious(this) }

    val next: PathElevatorStep? by lazy { path.getNext(this) }
}

class PathElevatorPath(
    val position: Cell<IntVec2>,
    val steps: List<PathElevatorStep>,
) {
    companion object {
        fun create(
            imageMetadata: ImageMetadata,
            position: Cell<IntVec2>,
        ): PathElevatorPath = looped { path ->
            val steps = listOf(
                PathElevatorStep(
                    lazyPath = path,
                    imageMetadata = imageMetadata,
                    initialRelativePosition = IntVec2(64 * 1, 0),
                ),
                PathElevatorStep(
                    lazyPath = path,
                    imageMetadata = imageMetadata,
                    initialRelativePosition = IntVec2(64 * 2, 0),
                ),
                PathElevatorStep(
                    lazyPath = path,
                    imageMetadata = imageMetadata,
                    initialRelativePosition = IntVec2(64 * 3, 0),
                ),
            )

            PathElevatorPath(
                position = position,
                steps = steps,
            )
        }
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
) :
    Entity(),
    WapObjectExportable {

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
    )


    override fun isSelectableIn(area: IntRect): Boolean =
        path.steps.any {
            val stepBoundingBox = it.wapSprite.boundingBox.sample()
            stepBoundingBox.overlaps(area)
        }

    override fun toEntityData(): EntityData? = null

    override fun exportWapObject(): Wwd.Object_ =
        ElevatorPrototype.wwdObjectPrototype
}