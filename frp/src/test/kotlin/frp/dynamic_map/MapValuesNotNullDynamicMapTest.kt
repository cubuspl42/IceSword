package frp.dynamic_map

import icesword.frp.*
import icesword.frp.dynamic_map.DynamicMapUnionMerge
import icesword.frp.dynamic_map.MapValuesNotNullDynamicMap_
import kotlin.test.Test
import kotlin.test.assertEquals

class MapValuesNotNullDynamicMapTest {
    @Test
    fun testInitialContent() {
        val source = DynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to -22,
                "c" to 33,
                "d" to -44,
            )
        )

        val out = MapValuesNotNullDynamicMap_(
            source,
            transform = { (_, v) ->
                if (v > 0) v.toString()
                else null
            },
            tag = "",
        )

        out.subscribe { }

        assertEquals(
            expected = mapOf(
                "a" to "11",
                "c" to "33",
            ),
            actual = out.sample(),
        )
    }

    @Test
    fun testAdded1() {
        // Given
        val source = MutableDynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to -22,
                "c" to 33,
                "d" to -44,
            )
        )

        val out = MapValuesNotNullDynamicMap_(
            source,
            transform = { (_, v) ->
                if (v > 0) v.toString()
                else null
            },
            tag = "",
        )

        // When source adds entry that is not filtered out
        val changes = mutableListOf<MapChange<String, String>>()

        out.subscribe(changes::add)

        source.put("e", 55)

        // Then

        assertEquals(
            listOf(
                MapChange(
                    added = mapOf("e" to "55"),
                    updated = emptyMap(),
                    removedEntries = emptyMap(),
                ),
            ),
            changes,
        )

        assertEquals(
            expected = mapOf(
                "a" to "11",
                "c" to "33",
                "e" to "55",
            ),
            actual = out.sample(),
        )
    }

    @Test
    fun testAdded2() {
        // Given
        val source = MutableDynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to -22,
                "c" to 33,
                "d" to -44,
            )
        )

        val out = MapValuesNotNullDynamicMap_(
            source,
            transform = { (_, v) ->
                if (v > 0) v.toString()
                else null
            },
            tag = "",
        )

        // When source adds entry that is filtered out
        val changes = mutableListOf<MapChange<String, String>>()

        out.subscribe(changes::add)

        source.put("e", -55)

        // Then

        assertEquals(
            emptyList(),
            changes,
        )

        assertEquals(
            expected = mapOf(
                "a" to "11",
                "c" to "33",
            ),
            actual = out.sample(),
        )
    }

    @Test
    fun testUpdated1() {
        // Given
        val source = MutableDynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to -22,
                "c" to 33,
                "d" to -44,
            )
        )

        val out = MapValuesNotNullDynamicMap_(
            source,
            transform = { (_, v) ->
                if (v > 0) v.toString()
                else null
            },
            tag = "",
        )

        // When source updates entry that wasn't filtered out before and still isn't
        val changes = mutableListOf<MapChange<String, String>>()

        out.subscribe(changes::add)

        source.put("c", 34)

        // Then

        assertEquals(
            listOf(
                MapChange(
                    added = emptyMap(),
                    updated = mapOf("c" to "34"),
                    removedEntries = emptyMap(),
                ),
            ),
            changes,
        )

        assertEquals(
            expected = mapOf(
                "a" to "11",
                "c" to "34",
            ),
            actual = out.sample(),
        )
    }


    @Test
    fun testUpdated2() {
        // Given
        val source = MutableDynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to -22,
                "c" to 33,
                "d" to -44,
            )
        )

        val out = MapValuesNotNullDynamicMap_(
            source,
            transform = { (_, v) ->
                if (v > 0) v.toString()
                else null
            },
            tag = "",
        )

        // When source updates entry that was filtered out before but isn't anymore
        val changes = mutableListOf<MapChange<String, String>>()

        out.subscribe(changes::add)

        source.put("d", 44)

        // Then

        assertEquals(
            listOf(
                MapChange(
                    added = mapOf("d" to "44"),
                    updated = emptyMap(),
                    removedEntries = emptyMap(),
                ),
            ),
            changes,
        )

        assertEquals(
            expected = mapOf(
                "a" to "11",
                "c" to "33",
                "d" to "44",
            ),
            actual = out.sample(),
        )
    }

    @Test
    fun testUpdated3() {
        // Given
        val source = MutableDynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to -22,
                "c" to 33,
                "d" to -44,
            )
        )

        val out = MapValuesNotNullDynamicMap_(
            source,
            transform = { (_, v) ->
                if (v > 0) v.toString()
                else null
            },
            tag = "",
        )

        // When source updates entry that wasn't filtered out before but now is
        val changes = mutableListOf<MapChange<String, String>>()

        out.subscribe(changes::add)

        source.put("a", -11)

        // Then

        assertEquals(
            listOf(
                MapChange(
                    added = emptyMap(),
                    updated = emptyMap(),
                    removedEntries = mapOf("a" to "11"),
                ),
            ),
            changes,
        )

        assertEquals(
            expected = mapOf(
                "c" to "33",
            ),
            actual = out.sample(),
        )
    }

    @Test
    fun testUpdated4() {
        // Given
        val source = MutableDynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to -22,
                "c" to 33,
                "d" to -44,
            )
        )

        val out = MapValuesNotNullDynamicMap_(
            source,
            transform = { (_, v) ->
                if (v > 0) v.toString()
                else null
            },
            tag = "",
        )

        // When source updates entry that was filtered out and still is
        val changes = mutableListOf<MapChange<String, String>>()

        out.subscribe(changes::add)

        source.put("b", -23)

        // Then

        assertEquals(
            emptyList(),
            changes,
        )

        assertEquals(
            expected = mapOf(
                "a" to "11",
                "c" to "33",
            ),
            actual = out.sample(),
        )
    }

    @Test
    fun testRemoved1() {
        // Given
        val source = MutableDynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to -22,
                "c" to 33,
                "d" to -44,
            )
        )

        val out = MapValuesNotNullDynamicMap_(
            source,
            transform = { (_, v) ->
                if (v > 0) v.toString()
                else null
            },
            tag = "",
        )

        // When source removes entry that wasn't filtered out
        val changes = mutableListOf<MapChange<String, String>>()

        out.subscribe(changes::add)

        source.remove("c")

        // Then
        assertEquals(
            listOf(
                MapChange(
                    added = emptyMap(),
                    updated = emptyMap(),
                    removedEntries = mapOf("c" to "33"),
                ),
            ),
            changes,
        )

        assertEquals(
            expected = mapOf(
                "a" to "11",
            ),
            actual = out.sample(),
        )
    }

    @Test
    fun testRemoved2() {
        // Given
        val source = MutableDynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to -22,
                "c" to 33,
                "d" to -44,
            )
        )

        val out = MapValuesNotNullDynamicMap_(
            source,
            transform = { (_, v) ->
                if (v > 0) v.toString()
                else null
            },
            tag = "",
        )

        // When source removes entry that was filtered out

        val changes = mutableListOf<MapChange<String, String>>()

        out.subscribe(changes::add)

        source.remove("d")

        // Then

        assertEquals(
            emptyList(),
            changes,
        )

        assertEquals(
            expected = mapOf(
                "a" to "11",
                "c" to "33",
            ),
            actual = out.sample(),
        )
    }
}
