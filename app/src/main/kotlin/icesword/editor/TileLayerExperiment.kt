package icesword.editor

import icesword.frp.*
import icesword.geometry.IntVec2

interface TilesView {
    fun getTile(globalCoord: IntVec2): Int?
}

data class OffsetTilesView(
    val offset: IntVec2,
    val localTilesView: Map<IntVec2, Int>
) : TilesView {
    override fun getTile(globalCoord: IntVec2): Int? =
        localTilesView[globalCoord - offset]
}

data class CombinedTilesView(
    val tilesViews: List<TilesView>,
) : TilesView {
    override fun getTile(globalCoord: IntVec2): Int? =
        tilesViews.firstNotNullOf { it.getTile(globalCoord) }
}

class TileEntity {
    val tileOffset = MutCell(IntVec2.ZERO)

    val zOrder: Cell<Int> = Cell.constant(1)

    val localTiles: DynamicMap<IntVec2, Int> = DynamicMap.of(
        mapOf(

        )
    )

    val tilesView = Cell.map2(
        tileOffset,
        localTiles.contentView,
    ) { offset, localTilesView ->
        OffsetTilesView(
            offset = offset,
            localTilesView = localTilesView,
        )
    }
}


class ExperimentalTileLayer {
    private val tileEntities = DynamicSet.of(
        setOf(
            TileEntity(),
        )
    )

//    val tilesViews: DynamicList<TilesView> =
//        tileEntities.sortedByDynamic { it.zOrder }
//            .map { it.tilesView }


    val tilesView: Cell<TilesView> = run {
        val tileEntitiesTilesViews: DynamicList<OffsetTilesView> =
            tileEntities.fuseMapOrdered {
                OrderedDynamic(
                    it.tilesView,
                    key = it.zOrder,
                )
            }

        tileEntitiesTilesViews.contentView.map { tilesViewsView ->
            CombinedTilesView(tilesViews = tilesViewsView)
        }
    }
}