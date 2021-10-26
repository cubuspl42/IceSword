package frp.dynamic_map

import icesword.collections.defaultMapFactory
import icesword.frp.*
import kotlin.test.Test
import kotlin.test.assertEquals

class UnionMergeDynamicMapTest {
    private data class K(val a: Int)

    private data class V(val a: Int)

    private data class R(val a: Int)

    companion object {
        private fun <V> valueChange(oldValue: V, newValue: V): V = newValue
    }

    @Test
    fun testInitialContent() {
        val maps = DynamicSet.of(
            setOf(
                DynamicMap.of(
                    mapOf(
                        K(1) to V(11),
                        K(2) to V(21),
                        K(3) to V(31),
                    )
                ),
                DynamicMap.of(
                    mapOf(
                        K(3) to V(32),
                        K(4) to V(42),
                        K(5) to V(52),
                    )
                ),
                DynamicMap.of(
                    mapOf(
                        K(2) to V(23),
                        K(3) to V(33),
                    )
                ),
            )
        )

        val result: DynamicMap<K, R> = DynamicMap.unionMerge_(
            through = defaultMapFactory(),
            maps = maps,
            merge = { elements -> R(elements.sumOf { it.a }) },
            tag = "result",
        )

        result.changes.subscribe {}

        assertEquals(
            expected = mapOf(
                K(1) to R(11),
                K(2) to R(21 + 23),
                K(3) to R(31 + 32 + 33),
                K(4) to R(42),
                K(5) to R(52),
            ),
            actual = result.volatileContentView,
        )
    }

    @Test
    fun testSetAdded() {
        // "maps" set added entry

        val maps = MutableDynamicSet.of(
            setOf(
                DynamicMap.of(
                    mapOf(
                        K(1) to V(11),
                        K(2) to V(21),
                        K(3) to V(31),
                    )
                ),
                DynamicMap.of(
                    mapOf(
                        K(3) to V(32),
                        K(4) to V(42),
                        K(5) to V(52),
                    )
                ),
            )
        )

        val result: DynamicMap<K, R> = DynamicMap.unionMerge_(
            through = defaultMapFactory(),
            maps = maps,
            merge = { elements -> R(elements.sumOf { it.a }) },
            tag = "result",
        )

        result.changes.subscribe {}

        val changes = mutableListOf<MapChange<K, R>>()

        result.changes.subscribe(changes::add)

        maps.add(
            DynamicMap.of(
                mapOf(
                    K(2) to V(23),
                    K(3) to V(33),
                    K(6) to V(63),
                )
            ),
        )

        assertEquals(
            expected = mapOf(
                K(1) to R(11),
                K(2) to R(21 + 23),
                K(3) to R(31 + 32 + 33),
                K(4) to R(42),
                K(5) to R(52),
                K(6) to R(63),
            ),
            actual = result.volatileContentView,
        )

        assertEquals(
            expected = listOf(
                MapChange(
                    added = mapOf(
                        K(6) to R(63),
                    ),
                    updated = mapOf(
                        K(2) to valueChange(
                            oldValue = R(21),
                            newValue = R(21 + 23),
                        ),
                        K(3) to valueChange(
                            oldValue = R(31 + 32),
                            newValue = R(31 + 32 + 33),
                        ),
                    ),
                    removedEntries = emptyMap(),
                ),
            ),
            actual = changes,
        )
    }

    @Test
    fun testSetRemoved() {
        // "maps" set removed entry

        val map3 = DynamicMap.of(
            mapOf(
                K(2) to V(23),
                K(3) to V(33),
                K(4) to V(43),
                K(6) to V(63),
            )
        )

        val maps = MutableDynamicSet.of(
            setOf(
                DynamicMap.of(
                    mapOf(
                        K(1) to V(11),
                        K(2) to V(21),
                        K(3) to V(31),
                    )
                ),
                DynamicMap.of(
                    mapOf(
                        K(2) to V(22),
                        K(3) to V(32),
                        K(4) to V(42),
                        K(5) to V(52),
                    )
                ),
                map3,
            )
        )

        val result: DynamicMap<K, R> = DynamicMap.unionMerge_(
            through = defaultMapFactory(),
            maps = maps,
            merge = { elements -> R(elements.sumOf { it.a }) },
            tag = "result",
        )

        result.changes.subscribe {}

        val changes = mutableListOf<MapChange<K, R>>()

        result.changes.subscribe(changes::add)

        maps.remove(map3)

        assertEquals(
            expected = mapOf(
                K(1) to R(11),
                K(2) to R(21 + 22),
                K(3) to R(31 + 32),
                K(4) to R(42),
                K(5) to R(52),
            ),
            actual = result.volatileContentView,
        )

        assertEquals(
            expected = listOf(
                MapChange(
                    added = emptyMap(),
                    updated = mapOf(
                        K(2) to valueChange(
                            oldValue = R(21 + 22 + 23),
                            newValue = R(21 + 22),
                        ),
                        K(3) to valueChange(
                            oldValue = R(31 + 32 + 33),
                            newValue = R(31 + 32),
                        ),
                        K(4) to valueChange(
                            oldValue = R(42 + 43),
                            newValue = R(42),
                        ),
                    ),
                    removedEntries = mapOf(
                        K(6) to R(63),
                    ),
                ),
            ),
            actual = changes,
        )
    }

}
