package icesword.editor

import icesword.TILE_SIZE
import icesword.editor.entities.Elastic
import icesword.editor.entities.Fixture
import icesword.frp.Cell
import icesword.frp.DynamicMap
import icesword.frp.DynamicSet
import icesword.frp.adjust
import icesword.frp.associateWith
import icesword.frp.associateWithDynamic
import icesword.frp.fuseValues
import icesword.frp.getKeys
import icesword.frp.map
import icesword.frp.distinctMap
import icesword.frp.mapNested
import icesword.frp.switchMap
import icesword.frp.unionMapDynamic
import icesword.frp.unionWith
import icesword.frp.valuesSet
import icesword.geometry.IntRect
import icesword.geometry.IntVec2

interface TileGeneratorContext {
    fun containsAll(vararg metaTiles: MetaTile): Boolean
}

interface TileGenerator {
    companion object {
        fun chained(vararg generators: TileGenerator): TileGenerator =
            ChainedTileGenerator(generators.toList())

        fun forwardAll(vararg metaTiles: MetaTile): TileGenerator = ChainedTileGenerator(
            metaTiles.map { ForwardTileGenerator(it) },
        )
    }

    fun buildTile(context: TileGeneratorContext): Int?
}

class ChainedTileGenerator(
    private val generators: List<TileGenerator>,
) : TileGenerator {
    override fun buildTile(context: TileGeneratorContext): Int? =
        generators.firstNotNullOfOrNull { it.buildTile(context) }
}

class ForwardTileGenerator(
    private val metaTile: MetaTile,
) : TileGenerator {
    override fun buildTile(context: TileGeneratorContext): Int? =
        if (context.containsAll(metaTile)) metaTile.tileId
        else null
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

    private val localTileBoundingBox: Cell<IntRect?> =
        localMetaTiles.content.map { localMetaTilesNow ->
            if (localMetaTilesNow.isEmpty()) null
            else IntRect.enclosing(
                localMetaTilesNow.keys.map { IntRect.unit(position = it) },
            )
        }

    private val tileBoundingBox = Cell.map2(
        tileOffset,
        localTileBoundingBox,
    ) { tileOffsetNow, localTileBoundingBoxNow ->
        localTileBoundingBoxNow?.translate(tileOffsetNow)
    }

    val boundingBox: Cell<IntRect?> =
        tileBoundingBox.mapNested { it * TILE_SIZE }

    fun getMetaTileAt(globalTileCoord: IntVec2): Cell<MetaTile?> =
        tileOffset.switchMap { localMetaTiles.get(globalTileCoord - it) }

    override fun toString(): String = "MetaTileCluster(tileOffset=${tileOffset.sample()})"
}


class MetaTileLayerProduct(
    private val tileGenerator: TileGenerator,
    private val metaTileClusters: DynamicSet<MetaTileCluster>,
    globalTileCoords: DynamicSet<IntVec2>,
) {
    val tiles: DynamicMap<IntVec2, Int> =
        globalTileCoords.associateWithDynamic(
            tag = "globalTileCoords.associateWithDynamic",
            this::buildTileAt,
        )

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
            val context = object : TileGeneratorContext {
                override fun containsAll(vararg metaTiles: MetaTile): Boolean =
                    metaTiles.all { metaTilesContent.contains(it) }
            }

            val builtTileId = tileGenerator.buildTile(context)

            builtTileId ?: pickFallbackMetaTile(metaTilesContent)?.tileId ?: -1
        }
    }

    private fun pickFallbackMetaTile(metaTilesContent: Set<MetaTile?>): MetaTile? =
        metaTilesContent.filterNotNull().maxWithOrNull(
            compareBy({ it.z }, { it.tileId }),
        )
}

class MetaTileLayer(
    tileGenerator: TileGenerator,
    knotMeshLayer: KnotMeshLayer,
    elastics: DynamicSet<Elastic>,
    fixtures: DynamicSet<Fixture>,
) {
    val metaTileClusters = elastics.distinctMap { it.product.metaTileCluster }
        .unionWith(fixtures.distinctMap { it.product.metaTileCluster })
        .unionWith(DynamicSet.of(setOf(knotMeshLayer.metaTileCluster)))

    private val globalTileCoords: DynamicSet<IntVec2> =
        metaTileClusters.unionMapDynamic(
            tag = "metaTileClusters.unionMapDynamic",
        ) { it.globalTileCoords }

    val product = MetaTileLayerProduct(
        tileGenerator = tileGenerator,
        metaTileClusters = metaTileClusters,
        globalTileCoords = globalTileCoords,
    )
}
