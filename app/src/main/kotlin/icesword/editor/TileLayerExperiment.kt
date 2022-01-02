package icesword.editor

import icesword.editor.entities.Entity
import icesword.editor.entities.SimpleEntityTilePosition
import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.tileAtPoint

interface TilesView {
    fun getTile(globalCoord: IntVec2): Int?
}

data class OffsetTilesView(
    val offset: IntVec2,
    val localTilesView: Map<IntVec2, Int>,
) : TilesView {
    override fun getTile(globalCoord: IntVec2): Int? =
        localTilesView[globalCoord - offset]
}

interface EditorTilesView {
    val primaryTilesView: TilesView
    val previewTilesView: TilesView
}
