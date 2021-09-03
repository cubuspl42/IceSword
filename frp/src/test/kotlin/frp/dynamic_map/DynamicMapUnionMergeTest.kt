package frp.dynamic_map

import icesword.frp.*
import icesword.frp.dynamic_map.DynamicMapUnionMerge
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamicMapUnionMergeTest {
    @Test
    fun testInitialContent() {
        val map1 = DynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33,
            )
        )

        val map2 = DynamicMap.of(
            mapOf(
                "b" to 22,
                "c" to 44,
                "d" to 55,
            )
        )

        val map3 = DynamicMap.of(
            mapOf(
                "c" to 55,
                "d" to 66,
                "e" to 77,
            )
        )

        val maps = DynamicSet.of(
            setOf(map1, map2, map3)
        )

        val result = DynamicMapUnionMerge(maps, merge = { it.sum() })

        result.subscribe { }

        val initialContent: Map<String, Int> = result.sample()

        assertEquals(
            initialContent,
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33 + 44 + 55,
                "d" to 55 + 66,
                "e" to 77,
            ),
        )
    }

    @Test
    fun testInnerMapAdd1() {
        // Given

        val map1 = MutableDynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33,
            )
        )

        val map2 = MutableDynamicMap.of(
            mapOf(
                "b" to 22,
                "c" to 44,
                "d" to 55,
            )
        )

        val map3 = MutableDynamicMap.of(
            mapOf(
                "c" to 55,
                "d" to 66,
                "e" to 77,
            )
        )

        val maps = DynamicSet.of(
            setOf(map1, map2, map3)
        )

        // When inner map adds a key not present in other maps

        val changes = mutableListOf<MapChange<String, Int>>()

        val result = DynamicMapUnionMerge(maps, merge = { it.sum() })

        result.subscribe(changes::add)

        map3.put("f", 88)

        // Then an "added" change should be emitted

        assertEquals(
            listOf(
                MapChange(
                    added = mapOf("f" to 88),
                    updated = emptyMap(),
                    removedEntries = emptyMap(),
                ),
            ),
            changes,
        )

        assertEquals(
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33 + 44 + 55,
                "d" to 55 + 66,
                "e" to 77,
                "f" to 88,
            ),
            result.sample(),
        )
    }


    @Test
    fun testInnerMapAdd2() {
        // Given

        val map1 = MutableDynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33,
            )
        )

        val map2 = MutableDynamicMap.of(
            mapOf(
                "b" to 22,
                "c" to 44,
                "d" to 55,
            )
        )

        val map3 = MutableDynamicMap.of(
            mapOf(
                "c" to 55,
                "d" to 66,
                "e" to 77,
            )
        )

        val maps = DynamicSet.of(
            setOf(map1, map2, map3)
        )

        // When inner map adds a key present in other maps

        val changes = mutableListOf<MapChange<String, Int>>()

        val result = DynamicMapUnionMerge(maps, merge = { it.sum() })

        result.subscribe(changes::add)

        map1.put("d", 44)

        // Then an "updated" change should be emitted

        assertEquals(
            listOf(
                MapChange(
                    added = emptyMap(),
                    updated = mapOf("d" to 44 + 55 + 66),
                    removedEntries = emptyMap(),
                ),
            ),
            changes,
        )

        assertEquals(
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33 + 44 + 55,
                "d" to 44 + 55 + 66,
                "e" to 77,
            ),
            result.sample(),
        )
    }

    @Test
    fun testInnerMapAdd3() {
        // Given

        val map1 = MutableDynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33,
            )
        )

        val map2 = MutableDynamicMap.of(
            mapOf(
                "b" to 22,
                "c" to 44,
                "d" to 55,
            )
        )

        val map3 = MutableDynamicMap.of(
            mapOf(
                "c" to 55,
                "d" to 66,
                "e" to 77,
            )
        )

        val maps = DynamicSet.of(
            setOf(map1, map2, map3)
        )

        // When inner map adds an entry present in other maps

        val changes = mutableListOf<MapChange<String, Int>>()

        val result = DynamicMapUnionMerge(maps, merge = { it.sum() })

        result.subscribe(changes::add)

        map2.put("a", 11)

        // Then no change should be emitted

        assertEquals(
            listOf(
                MapChange(
                    added = emptyMap(),
                    updated = emptyMap(),
                    removedEntries = emptyMap(),
                ),
            ),
            changes,
        )

        assertEquals(
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33 + 44 + 55,
                "d" to 55 + 66,
                "e" to 77,
            ),
            result.sample(),
        )
    }

    @Test
    fun testInnerMapUpdate1() {
        // Given

        val map1 = MutableDynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33,
            )
        )

        val map2 = MutableDynamicMap.of(
            mapOf(
                "b" to 22,
                "c" to 44,
                "d" to 55,
            )
        )

        val map3 = MutableDynamicMap.of(
            mapOf(
                "c" to 55,
                "d" to 66,
                "e" to 77,
            )
        )

        val maps = DynamicSet.of(
            setOf(map1, map2, map3)
        )

        // When inner map updates on a key not present in other maps

        val changes = mutableListOf<MapChange<String, Int>>()

        val result = DynamicMapUnionMerge(maps, merge = { it.sum() })

        result.subscribe(changes::add)

        map3.put("e", 78)

        // Then an "updated" change should be emitted

        assertEquals(
            listOf(
                MapChange(
                    added = emptyMap(),
                    updated = mapOf("e" to 78),
                    removedEntries = emptyMap(),
                ),
            ),
            changes,
        )

        assertEquals(
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33 + 44 + 55,
                "d" to 55 + 66,
                "e" to 78,
            ),
            result.sample(),
        )
    }

    @Test
    fun testInnerMapUpdate2() {
        // Given

        val map1 = MutableDynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33,
            )
        )

        val map2 = MutableDynamicMap.of(
            mapOf(
                "b" to 22,
                "c" to 44,
                "d" to 55,
            )
        )

        val map3 = MutableDynamicMap.of(
            mapOf(
                "c" to 55,
                "d" to 66,
                "e" to 77,
            )
        )

        val maps = DynamicSet.of(
            setOf(map1, map2, map3)
        )

        // When inner map updates on a key present in other maps

        val changes = mutableListOf<MapChange<String, Int>>()

        val result = DynamicMapUnionMerge(maps, merge = { it.sum() })

        result.subscribe(changes::add)

        map2.put("c", 45)

        // Then an "updated" change should be emitted

        assertEquals(
            listOf(
                MapChange(
                    added = emptyMap(),
                    updated = mapOf("c" to 33 + 45 + 55),
                    removedEntries = emptyMap(),
                ),
            ),
            changes,
        )

        assertEquals(
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33 + 45 + 55,
                "d" to 55 + 66,
                "e" to 77,
            ),
            result.sample(),
        )
    }

    @Test
    fun testInnerMapUpdate3() {
        // Given

        val map1 = MutableDynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33,
            )
        )

        val map2 = MutableDynamicMap.of(
            mapOf(
                "b" to 22,
                "c" to 44,
                "d" to 55,
            )
        )

        val map3 = MutableDynamicMap.of(
            mapOf(
                "c" to 55,
                "d" to 66,
                "e" to 77,
            )
        )

        val maps = DynamicSet.of(
            setOf(map1, map2, map3)
        )

        // When inner map updates to an entry present in other maps

        val changes = mutableListOf<MapChange<String, Int>>()

        val result = DynamicMapUnionMerge(maps, merge = { it.sum() })

        result.subscribe(changes::add)

        map3.put("d", 55)

        // Then no change should be emitted

        assertEquals(
            listOf(
                MapChange(
                    added = emptyMap(),
                    updated = mapOf("d" to 55),
                    removedEntries = emptyMap(),
                ),
            ),
            changes,
        )

        assertEquals(
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33 + 44 + 55,
                "d" to 55,
                "e" to 77,
            ),
            result.sample(),
        )
    }

    @Test
    fun testInnerMapRemoved1() {
        // Given

        val map1 = MutableDynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33,
            )
        )

        val map2 = MutableDynamicMap.of(
            mapOf(
                "b" to 22,
                "c" to 44,
                "d" to 55,
            )
        )

        val map3 = MutableDynamicMap.of(
            mapOf(
                "c" to 55,
                "d" to 66,
                "e" to 77,
            )
        )

        val maps = DynamicSet.of(
            setOf(map1, map2, map3)
        )

        // When inner map removes a key not present in other maps

        val changes = mutableListOf<MapChange<String, Int>>()

        val result = DynamicMapUnionMerge(maps, merge = { it.sum() })

        result.subscribe(changes::add)

        map1.remove("a")

        // Then a "removed" change should be emitted

        assertEquals(
            listOf(
                MapChange(
                    added = emptyMap(),
                    updated = emptyMap(),
                    removedEntries = mapOf("a" to 11),
                ),
            ),
            changes,
        )

        assertEquals(
            mapOf(
                "b" to 22,
                "c" to 33 + 44 + 55,
                "d" to 55 + 66,
                "e" to 77,
            ),
            result.sample(),
        )
    }

    @Test
    fun testInnerMapRemoved2() {
        // Given

        val map1 = MutableDynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33,
            )
        )

        val map2 = MutableDynamicMap.of(
            mapOf(
                "b" to 22,
                "c" to 44,
                "d" to 55,
            )
        )

        val map3 = MutableDynamicMap.of(
            mapOf(
                "c" to 55,
                "d" to 66,
                "e" to 77,
            )
        )

        val maps = DynamicSet.of(
            setOf(map1, map2, map3)
        )

        // When inner map removes a key present in other maps

        val changes = mutableListOf<MapChange<String, Int>>()

        val result = DynamicMapUnionMerge(maps, merge = { it.sum() })

        result.subscribe(changes::add)

        map2.remove("c")

        // Then an "updated" change should be emitted

        assertEquals(
            listOf(
                MapChange(
                    added = emptyMap(),
                    updated = mapOf("c" to 33 + 55),
                    removedEntries = emptyMap(),
                ),
            ),
            changes,
        )

        assertEquals(
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33 + 55,
                "d" to 55 + 66,
                "e" to 77,
            ),
            result.sample(),
        )
    }

    @Test
    fun testInnerMapRemoved3() {
        // Given

        val map1 = MutableDynamicMap.of(
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33,
            )
        )

        val map2 = MutableDynamicMap.of(
            mapOf(
                "b" to 22,
                "c" to 44,
                "d" to 55,
            )
        )

        val map3 = MutableDynamicMap.of(
            mapOf(
                "c" to 55,
                "d" to 66,
                "e" to 77,
            )
        )

        val maps = DynamicSet.of(
            setOf(map1, map2, map3)
        )

        // When inner map removes an entry present in other maps

        val changes = mutableListOf<MapChange<String, Int>>()

        val result = DynamicMapUnionMerge(maps, merge = { it.sum() })

        result.subscribe(changes::add)

        map1.remove("b")

        // Then no change should be emitted

        assertEquals(
            listOf(
                MapChange(
                    added = emptyMap(),
                    updated = emptyMap(),
                    removedEntries = emptyMap(),
                ),
            ),
            changes,
        )

        assertEquals(
            mapOf(
                "a" to 11,
                "b" to 22,
                "c" to 33 + 44 + 55,
                "d" to 55 + 66,
                "e" to 77,
            ),
            result.sample(),
        )
    }
}
