package frp.dynamic_list

import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.DynamicList.IdentifiedElement
import icesword.frp.dynamic_list.ListChange
import icesword.frp.dynamic_list.OrderIdentity
import icesword.frp.dynamic_list.applyTo
import kotlin.test.Test
import kotlin.test.assertEquals

class ListChangeTest {
    @Test
    fun testDiffNoop() {
        // When

        val change = ListChange.diff(
            oldList = listOf(10, 20, 30),
            newList = listOf(10, 20, 30),
        )

        // Then

        assertEquals(
            expected = ListChange(
                pushIns = emptySet(),
                pullOuts = emptySet(),
            ),
            actual = change,
        )
    }

    @Test
    fun testDiffAddedOnly() {
        // When

        val change = ListChange.diff(
            oldList = listOf(10, 20, 30, 40, 50),
            newList = listOf(10, 60, 60, 70, 20, 30, 80, 90, 40, 50),
        )

        // Then

        assertEquals(
            expected = ListChange(
                pushIns = setOf(
                    ListChange.PushIn(
                        indexBefore = 1,
                        indexAfter = 1,
                        pushedInElements = listOf(
                            IdentifiedElement(
                                element = 60,
                                OrderIdentity(60, 0),
                            ),
                            IdentifiedElement(
                                element = 60,
                                OrderIdentity(60, 1),
                            ),
                            IdentifiedElement(
                                element = 70,
                                OrderIdentity(70, 0),
                            ),
                        ),
                    ),
                    ListChange.PushIn(
                        indexBefore = 3,
                        indexAfter = 6,
                        pushedInElements = listOf(
                            IdentifiedElement(
                                element = 80,
                                OrderIdentity(80, 0),
                            ),
                            IdentifiedElement(
                                element = 90,
                                OrderIdentity(90, 0),
                            ),
                        ),
                    ),
                ),
                pullOuts = emptySet(),
            ),
            actual = change,
        )
    }

    @Test
    fun testDiffRemovedOnly() {
        // When

        val change = ListChange.diff(
            oldList = listOf(10, 20, 30, 40, 50),
            newList = listOf(10, 30, 50),
        )

        // Then

        assertEquals(
            expected = ListChange(
                pushIns = emptySet(),
                pullOuts = setOf(
                    ListChange.PullOut(
                        indexBefore = 1,
                        pulledOutElement = IdentifiedElement(
                            element = 20,
                            identity = OrderIdentity(20, 0),
                        ),
                    ),
                    ListChange.PullOut(
                        indexBefore = 3,
                        pulledOutElement = IdentifiedElement(
                            element = 40,
                            identity = OrderIdentity(40, 0),
                        ),
                    ),
                ),
            ),
            actual = change,
        )
    }

    @Test
    fun testDiffMixed() {
        // When

        val change = ListChange.diff(
            oldList = listOf(10, 20, 30, 40, 50, 60, 70),
            newList = listOf(10, 80, 90, 80, 30, 100, 60, 70, 110, 50),
        )

        // Then

        assertEquals(
            expected = ListChange(
                pushIns = setOf(
                    ListChange.PushIn(
                        indexBefore = 1,
                        indexAfter = 1,
                        pushedInElements = listOf(
                            IdentifiedElement(
                                element = 80,
                                OrderIdentity(80, 0),
                            ),
                            IdentifiedElement(
                                element = 90,
                                OrderIdentity(90, 0),
                            ),
                            IdentifiedElement(
                                element = 80,
                                OrderIdentity(80, 1),
                            ),
                        ),
                    ),
                    ListChange.PushIn(
                        indexBefore = 3,
                        indexAfter = 5,
                        pushedInElements = listOf(
                            IdentifiedElement(
                                element = 100,
                                OrderIdentity(100, 0),
                            ),
                            IdentifiedElement(
                                element = 60,
                                OrderIdentity(60, 0),
                            ),
                            IdentifiedElement(
                                element = 70,
                                OrderIdentity(70, 0),
                            ),
                            IdentifiedElement(
                                element = 110,
                                OrderIdentity(110, 0),
                            ),
                        ),
                    ),
                ),
                pullOuts = setOf(
                    ListChange.PullOut(
                        indexBefore = 1,
                        pulledOutElement = IdentifiedElement(
                            element = 20,
                            identity = OrderIdentity(20, 0),
                        ),
                    ),
                    ListChange.PullOut(
                        indexBefore = 3,
                        pulledOutElement = IdentifiedElement(
                            element = 40,
                            identity = OrderIdentity(40, 0),
                        ),
                    ),
                    ListChange.PullOut(
                        indexBefore = 5,
                        pulledOutElement = IdentifiedElement(
                            element = 60,
                            identity = OrderIdentity(60, 0),
                        ),
                    ),
                    ListChange.PullOut(
                        indexBefore = 6,
                        pulledOutElement = IdentifiedElement(
                            element = 70,
                            identity = OrderIdentity(70, 0),
                        ),
                    ),
                ),
            ),
            actual = change,
        )
    }

