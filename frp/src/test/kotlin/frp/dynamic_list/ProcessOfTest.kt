package frp.dynamic_list

import icesword.frp.MutCell
import icesword.frp.StreamSink
import icesword.frp.Till
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.DynamicList.IdentifiedElement
import icesword.frp.dynamic_list.ListChange
import icesword.frp.dynamic_list.ReferenceIdentity
import icesword.frp.dynamic_list.store
import icesword.frp.dynamic_list.processOf
import icesword.frp.dynamic_list.staticListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class ProcessOfTest {
    @Test
    fun testInitialContent() {
        // Given

        val source = staticListOf(10, 20, 30, 40, 50)

        val e10 = source.volatileIdentifiedContentView[0]
        val e20 = source.volatileIdentifiedContentView[1]
        val e30 = source.volatileIdentifiedContentView[2]
        val e40 = source.volatileIdentifiedContentView[3]
        val e50 = source.volatileIdentifiedContentView[4]

        val ca = MutCell("a")

        // When the transform function samples something

        val result = source.processOf(Till.never) { ca.sample() + it.toString() }

        // And that thing changes after the operator invocation
        ca.set("b")

        // Then it should be observed that the original value was sampled for
        // initial content

        assertEquals(
            expected = listOf(
                IdentifiedElement(element = "a10", identity = e10.identity),
                IdentifiedElement(element = "a20", identity = e20.identity),
                IdentifiedElement(element = "a30", identity = e30.identity),
                IdentifiedElement(element = "a40", identity = e40.identity),
                IdentifiedElement(element = "a50", identity = e50.identity),
            ),
            actual = result.volatileIdentifiedContentView,
        )
    }

    @Test
    fun testAddedNewValue() {
        // Given

        val e10 = IdentifiedElement(element = 10, identity = ReferenceIdentity.allocate())
        val e20 = IdentifiedElement(element = 20, identity = ReferenceIdentity.allocate())
        val e30 = IdentifiedElement(element = 30, identity = ReferenceIdentity.allocate())
        val e40 = IdentifiedElement(element = 40, identity = ReferenceIdentity.allocate())
        val e50 = IdentifiedElement(element = 50, identity = ReferenceIdentity.allocate())
        val e60 = IdentifiedElement(element = 60, identity = ReferenceIdentity.allocate())
        val e70 = IdentifiedElement(element = 70, identity = ReferenceIdentity.allocate())

        val e11 = IdentifiedElement(element = 11, identity = ReferenceIdentity.allocate())
        val e51 = IdentifiedElement(element = 51, identity = ReferenceIdentity.allocate())

        val sourceChanges = StreamSink<ListChange<Int>>()

        val source = DynamicList.store(
            initialIdentifiedContent = listOf(e10, e20, e30, e40, e50, e60, e70),
            buildChanges = { sourceChanges },
            tillFreeze = Till.never,
        )

        val ca = MutCell("a")

        // When

        val result = source.processOf(Till.never) { ca.sample() + it.toString() }

        val changes = mutableListOf<ListChange<String>>()

        result.changes.subscribe(changes::add)

        ca.set("b")

        sourceChanges.send(
            ListChange(
                pushIns = setOf(
                    ListChange.PushIn(
                        indexBefore = 1,
                        indexAfter = 1,
                        pushedInElements = listOf(e11, e70),
                    ),
                    ListChange.PushIn(
                        indexBefore = 5,
                        indexAfter = 7,
                        pushedInElements = listOf(e51),
                    ),
                ),
                pullOuts = setOf(
                    ListChange.PullOut(
                        indexBefore = 3,
                        pulledOutElement = e40,
                    ),
                    ListChange.PullOut(
                        indexBefore = 6,
                        pulledOutElement = e70,
                    ),
                ),
            ),
        )

        // Then

        val b11 = IdentifiedElement(element = "b11", identity = e11.identity)
        val a40 = IdentifiedElement(element = "a40", identity = e40.identity)
        val b51 = IdentifiedElement(element = "b51", identity = e51.identity)
        val a70 = IdentifiedElement(element = "a70", identity = e70.identity)

        assertEquals(
            actual = changes,
            expected = listOf(
                ListChange(
                    pushIns = setOf(
                        ListChange.PushIn(
                            indexBefore = 1,
                            indexAfter = 1,
                            pushedInElements = listOf(b11, a70),
                        ),
                        ListChange.PushIn(
                            indexBefore = 5,
                            indexAfter = 7,
                            pushedInElements = listOf(b51),
                        ),
                    ),
                    pullOuts = setOf(
                        ListChange.PullOut(
                            indexBefore = 3,
                            pulledOutElement = a40,
                        ),
                        ListChange.PullOut(
                            indexBefore = 6,
                            pulledOutElement = a70,
                        ),
                    ),
                ),
            ),
        )

        assertEquals(
            expected = listOf(
                IdentifiedElement(element = "a10", identity = e10.identity),
                b11,
                a70,
                IdentifiedElement(element = "a20", identity = e20.identity),
                IdentifiedElement(element = "a30", identity = e30.identity),
                IdentifiedElement(element = "a50", identity = e50.identity),
                b51,
                IdentifiedElement(element = "a60", identity = e60.identity),
            ),
            actual = result.volatileIdentifiedContentView,
        )
    }
}
