package frp.dynamic_list

import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.ListChange
import icesword.frp.dynamic_list.diff
import icesword.frp.dynamic_list.sampleContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DiffTest {
    @Test
    fun testInitialContent() {
        // Given

        val content = Cell.constant(listOf(10, 20, 30, 40, 50))

        // When

        val result = DynamicList.diff(content)

        // Then

        assertEquals(
            expected = listOf(10, 20, 30, 40, 50),
            actual = result.sampleContent(),
        )
    }

    @Test
    fun testAddedNewValue() {
        // Given

        val content = MutCell(listOf(10, 20, 30, 40, 50))

        val result = DynamicList.diff(content)

        val changes = mutableListOf<ListChange<Int>>()

        result.changes.subscribe(changes::add)

        val identifiedContent1 = result.volatileIdentifiedContentView

        assertEquals(
            actual = identifiedContent1.size,
            expected = 5,
        )

        val e10id0v1 = identifiedContent1[0]
        val e20id0v1 = identifiedContent1[1]
        val e30id0v1 = identifiedContent1[2]
        val e40id0v1 = identifiedContent1[3]
        val e50id0v1 = identifiedContent1[4]

        assertEquals(actual = e10id0v1.element, expected = 10)
        assertEquals(actual = e20id0v1.element, expected = 20)
        assertEquals(actual = e30id0v1.element, expected = 30)
        assertEquals(actual = e40id0v1.element, expected = 40)
        assertEquals(actual = e50id0v1.element, expected = 50)

        // When

        content.set(
            listOf(10, 20, 25, 30, 40, 50)
        )

        // Then

        val identifiedContent2 = result.volatileIdentifiedContentView

        assertEquals(
            actual = identifiedContent2.size,
            expected = 6,
        )

        val e10id0v2 = identifiedContent2[0]
        val e20id0v2 = identifiedContent2[1]
        val e25id0v1 = identifiedContent2[2]
        val e30id0v2 = identifiedContent2[3]
        val e40id0v2 = identifiedContent2[4]
        val e50id0v2 = identifiedContent2[5]

        assertEquals(actual = e10id0v2, expected = e10id0v1)
        assertEquals(actual = e20id0v2, expected = e20id0v1)
        assertEquals(actual = e30id0v2, expected = e30id0v1)
        assertEquals(actual = e40id0v2, expected = e40id0v1)
        assertEquals(actual = e50id0v2, expected = e50id0v1)

        assertEquals(actual = e25id0v1.element, expected = 25)

        assertTrue("The new element is equal to one of the others") {
            listOf(e10id0v2, e20id0v2, e30id0v2, e40id0v2, e50id0v2)
                .none { it == e25id0v1 }
        }

        assertMatchesExclusivelyOrdered(
            list = changes,
            assertions = listOf { listChange ->
                val singleAdded = listChange.added.single()

                assertEquals(
                    actual = singleAdded.indexAfter,
                    expected = 2,
                )

                assertEquals(
                    actual = singleAdded.addedElement,
                    expected = e25id0v1,
                )

                assertMatchesExclusivelyUnordered(
                    collection = listChange.reordered,
                    matchers = setOf(
                        {
                            it.indexBefore == 2 && it.indexAfter == 3 && it.reorderedElement == e30id0v1
                        },
                        {
                            it.indexBefore == 3 && it.indexAfter == 4 && it.reorderedElement == e40id0v1
                        },
                        {
                            it.indexBefore == 4 && it.indexAfter == 5 && it.reorderedElement == e50id0v1
                        },
                    )
                )

                assertEquals(
                    actual = listChange.removed.size,
                    expected = 0,
                )
            }
        )
    }

    @Test
    fun testAddedDuplicateValue() {
        // Given

        val content = MutCell(listOf(10, 20, 30, 40, 50))

        val result = DynamicList.diff(content)

        val changes = mutableListOf<ListChange<Int>>()

        result.changes.subscribe(changes::add)

        val identifiedContent1 = result.volatileIdentifiedContentView

        val e10id0v1 = identifiedContent1[0]
        val e20id0v1 = identifiedContent1[1]
        val e30id0v1 = identifiedContent1[2]
        val e40id0v1 = identifiedContent1[3]
        val e50id0v1 = identifiedContent1[4]

        assertEquals(expected = 10, actual = e10id0v1.element)
        assertEquals(expected = 20, actual = e20id0v1.element)
        assertEquals(expected = 30, actual = e30id0v1.element)
        assertEquals(expected = 40, actual = e40id0v1.element)
        assertEquals(expected = 50, actual = e50id0v1.element)

        // When

        content.set(
            listOf(10, 20, 30, 20, 40, 50)
        )

        // Then

        val identifiedContent2 = result.volatileIdentifiedContentView

        assertEquals(
            actual = identifiedContent2.size,
            expected = identifiedContent1.size + 1,
        )

        val e10id0v2 = identifiedContent2[0]
        val e20id0v2 = identifiedContent2[1]
        val e30id0v2 = identifiedContent2[2]
        val e20id1v1 = identifiedContent2[3]
        val e40id0v2 = identifiedContent2[4]
        val e50id0v2 = identifiedContent2[5]

        assertEquals(expected = e10id0v1, actual = e10id0v2)
        assertEquals(expected = e20id0v1, actual = e20id0v2)
        assertEquals(expected = e30id0v1, actual = e30id0v2)
        assertEquals(expected = e40id0v1, actual = e40id0v2)
        assertEquals(expected = e50id0v1, actual = e50id0v2)

        assertEquals(expected = 20, actual = e20id1v1.element)

        assertTrue {
            listOf(e10id0v2, e20id0v2, e30id0v2, e40id0v2, e50id0v2)
                .none { it == e20id1v1 }
        }

        assertMatchesExclusivelyOrdered(
            list = changes,
            assertions = listOf { listChange ->
                val singleAdded = listChange.added.single()

                assertEquals(
                    actual = singleAdded.indexAfter,
                    expected = 3,
                )

                assertEquals(
                    actual = singleAdded.addedElement,
                    expected = e20id1v1,
                )

                assertMatchesExclusivelyUnordered(
                    collection = listChange.reordered,
                    matchers = setOf(
                        {
                            it.indexBefore == 3 && it.indexAfter == 4 && it.reorderedElement == e40id0v1
                        },
                        {
                            it.indexBefore == 4 && it.indexAfter == 5 && it.reorderedElement == e50id0v1
                        },
                    )
                )

                assertEquals(
                    actual = listChange.removed.size,
                    expected = 0,
                )
            }
        )
    }

    @Test
    fun testAddedReordered() {
        // Given

        val content = MutCell(listOf(10, 20, 30, 40, 50))

        val result = DynamicList.diff(content)

        val changes = mutableListOf<ListChange<Int>>()

        result.changes.subscribe(changes::add)

        val identifiedContent1 = result.volatileIdentifiedContentView

        val e10id0v1 = identifiedContent1[0]
        val e20id0v1 = identifiedContent1[1]
        val e30id0v1 = identifiedContent1[2]
        val e40id0v1 = identifiedContent1[3]
        val e50id0v1 = identifiedContent1[4]

        assertEquals(expected = 10, actual = e10id0v1.element)
        assertEquals(expected = 20, actual = e20id0v1.element)
        assertEquals(expected = 30, actual = e30id0v1.element)
        assertEquals(expected = 40, actual = e40id0v1.element)
        assertEquals(expected = 50, actual = e50id0v1.element)

        // When

        content.set(
            listOf(50, 20, 40, 60, 30, 10)
        )

        // Then

        val identifiedContent2 = result.volatileIdentifiedContentView

        assertEquals(
            actual = identifiedContent2.size,
            expected = identifiedContent1.size + 1,
        )

        val e50id0v2 = identifiedContent2[0]
        val e20id0v2 = identifiedContent2[1]
        val e40id0v2 = identifiedContent2[2]
        val e60id0v1 = identifiedContent2[3]
        val e30id0v2 = identifiedContent2[4]
        val e10id0v2 = identifiedContent2[5]

        assertEquals(expected = e10id0v1, actual = e10id0v2)
        assertEquals(expected = e20id0v1, actual = e20id0v2)
        assertEquals(expected = e30id0v1, actual = e30id0v2)
        assertEquals(expected = e40id0v1, actual = e40id0v2)
        assertEquals(expected = e50id0v1, actual = e50id0v2)

        assertEquals(expected = 60, actual = e60id0v1.element)

        assertTrue {
            listOf(e10id0v2, e20id0v2, e30id0v2, e40id0v2, e50id0v2)
                .none { it == e60id0v1 }
        }

        assertMatchesExclusivelyOrdered(
            list = changes,
            assertions = listOf { listChange ->
                val singleAdded = listChange.added.single()

                assertEquals(
                    actual = singleAdded.indexAfter,
                    expected = 3,
                )

                assertEquals(
                    actual = singleAdded.addedElement,
                    expected = e60id0v1,
                )

                // 10, 20, 30, 40, 50
                // 50, 20, 40, 60, 30, 10

                assertMatchesExclusivelyUnordered(
                    collection = listChange.reordered,
                    matchers = setOf(
                        {
                            it.indexBefore == 0 && it.indexAfter == 5 && it.reorderedElement == e10id0v1
                        },
                        {
                            it.indexBefore == 2 && it.indexAfter == 4 && it.reorderedElement == e30id0v1
                        },
                        {
                            it.indexBefore == 3 && it.indexAfter == 2 && it.reorderedElement == e40id0v1
                        },
                        {
                            it.indexBefore == 4 && it.indexAfter == 0 && it.reorderedElement == e50id0v1
                        },
                    )
                )

                assertEquals(
                    actual = listChange.removed.size,
                    expected = 0,
                )
            }
        )
    }

    @Test
    fun testRemoved() {
        // Given

        val content = MutCell(listOf(10, 20, 30, 40, 50))

        val result = DynamicList.diff(content)

        val changes = mutableListOf<ListChange<Int>>()

        result.changes.subscribe(changes::add)

        val identifiedContent1 = result.volatileIdentifiedContentView

        val e10id0v1 = identifiedContent1[0]
        val e20id0v1 = identifiedContent1[1]
        val e30id0v1 = identifiedContent1[2]
        val e40id0v1 = identifiedContent1[3]
        val e50id0v1 = identifiedContent1[4]

        assertEquals(expected = 10, actual = e10id0v1.element)
        assertEquals(expected = 20, actual = e20id0v1.element)
        assertEquals(expected = 30, actual = e30id0v1.element)
        assertEquals(expected = 40, actual = e40id0v1.element)
        assertEquals(expected = 50, actual = e50id0v1.element)

        // When

        content.set(
            listOf(10, 30, 50)
        )

        // Then

        val identifiedContent2 = result.volatileIdentifiedContentView

        assertEquals(
            actual = identifiedContent2.size,
            expected = 3,
        )

        val e10id0v2 = identifiedContent2[0]
        val e30id0v2 = identifiedContent2[1]
        val e50id0v2 = identifiedContent2[2]

        assertEquals(expected = e10id0v1, actual = e10id0v2)
        assertEquals(expected = e30id0v1, actual = e30id0v2)
        assertEquals(expected = e50id0v1, actual = e50id0v2)

        assertMatchesExclusivelyOrdered(
            list = changes,
            assertions = listOf { listChange ->
                assertEquals(
                    actual = listChange.added.size,
                    expected = 0,
                )

                assertMatchesExclusivelyUnordered(
                    collection = listChange.reordered,
                    matchers = setOf(
                        {
                            it.indexBefore == 2 && it.indexAfter == 1 && it.reorderedElement == e30id0v1
                        },
                        {
                            it.indexBefore == 4 && it.indexAfter == 2 && it.reorderedElement == e50id0v1
                        },
                    )
                )

                assertMatchesExclusivelyUnordered(
                    collection = listChange.removed,
                    matchers = setOf(
                        {
                            it.indexBefore == 1 && it.removedElement == e20id0v1
                        },
                        {
                            it.indexBefore == 3 && it.removedElement == e40id0v1
                        },
                    )
                )
            }
        )
    }

    @Test
    fun testReorderedSwap() {
        // Given

        val content = MutCell(listOf(10, 20, 30, 40, 50))

        val result = DynamicList.diff(content)

        val changes = mutableListOf<ListChange<Int>>()

        result.changes.subscribe(changes::add)

        val identifiedContent1 = result.volatileIdentifiedContentView

        val e10id0v1 = identifiedContent1[0]
        val e20id0v1 = identifiedContent1[1]
        val e30id0v1 = identifiedContent1[2]
        val e40id0v1 = identifiedContent1[3]
        val e50id0v1 = identifiedContent1[4]

        assertEquals(expected = 10, actual = e10id0v1.element)
        assertEquals(expected = 20, actual = e20id0v1.element)
        assertEquals(expected = 30, actual = e30id0v1.element)
        assertEquals(expected = 40, actual = e40id0v1.element)
        assertEquals(expected = 50, actual = e50id0v1.element)

        // When

        content.set(
            listOf(10, 20, 50, 40, 30)
        )

        // Then

        val identifiedContent2 = result.volatileIdentifiedContentView

        assertEquals(
            actual = identifiedContent2.size,
            expected = identifiedContent1.size,
        )

        val e10id0v2 = identifiedContent2[0]
        val e20id0v2 = identifiedContent2[1]
        val e50id0v2 = identifiedContent2[2]
        val e40id0v2 = identifiedContent2[3]
        val e30id0v2 = identifiedContent2[4]

        assertTrue { e10id0v2.element == 10 && e10id0v1 == e10id0v2 }
        assertTrue { e20id0v2.element == 20 && e20id0v1 == e20id0v2 }
        assertTrue { e30id0v2.element == 30 && e30id0v1 == e30id0v2 }
        assertTrue { e40id0v2.element == 40 && e40id0v1 == e40id0v2 }
        assertTrue { e50id0v2.element == 50 && e50id0v1 == e50id0v2 }

        assertMatchesExclusivelyOrdered(
            list = changes,
            assertions = listOf { listChange ->
                assertEquals(
                    actual = listChange.added.size,
                    expected = 0,
                )

                assertMatchesExclusivelyUnordered(
                    collection = listChange.reordered,
                    matchers = setOf(
                        {
                            it.indexBefore == 2 && it.indexAfter == 4
                        },
                        {
                            it.indexBefore == 4 && it.indexAfter == 2
                        },
                    )
                )

                assertEquals(
                    actual = listChange.removed.size,
                    expected = 0,
                )
            }
        )
    }

    @Test
    fun testReorderedMovedToFront() {
        // Given

        val content = MutCell(listOf(10, 20, 30, 40, 50))

        val result = DynamicList.diff(content)

        val changes = mutableListOf<ListChange<Int>>()

        result.changes.subscribe(changes::add)

        val identifiedContent1 = result.volatileIdentifiedContentView

        val e10id0v1 = identifiedContent1[0]
        val e20id0v1 = identifiedContent1[1]
        val e30id0v1 = identifiedContent1[2]
        val e40id0v1 = identifiedContent1[3]
        val e50id0v1 = identifiedContent1[4]

        assertEquals(expected = 10, actual = e10id0v1.element)
        assertEquals(expected = 20, actual = e20id0v1.element)
        assertEquals(expected = 30, actual = e30id0v1.element)
        assertEquals(expected = 40, actual = e40id0v1.element)
        assertEquals(expected = 50, actual = e50id0v1.element)

        // When

        content.set(
            listOf(50, 10, 20, 30, 40)
        )

        // Then

        val identifiedContent2 = result.volatileIdentifiedContentView

        assertEquals(
            actual = identifiedContent2.size,
            expected = identifiedContent1.size,
        )

        val e50id0v2 = identifiedContent2[0]
        val e10id0v2 = identifiedContent2[1]
        val e20id0v2 = identifiedContent2[2]
        val e30id0v2 = identifiedContent2[3]
        val e40id0v2 = identifiedContent2[4]

        assertEquals(expected = e10id0v1, actual = e10id0v2)
        assertEquals(expected = e20id0v1, actual = e20id0v2)
        assertEquals(expected = e30id0v1, actual = e30id0v2)
        assertEquals(expected = e40id0v1, actual = e40id0v2)
        assertEquals(expected = e50id0v1, actual = e50id0v2)

        assertMatchesExclusivelyOrdered(
            list = changes,
            assertions = listOf { listChange ->
                assertEquals(
                    actual = listChange.added.size,
                    expected = 0,
                )

                assertMatchesExclusivelyUnordered(
                    collection = listChange.reordered,
                    matchers = setOf(
                        {
                            it.indexBefore == 0 && it.indexAfter == 1
                        },
                        {
                            it.indexBefore == 1 && it.indexAfter == 2
                        },
                        {
                            it.indexBefore == 2 && it.indexAfter == 3
                        },
                        {
                            it.indexBefore == 3 && it.indexAfter == 4
                        },
                        {
                            it.indexBefore == 4 && it.indexAfter == 0
                        },
                    )
                )

                assertEquals(
                    actual = listChange.removed.size,
                    expected = 0,
                )
            }
        )
    }
}
