@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor

import icesword.editor.KnotPrototype.*
import icesword.frp.Cell
import icesword.frp.DynamicSet
import icesword.frp.MutableDynamicSet
import icesword.frp.adjust
import icesword.frp.associateWith
import icesword.frp.memorized
import icesword.frp.sample
import icesword.frp.unionMap
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.tileAtPoint
import icesword.tileTopLeftCorner
import icesword.tilesOverlappingArea
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
sealed class KnotPrototype {
    @Serializable
    sealed class RockPrototype : KnotPrototype()

    @Serializable
    @SerialName("UndergroundRock")
    object UndergroundRockPrototype : RockPrototype()

    @Serializable
    @SerialName("OvergroundRock")
    object OvergroundRockPrototype : RockPrototype()

    override fun toString(): String =
        this::class.simpleName ?: "?"
}


interface KnotFormula {
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

fun tilesAroundKnotForTileBuilding(knotCoord: IntVec2): List<IntVec2> {
    val k = knotCoord

    return listOf(
        IntVec2(k.x - 1, k.y - 1), IntVec2(k.x + 0, k.y - 1), IntVec2(k.x + 1, k.y - 1), IntVec2(k.x + 2, k.y - 1),
        IntVec2(k.x - 1, k.y + 0), IntVec2(k.x + 0, k.y + 0), IntVec2(k.x + 1, k.y + 0), IntVec2(k.x + 2, k.y + 0),
        IntVec2(k.x - 1, k.y + 1), IntVec2(k.x + 0, k.y + 1), IntVec2(k.x + 1, k.y + 1), IntVec2(k.x + 2, k.y + 1),
        IntVec2(k.x - 1, k.y + 2), IntVec2(k.x + 0, k.y + 2), IntVec2(k.x + 1, k.y + 2), IntVec2(k.x + 2, k.y + 2),
    )
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

fun knotsInArea(worldArea: IntRect): List<IntVec2> =
    tilesOverlappingArea(worldArea.translate(IntVec2(-32, -32)))

class KnotMesh(
    private val knotPrototype: KnotPrototype,
    initialTileOffset: IntVec2,
    initialLocalKnots: Set<IntVec2>,
) :
    Entity() {

    override val entityPosition = EntityTilePosition(
        initialTileOffset = initialTileOffset,
    )

    val tileOffset: Cell<IntVec2> =
        entityPosition.tileOffset

    companion object {
        fun createSquare(
            knotPrototype: KnotPrototype,
            initialTileOffset: IntVec2,
            initialSideLength: Int,
        ): KnotMesh {
            val initialLocalKnots: Set<IntVec2> =
                (0 until initialSideLength).flatMap { i ->
                    (0 until initialSideLength).map { j ->
                        IntVec2(j, i)
                    }
                }.toSet()

            return KnotMesh(
                initialTileOffset = initialTileOffset,
                knotPrototype = knotPrototype,
                initialLocalKnots = initialLocalKnots,
            )
        }

        fun load(data: KnotMeshData): KnotMesh =
            KnotMesh(
                initialTileOffset = data.tileOffset,
                knotPrototype = data.knotPrototype,
                initialLocalKnots = data.localKnotOffsets,
            )
    }


    private val _localKnots = MutableDynamicSet.of(
        initialContent = initialLocalKnots,
        tag = "KnotMesh/_localKnots",
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

    val globalKnots = globalKnotCoords.associateWith("globalKnots") {
        knotPrototype
    }.also {
        // FIXME
        it.changes.subscribe { }
    }

    fun putKnot(globalKnotCoord: IntVec2) {
        val localKnotCoord = globalKnotCoord - tileOffset.sample()
        _localKnots.add(localKnotCoord)
    }

    fun removeKnot(globalKnotCoord: IntVec2) {
        val localKnotCoord = globalKnotCoord - tileOffset.sample()
        _localKnots.remove(localKnotCoord)
    }

    val localTileCoords =
        localKnots.unionMap(tag = "localTileCoords/localKnots.unionMap", ::tilesAroundKnot)
            .memorized()

    override fun isSelectableIn(area: IntRect): Boolean {
        val globalKnotCoords = globalKnots.volatileContentView

        return knotsInArea(area).any { globalKnotCoord ->
            globalKnotCoords.containsKey(globalKnotCoord)
        }
    }

    override fun toString(): String =
        "KnotMesh(tileOffset=${tileOffset.sample()})"

    fun toData(): KnotMeshData = KnotMeshData(
        knotPrototype = knotPrototype,
        tileOffset = tileOffset.sample(),
        localKnotOffsets = localKnots.sample(),
    )

    init {
        localTileCoords.changes.subscribe { } // FIXME
        localKnots.changes.subscribe { } // FIXME
    }
}

@Serializable
data class KnotMeshData(
    val knotPrototype: KnotPrototype,
    val tileOffset: IntVec2,
    val localKnotOffsets: Set<IntVec2>,
)

fun buildTile__(
    tileCoord: IntVec2,
    globalKnots: Map<IntVec2, KnotPrototype>,
): Int {
    return 620
}

fun buildTile(
    tileCoord: IntVec2,
    globalKnots: Map<IntVec2, KnotPrototype>,
): MetaTile? {
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
                !hasKnot(intVec2(0, -1)) -> MetaTile(620)
        getKnot(intVec2(0, 0)) == UndergroundRockPrototype &&
                hasKnot(intVec2(0, -1)) &&
                !hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(-1, -1)) -> MetaTile.RockTop

        // FIXME
//        getKnot(intVec2(0, 0)) == UndergroundRockPrototype &&
//                hasKnot(intVec2(0, -1)) &&
//                !hasKnot(intVec2(-1, 0)) &&
//                !hasKnot(intVec2(-1, -1)) -> MetaTile.RockTop

        getKnot(intVec2(0, -1)) == UndergroundRockPrototype &&
                !hasKnot(intVec2(-1, -1)) &&
                !hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(0, 0)) -> MetaTile(623)
        getKnot(intVec2(0, 0)) == OvergroundRockPrototype &&
                !hasKnot(intVec2(-1, -1)) &&
                !hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(0, -1)) -> MetaTile(603)
        getKnot(intVec2(0, 0)) == OvergroundRockPrototype &&
                hasKnot(intVec2(0, -1)) &&
                !hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(-1, -1)) -> MetaTile.GrassUpper
        // &&
        //                !hasKnot(intVec2(0, -2))
        getKnot(intVec2(0, -1)) == OvergroundRockPrototype &&
                !hasKnot(intVec2(-1, -1)) &&
                !hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(0, 0)) -> MetaTile(607)
        hasKnot(intVec2(0, 0)) &&
                hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(0, -1)) &&
                !hasKnot(intVec2(-1, -1)) -> MetaTile.RockLeftSideOuter
        hasKnot(intVec2(-1, -1)) &&
                hasKnot(intVec2(-1, 0)) &&
                hasKnot(intVec2(0, -1)) &&
                hasKnot(intVec2(0, 0)) &&
                !hasKnot(intVec2(-1, -2)) &&
                // TODO: Upper part should use tile 613?
                !hasKnot(intVec2(0, -2)) -> MetaTile.RockLeftSideInner
        hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(-1, -1)) &&
                !hasKnot(intVec2(0, -1)) &&
                !hasKnot(intVec2(0, 0)) -> MetaTile(632)
        hasKnot(intVec2(-1, -1)) &&
                hasKnot(intVec2(-1, 0)) &&
                !hasKnot(intVec2(-1, -2)) &&
                !hasKnot(intVec2(0, -2)) &&
                !hasKnot(intVec2(0, -1)) &&
                !hasKnot(intVec2(0, 0)) -> MetaTile(633)
        hasKnot(intVec2(-1, -1)) &&
                hasKnot(intVec2(-1, 0)) &&
                hasKnot(intVec2(-1, -2)) &&
                !hasKnot(intVec2(0, -1)) &&
                !hasKnot(intVec2(0, 0)) -> MetaTile(634)
        hasKnot(intVec2(-1, -1)) &&
                !hasKnot(intVec2(0, -1)) &&
                !hasKnot(intVec2(0, 0)) &&
                !hasKnot(intVec2(-1, 0)) -> MetaTile(635)
        hasKnot(intVec2(-1, -1)) &&
                hasKnot(intVec2(0, -1)) &&
                !hasKnot(intVec2(-1, 0)) &&
                // What's the difference between 615 and 631??
                !hasKnot(intVec2(0, 0)) -> MetaTile.RockRightSide
        hasKnot(intVec2(0, -1)) &&
                hasKnot(intVec2(-1, 0)) &&
                hasKnot(intVec2(-1, -1)) &&
                !hasKnot(intVec2(0, 0)) -> MetaTile(617)
        hasKnot(intVec2(0, 0)) &&
                hasKnot(intVec2(-1, 0)) &&
                hasKnot(intVec2(-1, -1)) &&
                !hasKnot(intVec2(0, -1)) -> MetaTile(618)
        getKnot(intVec2(-1, -1)) is RockPrototype &&
                getKnot(intVec2(0, -1)) is RockPrototype &&
                getKnot(intVec2(0, 0)) is RockPrototype &&
                !hasKnot(intVec2(-1, 0)) -> MetaTile.RockLowerLeftCorner
        getKnot(intVec2(-1, -1)) == null &&
                getKnot(intVec2(-1, 0)) is RockPrototype &&
                getKnot(intVec2(0, -1)) is RockPrototype &&
                getKnot(intVec2(0, 0)) is RockPrototype -> MetaTile.RockLowerRightCornerOuter
        getKnot(intVec2(-1, -2)) == null &&
                getKnot(intVec2(-1, -1)) is RockPrototype &&
                getKnot(intVec2(-1, 0)) is RockPrototype &&
                getKnot(intVec2(0, -1)) is RockPrototype &&
                getKnot(intVec2(0, 0)) is RockPrototype -> MetaTile.RockLowerRightCornerInner
        getKnot(intVec2(-1, -1)) is RockPrototype &&
                getKnot(intVec2(-1, 0)) is RockPrototype &&
                getKnot(intVec2(0, -1)) is RockPrototype &&
                getKnot(intVec2(0, 0)) is RockPrototype -> MetaTile(630)
        else -> null
    }
}
