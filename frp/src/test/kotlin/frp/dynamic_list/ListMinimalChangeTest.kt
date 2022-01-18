package frp.dynamic_list

import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.ListMinimalChange
import icesword.frp.dynamic_list.OrderIdentity
import kotlin.test.Test
import kotlin.test.assertEquals

class ListMinimalChangeTest {
    @Test
    fun testDiffNoop() {
        // When

        val change = ListMinimalChange.diff(
            oldList = listOf(10, 20, 30),
            newList = listOf(10, 20, 30),
        )

        // Then

        assertEquals(
            expected = ListMinimalChange(
                pushIns = emptySet(),
                pullOuts = emptySet(),
            ),
            actual = change,
        )
    }

    @Test
    fun testDiffAddedOnly() {
        // When

        val change = ListMinimalChange.diff(
            oldList = listOf(10, 20, 30, 40, 50),
            newList = listOf(10, 60, 60, 70, 20, 30, 80, 90, 40, 50),
        )

        // Then

        assertEquals(
            expected = ListMinimalChange(
                pushIns = setOf(
                    ListMinimalChange.PushIn(
                        indexBefore = 1,
                        indexAfter = 1,
                        pushedInElements = listOf(
                            DynamicList.IdentifiedElement(
                                element = 60,
                                OrderIdentity(60, 0),
                            ),
                            DynamicList.IdentifiedElement(
                                element = 60,
                                OrderIdentity(60, 1),
                            ),
                            DynamicList.IdentifiedElement(
                                element = 70,
                                OrderIdentity(70, 0),
                            ),
                        ),
                    ),
                    ListMinimalChange.PushIn(
                        indexBefore = 3,
                        indexAfter = 6,
                        pushedInElements = listOf(
                            DynamicList.IdentifiedElement(
                                element = 80,
                                OrderIdentity(80, 0),
                            ),
                            DynamicList.IdentifiedElement(
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

        val change = ListMinimalChange.diff(
            oldList = listOf(10, 20, 30, 40, 50),
            newList = listOf(10, 30, 50),
        )

        // Then

        assertEquals(
            expected = ListMinimalChange(
                pushIns = emptySet(),
                pullOuts = setOf(
                    ListMinimalChange.PullOut(
                        indexBefore = 1,
                        pulledOutElement = DynamicList.IdentifiedElement(
                            element = 20,
                            identity = OrderIdentity(20, 0),
                        ),
                    ),
                    ListMinimalChange.PullOut(
                        indexBefore = 3,
                        pulledOutElement = DynamicList.IdentifiedElement(
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

        val change = ListMinimalChange.diff(
            oldList = listOf(10, 20, 30, 40, 50, 60, 70),
            newList = listOf(10, 80, 90, 80, 30, 100, 60, 70, 110, 50),
        )

        // Then

        assertEquals(
            expected = ListMinimalChange(
                pushIns = setOf(
                    ListMinimalChange.PushIn(
                        indexBefore = 1,
                        indexAfter = 1,
                        pushedInElements = listOf(
                            DynamicList.IdentifiedElement(
                                element = 80,
                                OrderIdentity(80, 0),
                            ),
                            DynamicList.IdentifiedElement(
                                element = 90,
                                OrderIdentity(90, 0),
                            ),
                            DynamicList.IdentifiedElement(
                                element = 80,
                                OrderIdentity(80, 1),
                            ),
                        ),
                    ),
                    ListMinimalChange.PushIn(
                        indexBefore = 3,
                        indexAfter = 5,
                        pushedInElements = listOf(
                            DynamicList.IdentifiedElement(
                                element = 100,
                                OrderIdentity(100, 0),
                            ),
                            DynamicList.IdentifiedElement(
                                element = 60,
                                OrderIdentity(60, 0),
                            ),
                            DynamicList.IdentifiedElement(
                                element = 70,
                                OrderIdentity(70, 0),
                            ),
                            DynamicList.IdentifiedElement(
                                element = 110,
                                OrderIdentity(110, 0),
                            ),
                        ),
                    ),
                ),
                pullOuts = setOf(
                    ListMinimalChange.PullOut(
                        indexBefore = 1,
                        pulledOutElement = DynamicList.IdentifiedElement(
                            element = 20,
                            identity = OrderIdentity(20, 0),
                        ),
                    ),
                    ListMinimalChange.PullOut(
                        indexBefore = 3,
                        pulledOutElement = DynamicList.IdentifiedElement(
                            element = 40,
                            identity = OrderIdentity(40, 0),
                        ),
                    ),
                    ListMinimalChange.PullOut(
                        indexBefore = 5,
                        pulledOutElement = DynamicList.IdentifiedElement(
                            element = 60,
                            identity = OrderIdentity(60, 0),
                        ),
                    ),
                    ListMinimalChange.PullOut(
                        indexBefore = 6,
                        pulledOutElement = DynamicList.IdentifiedElement(
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

        val change = ListMinimalChange.diff(
            oldList = listOf(10, 20, 30, 40, 50, 60, 70),
            newList = listOf(10, 20, 35, 38, 45, 48, 55, 60, 70),
        )

        // Then

        assertEquals(
            expected = ListMinimalChange(
                pushIns = setOf(
                    ListMinimalChange.PushIn(
                        indexBefore = 2,
                        indexAfter = 2,
                        pushedInElements = listOf(
                            DynamicList.IdentifiedElement(
                                element = 35,
                                OrderIdentity(35, 0),
                            ),
                            DynamicList.IdentifiedElement(
                                element = 38,
                                OrderIdentity(38, 0),
                            ),
                            DynamicList.IdentifiedElement(
                                element = 45,
                                OrderIdentity(45, 0),
                            ),
                            DynamicList.IdentifiedElement(
                                element = 48,
                                OrderIdentity(48, 0),
                            ),
                            DynamicList.IdentifiedElement(
                                element = 55,
                                OrderIdentity(55, 0),
                            ),
                        ),
                    ),
                ),
                pullOuts = setOf(
                    ListMinimalChange.PullOut(
                        indexBefore = 2,
                        pulledOutElement = DynamicList.IdentifiedElement(
                            element = 30,
                            identity = OrderIdentity(30, 0),
                        ),
                    ),
                    ListMinimalChange.PullOut(
                        indexBefore = 3,
                        pulledOutElement = DynamicList.IdentifiedElement(
                            element = 40,
                            identity = OrderIdentity(40, 0),
                        ),
                    ),
                    ListMinimalChange.PullOut(
                        indexBefore = 4,
                        pulledOutElement = DynamicList.IdentifiedElement(
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

        val change = ListMinimalChange(
            pushIns = setOf(
                ListMinimalChange.PushIn(
                    indexBefore = 0,
                    indexAfter = 0,
                    pushedInElements = listOf(
                        DynamicList.IdentifiedElement(
                            element = 10, identity = OrderIdentity(10, 0),
                        ),
                        DynamicList.IdentifiedElement(
                            element = 20, identity = OrderIdentity(20, 0),
                        ),
                        DynamicList.IdentifiedElement(
                            element = 30, identity = OrderIdentity(30, 0),
                        ),
                    ),
                ),
                ListMinimalChange.PushIn(
                    indexBefore = 10,
                    indexAfter = 13,
                    pushedInElements = listOf(
                        DynamicList.IdentifiedElement(
                            element = 40, identity = OrderIdentity(40, 0),
                        ),
                        DynamicList.IdentifiedElement(
                            element = 10, identity = OrderIdentity(10, 1),
                        ),
                    ),
                ),
                ListMinimalChange.PushIn(
                    indexBefore = 20,
                    indexAfter = 25,
                    pushedInElements = listOf(
                        DynamicList.IdentifiedElement(
                            element = 50, identity = OrderIdentity(50, 0),
                        ),
                        DynamicList.IdentifiedElement(
                            element = 60, identity = OrderIdentity(60, 1),
                        ),
                    ),
                ),
            ),
            pullOuts = setOf(
                ListMinimalChange.PullOut(
                    indexBefore = 4,
                    pulledOutElement = DynamicList.IdentifiedElement(
                        element = 20, identity = OrderIdentity(element = 20, order = 0),
                    ),
                ),
                ListMinimalChange.PullOut(
                    indexBefore = 5,
                    pulledOutElement = DynamicList.IdentifiedElement(
                        element = 40, identity = OrderIdentity(element = 40, order = 0),
                    ),
                ),
                ListMinimalChange.PullOut(
                    indexBefore = 6,
                    pulledOutElement = DynamicList.IdentifiedElement(
                        element = 25, identity = OrderIdentity(element = 25, order = 0),
                    ),
                ),
                ListMinimalChange.PullOut(
                    indexBefore = 7,
                    pulledOutElement = DynamicList.IdentifiedElement(
                        element = 10, identity = OrderIdentity(element = 10, order = 0),
                    ),
                ),
                ListMinimalChange.PullOut(
                    indexBefore = 15,
                    pulledOutElement = DynamicList.IdentifiedElement(
                        element = 45, identity = OrderIdentity(element = 45, order = 0),
                    ),
                ),
            ),
        )

        // Then

        assertEquals(
            expected = setOf(
                DynamicList.IdentifiedElement(
                    element = 30, identity = OrderIdentity(30, 0),
                ),
                DynamicList.IdentifiedElement(
                    element = 10, identity = OrderIdentity(10, 1),
                ),
                DynamicList.IdentifiedElement(
                    element = 50, identity = OrderIdentity(50, 0),
                ),
                DynamicList.IdentifiedElement(
                    element = 60, identity = OrderIdentity(60, 1),
                ),
            ),
            actual = change.addedElements,
        )

        assertEquals(
            expected = setOf(
                DynamicList.IdentifiedElement(
                    element = 25, identity = OrderIdentity(element = 25, order = 0),
                ),
                DynamicList.IdentifiedElement(
                    element = 45, identity = OrderIdentity(element = 45, order = 0),
                ),
            ),
            actual = change.removedElements,
        )
    }
}
