package frp.dynamic_set

import icesword.frp.MutableDynamicSet
import icesword.frp.SetChange
import icesword.frp.distinctMap
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals

class DistinctMapTest {
    @Test
    fun testInitialContent() {
        // Given

        val source = MutableDynamicSet.of(setOf(1, 2, 3, 5))

        // When

        val result = source.distinctMap { it * 2 }

        result.changes.subscribe { }

        // Then

        assertEquals(
            expected = setOf(
                2, 4, 6, 10,
            ),
            actual = result.volatileContentView,
        )
    }

    @Test
    fun testInitialContentDuplicates() {
        // Given

        val source = MutableDynamicSet.of(setOf(1.1, 1.2, 2.4, 2.5, 3.1))

        // When

        val result = source.distinctMap { floor(it) }

        result.changes.subscribe { }

        // Then

        assertEquals(
            expected = setOf(
                1.0, 2.0, 3.0,
            ),
            actual = result.volatileContentView,
        )
    }

    @Test
    fun testAdded() {
        // Given

        val source = MutableDynamicSet.of(setOf(1, 2, 3, 5))

        // When

        val result = source.distinctMap { it * 3 }

        val changes = mutableListOf<SetChange<Int>>()

        result.changes.subscribe(changes::add)

        // Then

        source.add(6)

        assertEquals(
            expected = listOf(
                SetChange(
                    added = setOf(18),
                    removed = emptySet(),
                ),
            ),
            actual = changes,
        )

        assertEquals(
            expected = setOf(
                3, 6, 9, 15, 18,
            ),
            actual = result.volatileContentView,
        )
    }

    @Test
    fun testRemoved() {
        // Given

        val source = MutableDynamicSet.of(setOf(2, 3, 4, 5))

        // When

        val result = source.distinctMap { it * 2 }

        val changes = mutableListOf<SetChange<Int>>()

        result.changes.subscribe(changes::add)

        // Then

        source.remove(4)

        assertEquals(
            expected = listOf(
                SetChange(
                    added = emptySet(),
                    removed = setOf(8),
                ),
            ),
            actual = changes,
        )

        assertEquals(
            expected = setOf(
                4, 6, 10,
            ),
            actual = result.volatileContentView,
        )
    }


    @Test
    fun testRemovedDuplicates() {
        // Given

        val source = MutableDynamicSet.of(setOf(3.8, 3.9, 4.7, 5.8))

        // When

        val result = source.distinctMap { it.roundToInt() }

        val changes = mutableListOf<SetChange<Int>>()

        result.changes.subscribe(changes::add)

        // Then

        source.remove(3.8)

        assertEquals(
            expected = emptyList(),
            actual = changes,
        )

        assertEquals(
            expected = setOf(
                4, 5, 6,
            ),
            actual = result.volatileContentView,
        )

        source.remove(3.9)

        assertEquals(
            expected = listOf(
                SetChange(
                    added = emptySet(),
                    removed = setOf(4),
                ),
            ),
            actual = changes,
        )

        assertEquals(
            expected = setOf(
                5, 6,
            ),
            actual = result.volatileContentView,
        )
    }
}
