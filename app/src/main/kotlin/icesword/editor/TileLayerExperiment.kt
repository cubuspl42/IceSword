package icesword.editor

import icesword.frp.*
import icesword.geometry.IntVec2
import icesword.tileAtPoint
import org.khronos.webgl.get

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
        tilesViews.firstNotNullOfOrNull { it.getTile(globalCoord) }
}

class TileEntity(
    initialTileOffset: IntVec2,
) :
    Entity(),
    EntityTileOffset by SimpleEntityTileOffset(
        initialTileOffset = initialTileOffset,
    ) {
    override fun isSelectableAt(worldPoint: IntVec2): Boolean =
        tilesView.view.getTile(worldPoint) != null

    override val tileOffset = MutCell(IntVec2.ZERO)

//    val zOrder: Cell<Int> = Cell.constant(1)

    private val a = 16

    private val localTiles: DynamicMap<IntVec2, Int> = DynamicMap.of(
        (0 until a).flatMap { i ->
            (0 until a).map { j ->
                IntVec2(j, i) to 630
            }
        }.toMap()
    )

//    val tilesView = Cell.map2(
//        tileOffset,
//        localTiles.contentView,
//    ) { offset, localTilesView ->
//        OffsetTilesView(
//            offset = offset,
//            localTilesView = localTilesView,
//        )
//    }

    val tilesView: DynamicView<TilesView> = DynamicView.map2(
        tileOffset.asView,
        localTiles.contentDynamicView,
    ) { offset, localTilesView ->
        object : TilesView {
            override fun getTile(globalCoord: IntVec2): Int? =
                localTilesView[globalCoord - offset.sample()]
        }
    }
}

class ExperimentalTileLayer(
    startPoint: IntVec2,
) {
    private val tileEntities = DynamicSet.of(
        setOf(
            TileEntity(initialTileOffset = tileAtPoint(startPoint) + IntVec2(4, 0)),
        )
    )

//    val tilesViews: DynamicList<TilesView> =
//        tileEntities.sortedByDynamic { it.zOrder }
//            .map { it.tilesView }


//    val tilesView: Cell<TilesView> = run {
//        val tileEntitiesTilesViews: DynamicList<OffsetTilesView> =
//            tileEntities.fuseMapOrdered {
//                OrderedDynamic(
//                    it.tilesView,
//                    key = it.zOrder,
//                )
//            }
//
//        tileEntitiesTilesViews.contentView.map { tilesViewsView ->
//            CombinedTilesView(tilesViews = tilesViewsView)
//        }
//    }

    val tilesView: DynamicView<TilesView> = run {
//        val tileEntitiesTilesViews: DynamicList<DynamicView<TilesView>> =
//            tileEntities.map {
//                OrderedDynamic(
//                    it.tilesView2,
//                    key = it.zOrder,
//                )
//            }.ordered()

        val tileEntitiesTilesViews: DynamicView<Set<TilesView>> =
            tileEntities.blendMap { it.tilesView }

//        val foo = tileEntitiesTilesViews.contentDynamicView
//
//        tileEntitiesTilesViews.contentView.map { tilesViewsView ->
//            CombinedTilesView(tilesViews = tilesViewsView)
//        }

        val outView: DynamicView<TilesView> =
            tileEntitiesTilesViews.map { tilesViews ->
                object : TilesView {
                    override fun getTile(globalCoord: IntVec2): Int? =
                        tilesViews.firstNotNullOfOrNull { it.getTile(globalCoord) }
                }
            }

        outView
    }
}