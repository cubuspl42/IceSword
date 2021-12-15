package icesword.editor

import icesword.frp.Cell
import icesword.frp.DynamicMap
import icesword.frp.DynamicSet
import icesword.frp.adjust
import icesword.frp.associateWith
import icesword.frp.associateWithDynamic
import icesword.frp.fuseValues
import icesword.frp.getKeys
import icesword.frp.map
import icesword.frp.switchMap
import icesword.frp.unionMapDynamic
import icesword.frp.unionWith
import icesword.frp.valuesSet
import icesword.geometry.IntVec2

interface TileGeneratorContext {
    fun containsAll(vararg metaTiles: MetaTile): Boolean
}

interface TileGenerator {
    fun buildTile(context: TileGeneratorContext): Int?
}

class MetaTileCluster(
    private val tileOffset: Cell<IntVec2>,
    val localMetaTiles: DynamicMap<IntVec2, MetaTile>,
) {
    val globalTileCoords: DynamicSet<IntVec2> =
        localMetaTiles.getKeys().adjust(
            hash = IntVec2.HASH,
            adjustment = tileOffset,
        ) { localKnotCoord: IntVec2, tileOffset: IntVec2 ->
            tileOffset + localKnotCoord
        }.also {
            it.changes.subscribe { change ->
            }
        }

    fun getMetaTileAt(globalTileCoord: IntVec2): Cell<MetaTile?> =
        tileOffset.switchMap { localMetaTiles.get(globalTileCoord - it) }

    override fun toString(): String = "MetaTileCluster(tileOffset=${tileOffset.sample()})"
}

class MetaTileLayer(
    private val tileGenerator: TileGenerator,
    knotMeshLayer: KnotMeshLayer,
    elastics: DynamicSet<Elastic>,
) {
    private val metaTileClusters = elastics.map(
        tag = "MetaTileLayer/elastics.map"
    ) { it.metaTileCluster }
        .unionWith(DynamicSet.of(setOf(knotMeshLayer.metaTileCluster)))

    private val globalTileCoords: DynamicSet<IntVec2> =
        metaTileClusters.unionMapDynamic(
            tag = "metaTileClusters.unionMapDynamic",
        ) { it.globalTileCoords }
            .also {
                it.changes.subscribe { change ->
                }
            }

    val tiles: DynamicMap<IntVec2, Int> =
        globalTileCoords.associateWithDynamic(
            tag = "globalTileCoords.associateWithDynamic",
            this::buildTileAt,
        ).also {
            it.changes.subscribe { } // FIXME
        }

    private fun buildTileAt(
        globalTileCoord: IntVec2,
    ): Cell<Int> {
        val metaTiles = metaTileClusters
            .associateWith(tag = "buildTileAt($globalTileCoord) / .associateWith") {
                it.getMetaTileAt(globalTileCoord)
            }
            .fuseValues(tag = "buildTileAt($globalTileCoord) / .fuseValues")
            .valuesSet

        return metaTiles.content.map { metaTilesContent ->
            val context = object  : TileGeneratorContext {
                override fun containsAll(vararg metaTiles: MetaTile): Boolean =
                    metaTiles.all { metaTilesContent.contains(it) }
            }

            val tileId = tileGenerator.buildTile(context)

            tileId ?: metaTilesContent.firstNotNullOfOrNull { it?.tileId } ?: -1
        }
    }

}