    @Test
    fun testDiffMixed2() {
        // When

        val change = ListChange.diff(
            oldList = listOf(10, 20, 30, 40, 50, 60, 70),
            newList = listOf(10, 20, 35, 38, 45, 48, 55, 60, 70),
        )

        // Then

        assertEquals(
            expected = ListChange(
                pushIns = setOf(
                    ListChange.PushIn(
                        indexBefore = 2,
                        indexAfter = 2,
                        pushedInElements = listOf(
                            IdentifiedElement(
                                element = 35,
                                OrderIdentity(35, 0),
                            ),
                            IdentifiedElement(
                                element = 38,
                                OrderIdentity(38, 0),
                            ),
                            IdentifiedElement(
                                element = 45,
                                OrderIdentity(45, 0),
                            ),
                            IdentifiedElement(
                                element = 48,
                                OrderIdentity(48, 0),
                            ),
                            IdentifiedElement(
                                element = 55,
                                OrderIdentity(55, 0),
                            ),
                        ),
                    ),
                ),
                pullOuts = setOf(
                    ListChange.PullOut(
                        indexBefore = 2,
                        pulledOutElement = IdentifiedElement(
                            element = 30,
                            identity = OrderIdentity(30, 0),
                        ),
                    ),
                    ListChange.PullOut(
                        indexBefore = 3,
                        pulledOutElement = IdentifiedElement(
                            element = 40,
                            identity = OrderIdentity(40, 0),
                        ),
                    ),
                    ListChange.PullOut(
                        indexBefore = 4,
                        pulledOutElement = IdentifiedElement(
                            element = 50,
                            identity = OrderIdentity(50, 0),
                        ),
                    ),
                ),
            ),
            actual = change,
        )
    }

    @Test
    fun testAddedRemovedElements() {
        // When

        val change = ListChange(
            pushIns = setOf(
                ListChange.PushIn(
                    indexBefore = 0,
                    indexAfter = 0,
                    pushedInElements = listOf(
                        IdentifiedElement(
                            element = 10, identity = OrderIdentity(10, 0),
                        ),
                        IdentifiedElement(
                            element = 20, identity = OrderIdentity(20, 0),
                        ),
                        IdentifiedElement(
                            element = 30, identity = OrderIdentity(30, 0),
                        ),
                    ),
                ),
                ListChange.PushIn(
                    indexBefore = 10,
                    indexAfter = 13,
                    pushedInElements = listOf(
                        IdentifiedElement(
                            element = 40, identity = OrderIdentity(40, 0),
                        ),
                        IdentifiedElement(
                            element = 10, identity = OrderIdentity(10, 1),
                        ),
                    ),
                ),
                ListChange.PushIn(
                    indexBefore = 20,
                    indexAfter = 25,
                    pushedInElements = listOf(
                        IdentifiedElement(
                            element = 50, identity = OrderIdentity(50, 0),
                        ),
                        IdentifiedElement(
                            element = 60, identity = OrderIdentity(60, 1),
                        ),
                    ),
                ),
            ),
            pullOuts = setOf(
                ListChange.PullOut(
                    indexBefore = 4,
                    pulledOutElement = IdentifiedElement(
                        element = 20, identity = OrderIdentity(element = 20, order = 0),
                    ),
                ),
                ListChange.PullOut(
                    indexBefore = 5,
                    pulledOutElement = IdentifiedElement(
                        element = 40, identity = OrderIdentity(element = 40, order = 0),
                    ),
                ),
                ListChange.PullOut(
                    indexBefore = 6,
                    pulledOutElement = IdentifiedElement(
                        element = 25, identity = OrderIdentity(element = 25, order = 0),
                    ),
                ),
                ListChange.PullOut(
                    indexBefore = 7,
                    pulledOutElement = IdentifiedElement(
                        element = 10, identity = OrderIdentity(element = 10, order = 0),
                    ),
                ),
                ListChange.PullOut(
                    indexBefore = 15,
                    pulledOutElement = IdentifiedElement(
                        element = 45, identity = OrderIdentity(element = 45, order = 0),
                    ),
                ),
            ),
        )

        // Then

        assertEquals(
            expected = setOf(
                IdentifiedElement(
                    element = 30, identity = OrderIdentity(30, 0),
                ),
                IdentifiedElement(
                    element = 10, identity = OrderIdentity(10, 1),
                ),
                IdentifiedElement(
                    element = 50, identity = OrderIdentity(50, 0),
                ),
                IdentifiedElement(
                    element = 60, identity = OrderIdentity(60, 1),
                ),
            ),
            actual = change.addedElements,
        )

        assertEquals(
            expected = setOf(
                IdentifiedElement(
                    element = 25, identity = OrderIdentity(element = 25, order = 0),
                ),
                IdentifiedElement(
                    element = 45, identity = OrderIdentity(element = 45, order = 0),
                ),
            ),
            actual = change.removedElements,
        )
    }

