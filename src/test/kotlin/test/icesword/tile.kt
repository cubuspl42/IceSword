package test.icesword

import icesword.geometry.IntVec2
import icesword.tileAtPoint
import kotlin.test.Test
import kotlin.test.expect

private class Test {
    @Test
    fun testTileAtPoint() {
        expect(IntVec2(0, 0)) { tileAtPoint(IntVec2(0, 0)) }

        expect(IntVec2(63, 63)) { tileAtPoint(IntVec2(0, 0)) }

        expect(IntVec2(64, 0)) { tileAtPoint(IntVec2(1, 0)) }

        expect(IntVec2(64, 64)) { tileAtPoint(IntVec2(1, 1)) }

        expect(IntVec2(-1, 0)) { tileAtPoint(IntVec2(-1, 0)) }

        expect(IntVec2(0, -1)) { tileAtPoint(IntVec2(0, -1)) }

        expect(IntVec2(-63, 0)) { tileAtPoint(IntVec2(-1, 0)) }

        expect(IntVec2(-64, 0)) { tileAtPoint(IntVec2(-1, 0)) }

        expect(IntVec2(-65, 0)) { tileAtPoint(IntVec2(-2, 0)) }
    }
}
