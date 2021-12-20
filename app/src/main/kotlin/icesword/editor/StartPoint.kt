package icesword.editor

import icesword.geometry.IntRect
import icesword.geometry.IntVec2

private const val hitBoxRadius = 32

class StartPoint(
    initialPosition: IntVec2,
) : Entity() {

    override val entityPosition = EntityPixelPosition(
        initialPosition = initialPosition,
    )

    override fun isSelectableIn(area: IntRect): Boolean {
        val hitBox = IntRect.fromCenter(
            center = position.sample(),
            sideLength = hitBoxRadius * 2,
        )

        return hitBox.overlaps(area)
    }

    override fun toString(): String =
        "StartPoint()"
}
