package frp.dynamic_set

import icesword.frp.MutableDynamicSet
import icesword.frp.SetChange
import icesword.frp.Till
import icesword.frp.mapTillRemoved
import kotlin.test.Test
import kotlin.test.assertEquals

class MapTillRemovedDynamicSetTest {
    @Test
    fun testInitialContent() {
        // Given

        val source = MutableDynamicSet.of(setOf(1, 2, 3, 5))

        // When

        val result = source.mapTillRemoved(Till.never) { it, _ -> it * 2 }

        // Then

        assertEquals(
            expected = setOf(
                2, 4, 6, 10,
            ),
            actual = result.volatileContentView,
        )
    }

    @Test
    fun testAddedRemoved() {
        // Given

        val source = MutableDynamicSet.of(setOf(1, 2, 3, 5))

        // When

        val result = source.mapTillRemoved(Till.never) { it, _ -> it * 2 }

        val changes = mutableListOf<SetChange<Int>>()

        result.changes.subscribe(changes::add)

        // Then

        source.add(6)

        assertEquals(
            expected = listOf(
                SetChange(
                    added = setOf(12),
                    removed = emptySet(),
                ),
            ),
            actual = changes,
        )

        assertEquals(
            expected = setOf(
                2, 4, 6, 10, 12,
            ),
            actual = result.volatileContentView,
        )

        changes.clear()

        source.remove(6)

        assertEquals(
            expected = listOf(
                SetChange(
                    added = emptySet(),
                    removed = setOf(12),
                ),
            ),
            actual = changes,
        )

        assertEquals(
            expected = setOf(
                2, 4, 6, 10,
            ),
            actual = result.volatileContentView,
        )
    }

    @Test
    fun testRemovedAdded() {
        // Given

        val source = MutableDynamicSet.of(setOf(1, 2, 3, 5))

        // When

        val result = source.mapTillRemoved(Till.never) { it, _ -> it * 2 }

        val changes = mutableListOf<SetChange<Int>>()

        result.changes.subscribe(changes::add)

        // Then

        source.remove(5)

        assertEquals(
            expected = listOf(
                SetChange(
                    added = emptySet(),
                    removed = setOf(10),
                ),
            ),
            actual = changes,
        )

        assertEquals(
            expected = setOf(
                2, 4, 6,
            ),
            actual = result.volatileContentView,
        )

        changes.clear()

        source.add(5)

        assertEquals(
            expected = listOf(
                SetChange(
                    added = setOf(10),
                    removed = emptySet(),
                ),
            ),
            actual = changes,
        )

        assertEquals(
            expected = setOf(
                2, 4, 6, 10,
            ),
            actual = result.volatileContentView,
        )
    }
}
