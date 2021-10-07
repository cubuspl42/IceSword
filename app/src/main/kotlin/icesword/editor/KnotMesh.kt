package icesword.editor

import icesword.TILE_SIZE
import icesword.frp.*
import icesword.geometry.IntVec2
import icesword.tileAtPoint
import icesword.tileTopLeftCorner

abstract class KnotPrototype {
    override fun toString(): String =
        this::class.simpleName ?: "?"
}

abstract class RockPrototype : KnotPrototype()

object UndergroundRockPrototype : RockPrototype()

object OvergroundRockPrototype : RockPrototype()

//interface KnotFormula {
//    // Knot filter. Vectors represent knot coordinates relative to the meta tile to be generated. Coordinate (0, 0) is
//    // the knot on the bottom right of the tile. Non-null entry represent the knot required to be present at the given
//    // coordinate. Null entry means that the given knot coordinate must be unoccupied.
//    val filter: Map<IntVec2, KnotPrototype?>
//
//    // Meta tile that is generated if the filter passes.
//    val output: MetaTile
//}

interface KnotFormula {
//    companion object {
//
//        fun buildTile(relativeKnots: Map<IntVec2, KnotPrototype>): Int? {
//            TODO()
//        }
//    }

    // Vectors represent knot-coordinates relative to the meta tile to be generated. Coordinate (0, 0) is
    // the knot on the bottom right of the tile.
    fun buildMetaTile(relativeKnots: Map<IntVec2, KnotPrototype>): Int?
}

fun tilesAroundKnot(knotCoord: IntVec2): Set<IntVec2> {
    @Suppress("UnnecessaryVariable")
    val k = knotCoord

    return setOf(
        k, IntVec2(x = k.x + 1, k.y),
        IntVec2(x = k.x, k.y + 1), IntVec2(x = k.x + 1, k.y + 1),
    )
}

fun tilesAroundKnotLst(knotCoord: IntVec2): List<IntVec2> {
    val k = knotCoord

    return listOf(
        k,
        IntVec2(x = k.x + 1, k.y),
        IntVec2(x = k.x, k.y + 1),
        IntVec2(x = k.x + 1, k.y + 1),
    )
}

fun tilesAroundKnotSeq(knotCoord: IntVec2): Sequence<IntVec2> = sequence {
    val k = knotCoord

    yield(k)
    yield(IntVec2(x = k.x + 1, k.y))
    yield(IntVec2(x = k.x, k.y + 1))
    yield(IntVec2(x = k.x + 1, k.y + 1))
}


fun closestKnot(point: IntVec2): IntVec2 =
    tileAtPoint(point - IntVec2(32, 32))

fun knotCenter(knotCoord: IntVec2): IntVec2 =
    tileTopLeftCorner(
        IntVec2(
            x = knotCoord.x + 1,
            y = knotCoord.y + 1,
        )
    )

fun knotsAroundTile(tileCoord: IntVec2, distance: Int): List<IntVec2> {
    @Suppress("UnnecessaryVariable")
    val d = distance

    val range = (-(d + 1)..d)

    return range.flatMap { y ->
        range.map { x ->
            tileCoord + IntVec2(x, y)
        }
    }
}

