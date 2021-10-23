package icesword.editor

import icesword.TILE_SIZE
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.map
import icesword.geometry.IntRect
import icesword.geometry.IntVec2

private const val hitBoxRadius = 32

class StartPoint(
    initialPosition: IntVec2,
) :
    Entity() {

    private val _position = MutCell(initialPosition)

    override val position: Cell<IntVec2>
        get() = _position

    override fun isSelectableIn(area: IntRect): Boolean {
        val hitBox = IntRect.fromCenter(
            center = position.sample(),
            sideLength = hitBoxRadius * 2,
        )

        return hitBox.overlaps(area)
    }

    // TODO: Nuke?
    override val tileOffset: Cell<IntVec2> =
        position.map { it.divRound(TILE_SIZE) }

    override fun setPosition(newPosition: IntVec2) {
        _position.set(newPosition)
    }

    override fun toString(): String =
        "StartPoint()"
}
