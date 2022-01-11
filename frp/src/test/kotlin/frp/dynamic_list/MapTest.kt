package frp.dynamic_list

import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.ListChange
import icesword.frp.dynamic_list.MutableDynamicList
import icesword.frp.dynamic_list.map
import kotlin.test.Test
import kotlin.test.assertEquals

class MapTest {
    @Test
    fun testInitialContent() {
        // Given

        val source = DynamicList.of(listOf(10, 20, 30))

        val sourceContent = source.volatileIdentifiedContentView

        val e10 = sourceContent[0]
        val e20 = sourceContent[1]
        val e30 = sourceContent[2]

        // When

        val result = source.map { it.toString() }

        // Then

        val resultContent = result.volatileIdentifiedContentView

        assertMatchesExclusivelyOrdered(
            list = resultContent,
            assertions = listOf(
                {
                    assertEquals(expected = "10", actual = it.element)
                    assertEquals(expected = e10.identity, actual = it.identity)
                },
                {
                    assertEquals(expected = "20", actual = it.element)
                    assertEquals(expected = e20.identity, actual = it.identity)
                },
                {
                    assertEquals(expected = "30", actual = it.element)
                    assertEquals(expected = e30.identity, actual = it.identity)
                },
            )
        )
    }

    @Test
    fun testAddedNewValue() {
        // Given

        val source = MutableDynamicList(listOf(10, 20, 30))

        val sourceContent1 = source.volatileIdentifiedContentView

        val e10id0 = sourceContent1[0]
        val e20id0 = sourceContent1[1]
        val e30id0 = sourceContent1[2]

        // When

        val result = source.map { it.toString() }

        val changes = mutableListOf<ListChange<String>>()

        result.changes.subscribe(changes::add)

        source.add(20)

        val sourceContent2 = source.volatileIdentifiedContentView

        val e20id1 = sourceContent2.last()

        // Then

        val resultContent = result.volatileIdentifiedContentView

        assertMatchesExclusivelyOrdered(
            list = resultContent,
            assertions = listOf(
                {
                    assertEquals(expected = "10", actual = it.element)
                    assertEquals(expected = e10id0.identity, actual = it.identity)
                },
                {
                    assertEquals(expected = "20", actual = it.element)
                    assertEquals(expected = e20id0.identity, actual = it.identity)
                },
                {
                    assertEquals(expected = "30", actual = it.element)
                    assertEquals(expected = e30id0.identity, actual = it.identity)
                },
                {
                    assertEquals(expected = "20", actual = it.element)
                    assertEquals(expected = e20id1.identity, actual = it.identity)
                },
            )
        )
    }
}
