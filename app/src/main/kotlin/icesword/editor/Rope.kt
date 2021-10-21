package icesword.editor

import icesword.TILE_SIZE
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.map
import icesword.geometry.IntVec2
import icesword.scene.Texture

class Rope(
    val texture: Texture,
    initialPosition: IntVec2,
) :
    Entity() {

    companion object {
        const val radius: Int = 16
    }

    private val _position = MutCell(initialPosition)

    override val position: Cell<IntVec2>
        get() = _position

    override fun isSelectableAt(worldPoint: IntVec2): Boolean {
        val localPoint = worldPoint - position.sample()
        val rect = texture.sourceRect
        return rect.contains(localPoint)
    }

    // TODO: Deduplicate!
    override val tileOffset: Cell<IntVec2> =
        position.map { it.divRound(TILE_SIZE) }

    override fun setPosition(newPosition: IntVec2) {
        _position.set(newPosition)
    }

    override fun toString(): String =
        "Rope()"
}

