package icesword.editor

import icesword.TILE_SIZE
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.map
import icesword.geometry.IntVec2

private const val hitBoxRadius = 32

class StartPoint(
    initialPosition: IntVec2, // TODO: Refactor to pixel offset
) :
    Entity() {

    private val _position = MutCell(initialPosition)

    override val position: Cell<IntVec2>
        get() = _position

    override fun isSelectableAt(worldPoint: IntVec2): Boolean =
        (position.sample() - worldPoint).length < hitBoxRadius

    // TODO: Nuke?
    override val tileOffset: Cell<IntVec2> =
        position.map { it.divRound(TILE_SIZE) }

    override fun setPosition(newPosition: IntVec2) {
        _position.set(newPosition)
    }

    override fun toString(): String =
        "StartPoint()"
}
