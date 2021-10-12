package frp.dynamic_map

import icesword.frp.*
import icesword.frp.dynamic_map.ProjectDynamicMap
import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectDynamicMapTest {
    private data class K(val a: Int)

    private data class K2(val a: Int)

    private data class V(val a: Int)

    private data class V2(val a: Int)

    companion object {
        private fun valueChange(oldValue: V2, newValue: V2): V2 = newValue

        private fun projectDynamicMap(source: MutableDynamicMap<K, V>) =
            ProjectDynamicMap(
                source = source,
                projectKey = { k ->
                    setOf(
                        K2(k.a - 2),
                        K2(k.a - 1),
                        K2(k.a),
                        K2(k.a + 1),
                        K2(k.a + 2),
                    )
                },
                buildValue = { k2, map ->
                    val a = map[K(k2.a - 2)]?.a ?: 0;
                    val b = map[K(k2.a - 1)]?.a ?: 0;
                    val c = map[K(k2.a)]?.a ?: 0;
                    val d = map[K(k2.a + 1)]?.a ?: 0;
                    val e = map[K(k2.a + 2)]?.a ?: 0;

                    V2(a + b + c + d + e);
                }
            )
    }

    @Test
    fun testInitialContent() {
        val source = MutableDynamicMap(
            mapOf(
                K(5) to V(50),
                K(6) to V(60),
                K(7) to V(70),
                K(8) to V(80),
                K(10) to V(100),
                K(12) to V(120),
                K(13) to V(130),
                K(15) to V(150),
            )
        )

        val result = projectDynamicMap(source)

        result.changes.subscribe {}

        assertEquals(
            expected = mapOf(
                K2(3) to V2(50),
                K2(4) to V2(50 + 60),
                K2(5) to V2(50 + 60 + 70),
                K2(6) to V2(50 + 60 + 70 + 80),
                K2(7) to V2(50 + 60 + 70 + 80),

                K2(8) to V2(60 + 70 + 80 + 100),
                K2(9) to V2(70 + 80 + 100),
                K2(10) to V2(80 + 100 + 120),
                K2(11) to V2(100 + 120 + 130),
                K2(12) to V2(100 + 120 + 130),

                K2(13) to V2(120 + 130 + 150),
                K2(14) to V2(120 + 130 + 150),
                K2(15) to V2(130 + 150),
                K2(16) to V2(150),
                K2(17) to V2(150),
            ),
            actual = result.volatileContentView,
        )
    }

    @Test
    fun testSourceUpdatedEntry() {
        val source = MutableDynamicMap(
            mapOf(
                K(5) to V(50),
                K(6) to V(60),
                K(7) to V(70),
                K(8) to V(80),
                K(10) to V(100),
                K(12) to V(120),
                K(13) to V(130),
                K(15) to V(150),
            )
        )

        val result = projectDynamicMap(source)

        val changes = mutableListOf<MapChange<K2, V2>>()

        var observedVolatileContent: Map<K2, V2>? = null

        result.changes.subscribe {
            observedVolatileContent = result.volatileContentView.toMap()
            changes.add(it)
        }

        source.put(K(8), V(81))

        assertEquals(
            expected = mapOf(
                K2(3) to V2(50),
                K2(4) to V2(50 + 60),
                K2(5) to V2(50 + 60 + 70),
                K2(6) to V2(50 + 60 + 70 + 81),
                K2(7) to V2(50 + 60 + 70 + 81),

                K2(8) to V2(60 + 70 + 81 + 100),
                K2(9) to V2(70 + 81 + 100),
                K2(10) to V2(81 + 100 + 120),
                K2(11) to V2(100 + 120 + 130),
                K2(12) to V2(100 + 120 + 130),

                K2(13) to V2(120 + 130 + 150),
                K2(14) to V2(120 + 130 + 150),
                K2(15) to V2(130 + 150),
                K2(16) to V2(150),
                K2(17) to V2(150),
            ),
            actual = observedVolatileContent,
        )

        assertEquals(
            expected = listOf(
                MapChange(
                    added = emptyMap(),
                    updated = mapOf(
                        K2(6) to valueChange(
                            oldValue = V2(50 + 60 + 70 + 80),
                            newValue = V2(50 + 60 + 70 + 81),
                        ),
                        K2(7) to valueChange(
                            oldValue = V2(50 + 60 + 70 + 80),
                            newValue = V2(50 + 60 + 70 + 81),
                        ),
                        K2(8) to valueChange(
                            oldValue = V2(60 + 70 + 80 + 100),
                            newValue = V2(60 + 70 + 81 + 100),
                        ),
                        K2(9) to valueChange(
                            oldValue = V2(70 + 80 + 100),
                            newValue = V2(70 + 81 + 100),
                        ),
                        K2(10) to valueChange(
                            oldValue = V2(80 + 100 + 120),
                            newValue = V2(81 + 100 + 120),
                        ),
                    ),
                    removedEntries = emptyMap(),
                ),
            ),
            actual = changes,
        )
    }

    @Test
    fun testSourceAddedEntryFullyProjected() {
        // source added entry (fully projected before)

        val source = MutableDynamicMap(
            mapOf(
                K(5) to V(50),
                K(6) to V(60),
                K(7) to V(70),
                K(8) to V(80),
                K(10) to V(100),
                K(12) to V(120),
                K(13) to V(130),
                K(15) to V(150),
            )
        )

        val result = projectDynamicMap(source)

        val changes = mutableListOf<MapChange<K2, V2>>()

        result.changes.subscribe(changes::add)

        source.put(K(9), V(90))

        assertEquals(
            expected = listOf(
                MapChange(
                    added = emptyMap(),
                    updated = mapOf(
                        K2(7) to valueChange(
                            oldValue = V2(50 + 60 + 70 + 80),
                            newValue = V2(50 + 60 + 70 + 80 + 90),
                        ),
                        K2(8) to valueChange(
                            oldValue = V2(60 + 70 + 80 + 100),
                            newValue = V2(60 + 70 + 80 + 90 + 100),
                        ),
                        K2(9) to valueChange(
                            oldValue = V2(70 + 80 + 100),
                            newValue = V2(70 + 80 + 90 + 100),
                        ),
                        K2(10) to valueChange(
                            oldValue = V2(80 + 100 + 120),
                            newValue = V2(80 + 90 + 100 + 120),
                        ),
                        K2(11) to valueChange(
                            oldValue = V2(100 + 120 + 130),
                            newValue = V2(90 + 100 + 120 + 130),
                        ),
                    ),
                    removedEntries = emptyMap(),
                )
            ),
            actual = changes,
        )
    }

    @Test
    fun testSourceAddedEntryPartiallyProjected() {
        // source added entry (partially projected before)

        val source = MutableDynamicMap(
            mapOf(
                K(5) to V(50),
                K(6) to V(60),
                K(7) to V(70),
                K(8) to V(80),
                K(10) to V(100),
                K(12) to V(120),
                K(13) to V(130),
                K(15) to V(150),
            )
        )

        val result = projectDynamicMap(source)

        val changes = mutableListOf<MapChange<K2, V2>>()

        result.changes.subscribe(changes::add)

        source.put(K(17), V(170))

        val expectedChange = MapChange(
            added = mapOf(
                K2(18) to V2(170),
                K2(19) to V2(170)
            ),
            updated = mapOf(
                K2(15) to valueChange(
                    oldValue = V2(130 + 150),
                    newValue = V2(130 + 150 + 170),
                ),
                K2(16) to valueChange(
                    oldValue = V2(150),
                    newValue = V2(150 + 170),
                ),
                K2(17) to valueChange(
                    oldValue = V2(150),
                    newValue = V2(150 + 170),
                ),
            ),
            removedEntries = emptyMap(),
        )

        assertEquals(
            expected = listOf(
                expectedChange
            ),
            actual = changes,
        )
    }

    @Test
    fun testSourceRemovedEntryFullyCoProjected() {
        // source removed entry (fully co-projected)

        val source = MutableDynamicMap(
            mapOf(
                K(5) to V(50),
                K(6) to V(60),
                K(7) to V(70),
                K(8) to V(80),
                K(10) to V(100),
                K(12) to V(120),
                K(13) to V(130),
                K(15) to V(150),
            )
        )

        val result = projectDynamicMap(source)

        val changes = mutableListOf<MapChange<K2, V2>>()

        result.changes.subscribe(changes::add)

        source.remove(K(8))

        assertEquals(
            expected = listOf(
                MapChange(
                    added = emptyMap(),
                    updated = mapOf(
                        K2(6) to valueChange(
                            oldValue = V2(50 + 60 + 70 + 80),
                            newValue = V2(50 + 60 + 70),
                        ),
                        K2(7) to valueChange(
                            oldValue = V2(50 + 60 + 70 + 80),
                            newValue = V2(50 + 60 + 70),
                        ),
                        K2(8) to valueChange(
                            oldValue = V2(60 + 70 + 80 + 100),
                            newValue = V2(60 + 70 + 100),
                        ),
                        K2(9) to valueChange(
                            oldValue = V2(70 + 80 + 100),
                            newValue = V2(70 + 100),
                        ),
                        K2(10) to valueChange(
                            oldValue = V2(80 + 100 + 120),
                            newValue = V2(100 + 120),
                        ),
                    ),
                    removedEntries = emptyMap(),
                )
            ),
            actual = changes,
        )
    }

    @Test
    fun testSourceRemovedEntryPartiallyCoProjected() {
        // source removed entry (partially co-projected)

        val source = MutableDynamicMap(
            mapOf(
                K(5) to V(50),
                K(6) to V(60),
                K(7) to V(70),
                K(8) to V(80),
                K(10) to V(100),
                K(12) to V(120),
                K(13) to V(130),
                K(15) to V(150),
            )
        )

        val result = projectDynamicMap(source)

        val changes = mutableListOf<MapChange<K2, V2>>()

        result.changes.subscribe(changes::add)

        source.remove(K(5))

        assertEquals(
            expected = listOf(
                MapChange(
                    added = emptyMap(),
                    updated = mapOf(
                        K2(4) to valueChange(
                            oldValue = V2(50 + 60),
                            newValue = V2(60),
                        ),
                        K2(5) to valueChange(
                            oldValue = V2(50 + 60 + 70),
                            newValue = V2(60 + 70),
                        ),
                        K2(6) to valueChange(
                            oldValue = V2(50 + 60 + 70 + 80),
                            newValue = V2(60 + 70 + 80),
                        ),
                        K2(7) to valueChange(
                            oldValue = V2(50 + 60 + 70 + 80),
                            newValue = V2(60 + 70 + 80),
                        ),
                    ),
                    removedEntries = mapOf(
                        K2(3) to V2(50),
                    ),
                )
            ),
            actual = changes,
        )
    }

    @Test
    fun testSourceRemovedEntryNotCoProjected() {
        // source removed entry (not co-projected)

        val source = MutableDynamicMap(
            mapOf(
                K(4) to V(40),
                K(5) to V(50),
                K(10) to V(100),
                K(15) to V(150),
                K(16) to V(160),
            )
        )

        val result = projectDynamicMap(source)

        val changes = mutableListOf<MapChange<K2, V2>>()

        result.changes.subscribe(changes::add)

        source.remove(K(10))

        assertEquals(
            expected = listOf(
                MapChange(
                    added = emptyMap(),
                    updated = emptyMap(),
                    removedEntries = mapOf(
                        K2(8) to V2(100),
                        K2(9) to V2(100),
                        K2(10) to V2(100),
                        K2(11) to V2(100),
                        K2(12) to V2(100),
                    ),
                )
            ),
            actual = changes,
        )
    }


    @Test
    fun testSourceRemovedAllEntries() {
        // source removed all entries at once

        val source = MutableDynamicMap(
            mapOf(
                K(5) to V(50),
                K(6) to V(60),
            )
        )

        val result = projectDynamicMap(source)

        val changes = mutableListOf<MapChange<K2, V2>>()

        result.changes.subscribe(changes::add)

        source.applyChange(
            MapChange(
                added = emptyMap(),
                updated = emptyMap(),
                removedEntries = mapOf(
                    K(5) to V(50),
                    K(6) to V(60),
                ),
            )
        )

        assertEquals(
            expected = listOf(
                MapChange(
                    added = emptyMap(),
                    updated = emptyMap(),
                    removedEntries = mapOf(
                        K2(3) to V2(50),
                        K2(4) to V2(50 + 60),
                        K2(5) to V2(50 + 60),
                        K2(6) to V2(50 + 60),
                        K2(7) to V2(50 + 60),
                        K2(8) to V2(60),
                    ),
                )
            ),
            actual = changes,
        )
    }

    @Test
    fun testSourceAddedRemoved() {
        // source removed an entry and added a new one at adjacent key at once

        val source = MutableDynamicMap(
            mapOf(
                K(5) to V(50),
            )
        )

        val result = projectDynamicMap(source)

        val changes = mutableListOf<MapChange<K2, V2>>()

        result.changes.subscribe(changes::add)

        source.applyChange(
            MapChange(
                added = mapOf(
                    K(6) to V(60),
                ),
                updated = emptyMap(),
                removedEntries = mapOf(
                    K(5) to V(50),
                ),
            )
        )

        assertEquals(
            expected = listOf(
                MapChange(
                    added = mapOf(
                        K2(8) to V2(60),
                    ),
                    updated = mapOf(
                        K2(a = 4) to V2(a = 60),
                        K2(a = 5) to V2(a = 60),
                        K2(a = 6) to V2(a = 60),
                        K2(a = 7) to V2(a = 60)
                    ),
                    removedEntries = mapOf(
                        K2(3) to V2(50),
                    ),
                )
            ),
            actual = changes,
        )
    }
}
