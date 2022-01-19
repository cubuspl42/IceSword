package frp.dynamic_list

import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.ListChange
import icesword.frp.dynamic_list.OrderIdentity
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
    fun testChange() {
        // Given

        val content = MutCell(listOf(10, 20, 30, 40, 50))

        // When

        val result = DynamicList.diff(content)

        val changes = mutableListOf<ListChange<Int>>()

        result.changes.subscribe(changes::add)

        content.set(
            listOf(10, 20, 21, 22, 30, 50)
        )

        // Then

        assertEquals(
            actual = changes,
            expected = listOf(
                ListChange(
                    pushIns = setOf(
                        ListChange.PushIn(
                            indexBefore = 2,
                            indexAfter = 2,
                            pushedInElements = listOf(
                                DynamicList.IdentifiedElement(
                                    element = 21,
                                    identity = OrderIdentity(element = 21, order = 0),
                                ),
                                DynamicList.IdentifiedElement(
                                    element = 22,
                                    identity = OrderIdentity(element = 22, order = 0),
                                ),
                            )
                        )
                    ),
                    pullOuts = setOf(
                        ListChange.PullOut(
                            indexBefore = 3,
                            pulledOutElement = DynamicList.IdentifiedElement(
                                element = 40,
                                identity = OrderIdentity(element = 40, order = 0),
                            ),
                        )
                    )
                )
            ),
        )

        assertEquals(
            actual = result.volatileIdentifiedContentView,
            expected = listOf(
                DynamicList.IdentifiedElement(
                    element = 10, identity = OrderIdentity(element = 10, order = 0),
                ),
                DynamicList.IdentifiedElement(
                    element = 20, identity = OrderIdentity(element = 20, order = 0),
                ),
                DynamicList.IdentifiedElement(
                    element = 21, identity = OrderIdentity(element = 21, order = 0),
                ),
                DynamicList.IdentifiedElement(
                    element = 22, identity = OrderIdentity(element = 22, order = 0),
                ),
                DynamicList.IdentifiedElement(
                    element = 30, identity = OrderIdentity(element = 30, order = 0),
                ),
                DynamicList.IdentifiedElement(
                    element = 50, identity = OrderIdentity(element = 50, order = 0),
                ),
            )
        )
    }
}