class KnotMesh(
    initialTileOffset: IntVec2,
    knotPrototype: KnotPrototype,
    initialSize: Int = 2,
) :
    Entity(),
    EntityTileOffset by SimpleEntityTileOffset(
        initialTileOffset = initialTileOffset,
    ) {


    private val _localKnots = MutableDynamicSet.of(
        (0 until initialSize).flatMap { i ->
            (0 until initialSize).map { j ->
                IntVec2(j, i)
            }
        }.toSet()
    )

    val localKnots: DynamicSet<IntVec2>
        get() = _localKnots

    private val globalKnotCoords = localKnots
        .adjust(
            hash = IntVec2.HASH,
            adjustment = tileOffset,
        ) { localKnotCoord: IntVec2, tileOffset: IntVec2 ->
            tileOffset + localKnotCoord
        }


//    val globalKnots: DynamicMap<IntVec2, KnotPrototype> =
//        DynamicMap.fromEntries(
//            _localKnots.fuseMap { localKnotCoord ->
//                tileOffset.map { (it + localKnotCoord) to knotPrototype }
//            }
//        ).also {
//            // FIXME
//            it.changes.subscribe { }
//        }

    val globalKnots = globalKnotCoords.associateWith("globalKnots") {
        knotPrototype
    }

    fun putKnot(globalKnotCoord: IntVec2) {
        val localKnotCoord = globalKnotCoord - tileOffset.sample()
        _localKnots.add(localKnotCoord)

//        val tileCoords = tilesAroundKnot(localKnotCoord)
//
//        val localKnots = this._localKnots.sample()

//        tileCoords.forEach { tileCoord ->
//            localTiles.put(tileCoord, buildTileAt(localKnots, tileCoord))
//        }
    }

//    private val localTileCoords = localKnots.unionMap(::tilesAroundKnot)

//    private val localTiles = localTileCoords.associateWith { tileCoord ->
//        val nearbyKnots = knotsAroundTile(tileCoord, distance = 1)
//            .filter(localKnots::contains)
//            .toSet()
//
//        val relativeKnots = nearbyKnots.map { it - tileCoord }.toSet()
//
//        buildTile(relativeKnots)
//    }


//    private fun <A> withCounter(f: (inc: () -> Unit) -> A): A {
//
//        var counter = 0
//
//        fun inc() {
//            ++counter
//        }
//
//        return f(::inc)
//
//        val a = f(::inc)
//
//        println("Counter: $counter")
//
//        return a
//    }

// LOCAL TILES

//    private val localTiles = run {
//        val localKnots = _localKnots.sample()
//        val localTileCoords = localKnots.flatMap(::tilesAroundKnot)
//
//        MutableDynamicMap.of(
//            localTileCoords.associateWith { tileCoord ->
//                buildTileAt(localKnots, tileCoord)
//            }
//        )
//    }

//    private fun buildTileAt(
//        localKnots: Set<IntVec2>,
//        tileCoord: IntVec2,
//    ): Int {
//        val nearbyKnots = knotsAroundTile(tileCoord, distance = 1)
//            .filter(localKnots::contains)
//
//        val relativeKnots = nearbyKnots.map { it - tileCoord }
//
//        return buildTile(relativeKnots)
//    }

//    private val localTiles = DynamicMap.diff(
//        Cell.map2(
//            localKnots.trackContent(till),
//            localTileCoords.trackContent(till),
//        ) { localKnots, localTileCoords ->
//            withCounter { inc ->
//                localTileCoords.associateWith { tileCoord ->
//                    val nearbyKnots = knotsAroundTile(tileCoord, distance = 1)
//                        .filter(localKnots::contains)
//
//                    val relativeKnots = nearbyKnots.map { it - tileCoord }
//
//                    inc()
//                    buildTile(relativeKnots)
//                }
//            }
//        },
//    )

//    val tiles = localTiles.mapKeys(tag = "localTiles.mapKeys") { (localTileOffset, _) ->
//        initialTileOffset + localTileOffset
//    }

//    val tilesDynamic = localTiles.mapKeysDynamic { (localTileOffset, _) ->
//        tileOffset.map { it + localTileOffset }
//    }

//    private val globalTileCoords = globalKnots.keys.unionMapDynamic { DynamicSet.of(tilesAroundKnot(it)) }

    override fun isSelectableAt(worldPoint: IntVec2): Boolean {
        return globalKnots.volatileContentView.keys.any {
            (knotCenter(it) - worldPoint).length < TILE_SIZE
        }
    }

    override fun toString(): String =
        "KnotMesh(tileOffset=${tileOffset.sample()})"
}

fun buildTile(
    tileCoord: IntVec2,
    globalKnots: Map<IntVec2, KnotPrototype>,
): Int {
    return 620
}

