package icesword.editor

import icesword.TILE_SIZE
import icesword.frp.Cell
import icesword.frp.map
import icesword.geometry.IntVec2

private const val hitBoxRadius = 32

class StartPoint(
    initialTileOffset: IntVec2, // TODO: Refactor to pixel offset
) :
    Entity(),
    EntityTileOffset by SimpleEntityTileOffset(
        initialTileOffset = initialTileOffset,
    ) {

    override fun isSelectableAt(worldPoint: IntVec2): Boolean =
        (position.sample() - worldPoint).length < hitBoxRadius
}