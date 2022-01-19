package frp.dynamic_list

import icesword.frp.StreamSink
import icesword.frp.Till
import icesword.frp.TillMarker
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.DynamicList.IdentifiedElement
import icesword.frp.dynamic_list.ListChange
import icesword.frp.dynamic_list.ReferenceIdentity
import icesword.frp.dynamic_list.mapTillRemoved
import icesword.frp.dynamic_list.staticListOf
import icesword.frp.dynamic_list.store
import kotlin.test.Test
import kotlin.test.assertEquals

class MapTillRemovedTest {
    private data class V1(
        val value: Int,
        val tillRemoved: Till,
    )

    @Test
    fun testInitialContent() {
        // Given

        val source = staticListOf(10, 20, 30)

        val e10 = source.volatileIdentifiedContentView[0]
        val e20 = source.volatileIdentifiedContentView[1]
        val e30 = source.volatileIdentifiedContentView[2]

        // When

        val result = source.mapTillRemoved(Till.never) { element, tillRemoved ->
            V1(value = element, tillRemoved = tillRemoved)
        }

        // Then

        assertMatchesExclusivelyOrdered(
            list = result.volatileIdentifiedContentView,
            assertions = listOf(
                {
                    assertEquals(expected = e10.identity, actual = it.identity)
                    assertEquals(expected = 10, actual = it.element.value)
                    assertEquals(expected = false, actual = it.element.tillRemoved.wasReached())
                },
                {
                    assertEquals(expected = e20.identity, actual = it.identity)
                    assertEquals(expected = 20, actual = it.element.value)
                    assertEquals(expected = false, actual = it.element.tillRemoved.wasReached())
                },
                {
                    assertEquals(expected = e30.identity, actual = it.identity)
                    assertEquals(expected = 30, actual = it.element.value)
                    assertEquals(expected = false, actual = it.element.tillRemoved.wasReached())
                },
            ),
        )
    }

    @Test
    fun testRemoved() {
        // Given

        val e10 = IdentifiedElement(element = 10, identity = ReferenceIdentity.allocate())
        val e20 = IdentifiedElement(element = 20, identity = ReferenceIdentity.allocate())
        val e30 = IdentifiedElement(element = 30, identity = ReferenceIdentity.allocate())
        val e40 = IdentifiedElement(element = 40, identity = ReferenceIdentity.allocate())

        val sourceChanges = StreamSink<ListChange<Int>>()

        val source = DynamicList.store(
            listOf(e10, e20, e30, e40),
            buildChanges = { sourceChanges },
            tillFreeze = Till.never,
        )

        // When

        val result = source.mapTillRemoved(Till.never) { element, tillRemoved ->
            V1(value = element, tillRemoved = tillRemoved)
        }

        val changes = mutableListOf<ListChange<V1>>()

        result.changes.subscribe(changes::add)

        assertEquals(expected = 4, actual = result.volatileContentView.size)

        val v10 = result.volatileContentView[0]
        val v20 = result.volatileContentView[1]
        val v30 = result.volatileContentView[2]
        val v40 = result.volatileContentView[3]

        sourceChanges.send(
            ListChange(
                pushIns = emptySet(),
                pullOuts = setOf(
                    ListChange.PullOut(
                        indexBefore = 1,
                        pulledOutElement = e20,
                    ),
                    ListChange.PullOut(
                        indexBefore = 3,
                        pulledOutElement = e40,
                    ),
                )
            )
        )

        // Then

        assertEquals(expected = false, actual = v10.tillRemoved.wasReached())
        assertEquals(expected = true, actual = v20.tillRemoved.wasReached())
        assertEquals(expected = false, actual = v30.tillRemoved.wasReached())
        assertEquals(expected = true, actual = v40.tillRemoved.wasReached())

        assertMatchesExclusivelyOrdered(
            list = changes,
            assertions = listOf {
                assertEquals(expected = emptySet(), actual = it.pushIns)

                assertMatchesExclusivelyUnordered(
                    collection = it.pullOuts,
                    matchers = setOf(
                        { pullOut ->
                            val pulledOutElement = pullOut.pulledOutElement

                            pullOut.indexBefore == 1 &&
                                    pulledOutElement.identity == e20.identity &&
                                    pulledOutElement.element.value == 20
                        },
                        { pullOut ->
                            val pulledOutElement = pullOut.pulledOutElement

                            pullOut.indexBefore == 3 &&
                                    pulledOutElement.identity == e40.identity &&
                                    pulledOutElement.element.value == 40
                        },
                    ),
                )
            },
        )

        assertMatchesExclusivelyOrdered(
            list = result.volatileIdentifiedContentView,
            assertions = listOf(
                {
                    assertEquals(expected = 10, actual = it.element.value)
                    assertEquals(expected = e10.identity, actual = it.identity)
                },
                {
                    assertEquals(expected = 30, actual = it.element.value)
                    assertEquals(expected = e30.identity, actual = it.identity)
                },
            ),
        )
    }

    @Test
    fun testTillAbort() {
        // Given

        val source = staticListOf(10, 20, 30)

        // When

        val tillAbort = TillMarker()

        val result = source.mapTillRemoved(tillAbort) { element, tillRemoved ->
            V1(value = element, tillRemoved = tillRemoved)
        }

        val v10 = result.volatileContentView[0]
        val v20 = result.volatileContentView[1]
        val v30 = result.volatileContentView[2]

        // Then

        assertEquals(expected = false, actual = v10.tillRemoved.wasReached())
        assertEquals(expected = false, actual = v20.tillRemoved.wasReached())
        assertEquals(expected = false, actual = v30.tillRemoved.wasReached())

        tillAbort.markReached()

        assertEquals(expected = true, actual = v10.tillRemoved.wasReached())
        assertEquals(expected = true, actual = v20.tillRemoved.wasReached())
        assertEquals(expected = true, actual = v30.tillRemoved.wasReached())
    }
}