fun buildTile_(
    tileCoord: IntVec2,
    globalKnots: Map<IntVec2, KnotPrototype>,
): Int {
//    val relativeKnotCoords = relativeKnots.keys

    fun getKnot(relativeCoord: IntVec2): KnotPrototype? =
        globalKnots.get(tileCoord.plus(relativeCoord));

    fun hasKnot(relativeCoord: IntVec2) =
        getKnot(relativeCoord) != null;

    fun intVec2(y: Int, x: Int) = IntVec2(x, y)

    return when {
        getKnot(intVec2(0, 0)) == UndergroundRockPrototype &&
                !hasKnot(intVec2(-1, -1)) &&
                !hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(0, -1)) -> 620
        getKnot(intVec2(0, 0)) == UndergroundRockPrototype &&
                hasKnot(intVec2(0, -1)) &&
                !hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(-1, -1)) -> 621
        // &&
//                !hasKnot(intVec2(0, -2)
        getKnot(intVec2(0, 0)) == UndergroundRockPrototype &&
                hasKnot(intVec2(0, -1)) &&
                !hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(-1, -1)) -> 622
        getKnot(intVec2(0, -1)) == UndergroundRockPrototype &&
                !hasKnot(intVec2(-1, -1)) &&
                !hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(0, 0)) -> 623
        getKnot(intVec2(0, 0)) == OvergroundRockPrototype &&
                !hasKnot(intVec2(-1, -1)) &&
                !hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(0, -1)) -> 603
        getKnot(intVec2(0, 0)) == OvergroundRockPrototype &&
                hasKnot(intVec2(0, -1)) &&
                !hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(-1, -1)) -> 604
        // &&
        //                !hasKnot(intVec2(0, -2))
        getKnot(intVec2(0, -1)) == OvergroundRockPrototype &&
                !hasKnot(intVec2(-1, -1)) &&
                !hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(0, 0)) -> 607
        hasKnot(intVec2(0, 0)) &&
                hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(0, -1)) &&
                !hasKnot(intVec2(-1, -1)) -> 624
        hasKnot(intVec2(-1, -1)) &&
                hasKnot(intVec2(-1, 0)) &&
                hasKnot(intVec2(0, -1)) &&
                hasKnot(intVec2(0, 0)) &&
                !hasKnot(intVec2(-1, -2)) &&
                !hasKnot(intVec2(0, -2)) -> 613
        hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(-1, -1)) &&
                !hasKnot(intVec2(0, -1)) &&
                !hasKnot(intVec2(0, 0)) -> 632
        hasKnot(intVec2(-1, -1)) &&
                hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(-1, -2)) &&
                !hasKnot(intVec2(0, -2)) &&
                !hasKnot(intVec2(0, -1)) &&
                !hasKnot(intVec2(0, 0)) -> 633
        hasKnot(intVec2(-1, -1)) &&
                hasKnot(intVec2(-1, 0)) &&
                hasKnot(intVec2(-1, -2)) &&
                !hasKnot(intVec2(0, -1)) &&
                !hasKnot(intVec2(0, 0)) -> 634
        hasKnot(intVec2(-1, -1)) &&
                !hasKnot(intVec2(0, -1)) &&
                !hasKnot(intVec2(0, 0)) &&
                !hasKnot(intVec2(-1, 0)) -> 635
        hasKnot(intVec2(-1, -1)) &&
                hasKnot(intVec2(0, -1)) &&
                !hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(0, 0)) -> 615
        hasKnot(intVec2(0, -1)) &&
                hasKnot(intVec2(-1, 0)) &&
                hasKnot(intVec2(-1, -1)) &&
                !hasKnot(intVec2(0, 0)) -> 617
        hasKnot(intVec2(0, 0)) &&
                hasKnot(intVec2(-1, 0)) &&
                hasKnot(intVec2(-1, -1)) &&
                !hasKnot(intVec2(0, -1)) -> 618
        getKnot(intVec2(-1, -1)) is RockPrototype &&
                getKnot(intVec2(0, -1)) is RockPrototype &&
                getKnot(intVec2(0, 0)) is RockPrototype &&
                !hasKnot(intVec2(-1, 0)) -> 638
        else -> 630
    }
}
