package frp.dynamic_list

import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.ListOperation
import icesword.frp.dynamic_list.ReferenceIdentity
import icesword.frp.dynamic_list.applyTo
import kotlin.test.Test
import kotlin.test.assertEquals

class ListOperationTest {
    @Test
    fun testInserts() {
        // Given

        val e20 = DynamicList.IdentifiedElement(
            element = 20,
            identity = ReferenceIdentity.allocate(),
        )

        val e30 = DynamicList.IdentifiedElement(
            element = 30,
            identity = ReferenceIdentity.allocate(),
        )

        val e40 = DynamicList.IdentifiedElement(
            element = 40,
            identity = ReferenceIdentity.allocate(),
        )

        val e50 = DynamicList.IdentifiedElement(
            element = 50,
            identity = ReferenceIdentity.allocate(),
        )

        val mutableContent = mutableListOf(e20, e30, e40, e50)

        val operation = ListOperation(
            inserts = listOf(
                ListOperation.InsertElement(
                    insertionIndex = 0,
                    insertedElement = 15,
                ),
                ListOperation.InsertElement(
                    insertionIndex = 2,
                    insertedElement = 35,
                ),
                ListOperation.InsertElement(
                    insertionIndex = 4,
                    insertedElement = 55,
                ),
            ),
            removes = emptySet(),
            reorders = emptyList(),
        )

        // When

        operation.applyTo(mutableContent)

        // Then

        assertIdentifiedContentIsConsistent(
            identifiedContent = mutableContent,
        )

        assertMatchesExclusivelyOrdered(
            list = mutableContent,
            assertions = listOf(
                {
                    assertEquals(expected = 15, actual = it.element)
                },
                {
                    assertEquals(expected = e20, actual = it)
                },
                {
                    assertEquals(expected = e30, actual = it)
                },
                {
                    assertEquals(expected = 35, actual = it.element)
                },
                {
                    assertEquals(expected = e40, actual = it)
                },
                {
                    assertEquals(expected = e50, actual = it)
                },
                {
                    assertEquals(expected = 55, actual = it.element)
                },
            ),
        )
    }

    @Test
    fun testInsertToEmpty() {
        // Given

        val mutableContent = mutableListOf<DynamicList.IdentifiedElement<Int>>()

        val operation = ListOperation(
            inserts = listOf(
                ListOperation.InsertElement(
                    insertionIndex = 0,
                    insertedElement = 10,
                ),
            ),
            removes = emptySet(),
            reorders = emptyList(),
        )

        // When

        operation.applyTo(mutableContent)

        // Then

        assertMatchesExclusivelyOrdered(
            list = mutableContent,
            assertions = listOf {
                assertEquals(expected = 10, actual = it.element)
            }
        )
    }

    @Test
    fun testRemoves() {
        // Given

        val e20 = DynamicList.IdentifiedElement(
            element = 20,
            identity = ReferenceIdentity.allocate(),
        )

        val e30 = DynamicList.IdentifiedElement(
            element = 30,
            identity = ReferenceIdentity.allocate(),
        )

        val e40 = DynamicList.IdentifiedElement(
            element = 40,
            identity = ReferenceIdentity.allocate(),
        )

        val e50 = DynamicList.IdentifiedElement(
            element = 50,
            identity = ReferenceIdentity.allocate(),
        )

        val mutableContent = mutableListOf(e20, e30, e40, e50)

        val operation = ListOperation<Int>(
            inserts = emptyList(),
            removes = setOf(
                ListOperation.RemoveElement(
                    removalIndex = 1,
                ),
                ListOperation.RemoveElement(
                    removalIndex = 3,
                ),
            ),
            reorders = emptyList(),
        )

        // When

        operation.applyTo(mutableContent)

        // Then

        assertMatchesExclusivelyOrdered(
            list = mutableContent,
            assertions = listOf(
                {
                    assertEquals(expected = e20, actual = it)
                },
                {
                    assertEquals(expected = e40, actual = it)
                },
            ),
        )
    }

