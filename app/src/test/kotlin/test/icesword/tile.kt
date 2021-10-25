package test.icesword

import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.tileAtPoint
import icesword.tilesOverlappingArea
import kotlin.test.Test
import kotlin.test.expect

private class TileTest {
    @Test
    fun testTileAtPoint1() {
        expect(IntVec2(0, 0)) { tileAtPoint(IntVec2(0, 0)) }
    }

    @Test
    fun testTileAtPoint2() {
        expect(IntVec2(0, 0)) { tileAtPoint(IntVec2(63, 63)) }
    }

    @Test
    fun testTileAtPoint3() {
        expect(IntVec2(1, 0)) { tileAtPoint(IntVec2(64, 0)) }
    }

    @Test
    fun testTileAtPoint4() {
        expect(IntVec2(1, 1)) { tileAtPoint(IntVec2(64, 64)) }
    }

    @Test
    fun testTileAtPoint5() {
        expect(IntVec2(-1, 0)) { tileAtPoint(IntVec2(-1, 0)) }
    }

    @Test
    fun testTileAtPoint6() {
        expect(IntVec2(0, -1)) { tileAtPoint(IntVec2(0, -1)) }
    }

    @Test
    fun testTileAtPoint7() {
        expect(IntVec2(-1, 0)) { tileAtPoint(IntVec2(-63, 0)) }
    }

    @Test
    fun testTileAtPoint8() {
        expect(IntVec2(-1, 0)) { tileAtPoint(IntVec2(-64, 0)) }
    }

    @Test
    fun testTileAtPoint9() {
        expect(IntVec2(-2, 0)) { tileAtPoint(IntVec2(-65, 0)) }
    }

    @Test
    fun testTilesInArea1() {
        expect(
            expected = emptySet(),
        ) {
            tilesOverlappingArea(IntRect.ZERO).toSet()
        }
    }

    @Test
    fun testTilesInArea2() {
        expect(
            expected = setOf(IntVec2.ZERO),
        ) {
            tilesOverlappingArea(
                worldArea = IntRect(
                    position = IntVec2(16, 16),
                    IntSize(32, 32)
                )
            ).toSet()
        }
    }

    @Test
    fun testTilesInArea3() {
        expect(
            expected = setOf(
                IntVec2(-1, -1),
                IntVec2(0, -1),
                IntVec2(-1, 0),
                IntVec2(0, 0),
            ),
        ) {
            tilesOverlappingArea(
                worldArea = IntRect(
                    position = IntVec2(-16, -16),
                    IntSize(32, 32)
                )
            ).toSet()
        }
    }

    @Test
    fun testTilesInArea4() {
        expect(
            expected = setOf(IntVec2.ZERO),
        ) {
            tilesOverlappingArea(
                worldArea = IntRect(
                    position = IntVec2.ZERO,
                    IntSize(64, 64)
                )
            ).toSet()
        }
    }

    @Test
    fun testTilesInArea5() {
        expect(
            expected = setOf(
                IntVec2(0, 0),
                IntVec2(0, 1),
                IntVec2(0, 2),
                IntVec2(1, 0),
                IntVec2(1, 1),
                IntVec2(1, 2),
                IntVec2(2, 0),
                IntVec2(2, 1),
                IntVec2(2, 2),
            ),
        ) {
            tilesOverlappingArea(
                worldArea = IntRect(
                    position = IntVec2(32, 32),
                    IntSize(128, 128)
                )
            ).toSet()
        }
    }
}