    @Test
    fun testApplyTo() {
        // Given

        val mutableContent = mutableListOf(
            IdentifiedElement(
                element = 20, identity = OrderIdentity(20, 0),
            ),
            IdentifiedElement(
                element = 30, identity = OrderIdentity(30, 0),
            ),
            IdentifiedElement(
                element = 40, identity = OrderIdentity(40, 0),
            ),
            IdentifiedElement(
                element = 50, identity = OrderIdentity(50, 0),
            ),
            IdentifiedElement(
                element = 60, identity = OrderIdentity(60, 0),
            ),
            IdentifiedElement(
                element = 70, identity = OrderIdentity(70, 0),
            ),
        )

        val change = ListChange(
            pushIns = setOf(
                ListChange.PushIn(
                    indexBefore = 0,
                    indexAfter = -1,
                    pushedInElements = listOf(
                        IdentifiedElement(
                            element = 10, identity = OrderIdentity(10, 0),
                        ),
                        IdentifiedElement(
                            element = 11, identity = OrderIdentity(11, 0),
                        ),
                        IdentifiedElement(
                            element = 12, identity = OrderIdentity(12, 0),
                        ),
                    ),
                ),
                ListChange.PushIn(
                    indexBefore = 3,
                    indexAfter = -1,
                    pushedInElements = listOf(
                        IdentifiedElement(
                            element = 41, identity = OrderIdentity(41, 0),
                        ),
                        IdentifiedElement(
                            element = 20, identity = OrderIdentity(20, 1),
                        ),
                    ),
                ),
            ),
            pullOuts = setOf(
                ListChange.PullOut(
                    indexBefore = 1,
                    pulledOutElement = IdentifiedElement(
                        element = 30, identity = OrderIdentity(element = 30, order = 0),
                    ),
                ),
                ListChange.PullOut(
                    indexBefore = 3,
                    pulledOutElement = IdentifiedElement(
                        element = 50, identity = OrderIdentity(element = 50, order = 0),
                    ),
                ),
                ListChange.PullOut(
                    indexBefore = 5,
                    pulledOutElement = IdentifiedElement(
                        element = 70, identity = OrderIdentity(element = 70, order = 0),
                    ),
                ),
            ),
        )

        // When

        change.applyTo(mutableContent)

        // Then

        assertEquals(
            expected = listOf(
                IdentifiedElement(
                    element = 10, identity = OrderIdentity(10, 0),
                ),
                IdentifiedElement(
                    element = 11, identity = OrderIdentity(11, 0),
                ),
                IdentifiedElement(
                    element = 12, identity = OrderIdentity(12, 0),
                ),
                IdentifiedElement(
                    element = 20, identity = OrderIdentity(20, 0),
                ),
                IdentifiedElement(
                    element = 40, identity = OrderIdentity(40, 0),
                ),
                IdentifiedElement(
                    element = 41, identity = OrderIdentity(41, 0),
                ),
                IdentifiedElement(
                    element = 20, identity = OrderIdentity(20, 1),
                ),
                IdentifiedElement(
                    element = 60, identity = OrderIdentity(60, 0),
                ),
            ),
            actual = mutableContent,
        )
    }

    @Test
    fun testApplyToEmpty() {
        // Given

        val mutableContent = mutableListOf<IdentifiedElement<Int>>()

        val change = ListChange(
            pushIns = setOf(
                ListChange.PushIn(
                    indexBefore = 0,
                    indexAfter = 0,
                    pushedInElements = listOf(
                        IdentifiedElement(
                            element = 10, identity = OrderIdentity(10, 0),
                        ),
                    ),
                ),
            ),
            pullOuts = emptySet(),
        )

        // When

        change.applyTo(mutableContent)

        // Then

        assertEquals(
            expected = listOf(
                IdentifiedElement(
                    element = 10, identity = OrderIdentity(10, 0),
                ),
            ),
            actual = mutableContent,
        )
    }
}
