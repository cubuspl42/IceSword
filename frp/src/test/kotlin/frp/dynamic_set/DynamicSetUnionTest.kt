package frp.dynamic_set

import icesword.frp.DynamicSet
import icesword.frp.MutableDynamicSet
import icesword.frp.SetChange
import icesword.frp.dynamic_set.DynamicSetUnion
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamicSetUnionTest {
    @Test
    fun testRemovedLast() {
        // Given

        val set1 = MutableDynamicSet(setOf(1, 2, 3, 5))
        val set2 = MutableDynamicSet(setOf(2, 3, 4))

        val sets = DynamicSet.of(setOf(set1, set2))

        // When one of the sets removes an element which is not present
        // in other sets...

        val result = DynamicSetUnion(sets)

        val changes = mutableListOf<SetChange<Int>>()

        result.changes.subscribe(changes::add)

        set1.remove(1)

        // Then

        assertEquals(
            expected = listOf(
                SetChange(
                    added = emptySet(),
                    removed = setOf(1),
                ),
            ),
            actual = changes,
        )

        assertEquals(
            expected = setOf(2, 3, 4, 5),
            actual = result.volatileContentView,
        )
    }
}