    @Test
    fun testReorders() {
        // Given

        val e20 = DynamicList.IdentifiedElement(
            element = 20,
            identity = ReferenceIdentity.allocate(),
        )

        val e30 = DynamicList.IdentifiedElement(
            element = 30,
            identity = ReferenceIdentity.allocate(),
        )

        val e40 = DynamicList.IdentifiedElement(
            element = 40,
            identity = ReferenceIdentity.allocate(),
        )

        val e50 = DynamicList.IdentifiedElement(
            element = 50,
            identity = ReferenceIdentity.allocate(),
        )

        val e60 = DynamicList.IdentifiedElement(
            element = 60,
            identity = ReferenceIdentity.allocate(),
        )

        val e70 = DynamicList.IdentifiedElement(
            element = 70,
            identity = ReferenceIdentity.allocate(),
        )

        val mutableContent = mutableListOf(e20, e30, e40, e50, e60, e70)

        val operation = ListOperation<Int>(
            inserts = emptyList(),
            removes = emptySet(),
            reorders = listOf(
                ListOperation.ReorderElement(
                    sourceIndex = 0,
                    destinationIndex = 5,
                ),
                ListOperation.ReorderElement(
                    sourceIndex = 2,
                    destinationIndex = 4,
                ),
                ListOperation.ReorderElement(
                    sourceIndex = 5,
                    destinationIndex = 2,
                ),
            ),
        )

        // When

        operation.applyTo(mutableContent)

        // Then

        // [20->] 30 [->70] [40->] 50 [->40] 60 [->20] [70->]

        assertMatchesExclusivelyOrdered(
            list = mutableContent,
            assertions = listOf(
                {
                    assertEquals(expected = e30, actual = it)
                },
                {
                    assertEquals(expected = e70, actual = it)
                },
                {
                    assertEquals(expected = e50, actual = it)
                },
                {
                    assertEquals(expected = e40, actual = it)
                },
                {
                    assertEquals(expected = e60, actual = it)
                },
                {
                    assertEquals(expected = e20, actual = it)
                },
            ),
        )
    }

    @Test
    fun testMixed() {
        // Given

        val e20 = DynamicList.IdentifiedElement(
            element = 20,
            identity = ReferenceIdentity.allocate(),
        )

        val e30 = DynamicList.IdentifiedElement(
            element = 30,
            identity = ReferenceIdentity.allocate(),
        )

        val e40 = DynamicList.IdentifiedElement(
            element = 40,
            identity = ReferenceIdentity.allocate(),
        )

        val e50 = DynamicList.IdentifiedElement(
            element = 50,
            identity = ReferenceIdentity.allocate(),
        )

        val e60 = DynamicList.IdentifiedElement(
            element = 60,
            identity = ReferenceIdentity.allocate(),
        )

        val e70 = DynamicList.IdentifiedElement(
            element = 70,
            identity = ReferenceIdentity.allocate(),
        )

        val e80 = DynamicList.IdentifiedElement(
            element = 80,
            identity = ReferenceIdentity.allocate(),
        )

        val mutableContent = mutableListOf(e20, e30, e40, e50, e60, e70, e80)

        val operation = ListOperation(
            inserts = listOf(
                ListOperation.InsertElement(
                    insertionIndex = 0,
                    insertedElement = 15,
                ),
                ListOperation.InsertElement(
                    insertionIndex = 1,
                    insertedElement = 25,
                ),
                ListOperation.InsertElement(
                    insertionIndex = 4,
                    insertedElement = 55,
                ),
                ListOperation.InsertElement(
                    insertionIndex = 5,
                    insertedElement = 65,
                ),
            ),
            removes = setOf(
                ListOperation.RemoveElement(
                    removalIndex = 1,
                ),
                ListOperation.RemoveElement(
                    removalIndex = 3,
                ),
            ),
            reorders = listOf(
                ListOperation.ReorderElement(
                    sourceIndex = 0,
                    destinationIndex = 4,
                ),
                ListOperation.ReorderElement(
                    sourceIndex = 5,
                    destinationIndex = 2,
                ),
            )
        )

        // When

        operation.applyTo(mutableContent)

        // Then

        assertIdentifiedContentIsConsistent(
            identifiedContent = mutableContent,
        )

        // [+15] [20->] [+25] -30 [->70] 40 -50 [+55] [->20] 60 [+65] [70->] 80

        assertMatchesExclusivelyOrdered(
            list = mutableContent,
            assertions = listOf(
                {
                    assertEquals(expected = 15, actual = it.element)
                },
                {
                    assertEquals(expected = 25, actual = it.element)
                },
                {
                    assertEquals(expected = e70, actual = it)
                },
                {
                    assertEquals(expected = e40, actual = it)
                },
                {
                    assertEquals(expected = 55, actual = it.element)
                },
                {
                    assertEquals(expected = e20, actual = it)
                },
                {
                    assertEquals(expected = e60, actual = it)
                },
                {
                    assertEquals(expected = 65, actual = it.element)
                },
                {
                    assertEquals(expected = e80, actual = it)
                },
            ),
        )
    }
}

fun <E> assertIdentifiedContentIsConsistent(identifiedContent: List<DynamicList.IdentifiedElement<E>>) {
    val identities = identifiedContent.map { it.identity }.toSet()
    assertEquals(expected = identifiedContent.size, actual = identities.size)
}
