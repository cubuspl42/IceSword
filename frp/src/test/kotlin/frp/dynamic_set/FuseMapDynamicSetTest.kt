package frp.dynamic_set

import icesword.frp.*
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals

private data class Foo<A>(val cell: Cell<A>)

class FuseMapDynamicSetTest {
    @Test
    fun testInitialContent1() {
        // Given
        val set = DynamicSet.of(
            setOf(11, 22, 33)
        )

        // When

        val out = set.fuseMap {
            Cell.constant(it.toString())
        }

        out.changes.subscribe { }

        // Then

        val initialContent: Set<String> = out.sample()

        assertEquals(
            initialContent,
            setOf("11", "22", "33"),
        )
    }

    @Test
    fun testInitialContent2() {
        // Given

        val set = DynamicSet.of(
            setOf(1.1, 1.2, 2.1, 2.2, 3.0, 4.0)
        )

        // When there are conflicting elements

        val out = set.fuseMap {
            Cell.constant(it.roundToInt())
        }

        out.changes.subscribe { }

        // Then

        val initialContent: Set<Int> = out.sample()

        assertEquals(
            initialContent,
            setOf(1, 2, 3, 4),
        )
    }

    @Test
    fun testSetAdded1() {
        // Given

        val set = MutableDynamicSet.of(
            setOf(
                Foo(Cell.constant(11)),
                Foo(Cell.constant(22)),
            )
        )

        val out = set.fuseMap { it.cell }

        // When a non-colliding element is added to the set

        val changes = mutableListOf<SetChange<Int>>()

        out.changes.subscribe(changes::add)

        set.add(Foo(Cell.constant(33)))

        // Then

        assertEquals(
            listOf(
                SetChange(
                    added = setOf(33),
                    removed = emptySet(),
                ),
            ),
            changes,
        )

        assertEquals(
            out.sample(),
            setOf(11, 22, 33),
        )
    }

    @Test
    fun testSetAdded2() {
        // Given

        val set = MutableDynamicSet.of(
            setOf(
                Foo(Cell.constant(11)),
                Foo(Cell.constant(22)),
            )
        )

        val out = set.fuseMap { it.cell }

        // When a colliding element is added to the set

        val changes = mutableListOf<SetChange<Int>>()

        out.changes.subscribe(changes::add)

        set.add(Foo(Cell.constant(22)))

        // Then

        assertEquals(
            emptyList(),
            changes,
        )

        assertEquals(
            out.sample(),
            setOf(11, 22),
        )
    }


    @Test
    fun testSetRemoved1() {
        // Given

        val element = Foo(Cell.constant(33))

        val set = MutableDynamicSet.of(
            setOf(
                Foo(Cell.constant(11)),
                Foo(Cell.constant(22)),
                element,
            )
        )

        val out = set.fuseMap { it.cell }

        // When a non-colliding element is removed from the set

        val changes = mutableListOf<SetChange<Int>>()

        out.changes.subscribe(changes::add)

        set.remove(element)

        // Then

        assertEquals(
            listOf(
                SetChange(
                    added = emptySet(),
                    removed = setOf(33),
                ),
            ),
            changes,
        )

        assertEquals(
            out.sample(),
            setOf(11, 22),
        )
    }

    @Test
    fun testSetRemoved2() {
        // Given

        val element = Foo(Cell.constant(33))

        val set = MutableDynamicSet.of(
            setOf(
                Foo(Cell.constant(11)),
                Foo(Cell.constant(22)),
                Foo(Cell.constant(33)),
                element,
            )
        )

        val out = set.fuseMap { it.cell }

        // When a colliding element is removed from the set

        val changes = mutableListOf<SetChange<Int>>()

        out.changes.subscribe(changes::add)

        set.remove(element)

        // Then

        assertEquals(
            emptyList(),
            changes,
        )

        assertEquals(
            out.sample(),
            setOf(11, 22, 33),
        )
    }

    @Test
    fun testCellChanged1() {
        // Given

        val cell = MutCell(33)

        val set = MutableDynamicSet.of(
            setOf(
                Foo(Cell.constant(11)),
                Foo(Cell.constant(22)),
                Foo(cell),
            )
        )

        val out = set.fuseMap { it.cell }

        // When fused cell changes from a non-colliding value to a non-colliding value

        val changes = mutableListOf<SetChange<Int>>()

        out.changes.subscribe(changes::add)

        cell.set(34)

        // Then

        assertEquals(
            listOf(
                SetChange(
                    added = setOf(34),
                    removed = setOf(33),
                )
            ),
            changes,
        )

        assertEquals(
            out.sample(),
            setOf(11, 22, 34),
        )
    }


    @Test
    fun testCellChanged2() {
        // Given

        val cell = MutCell(22)

        val set = MutableDynamicSet.of(
            setOf(
                Foo(Cell.constant(11)),
                Foo(Cell.constant(22)),
                Foo(Cell.constant(33)),
                Foo(cell),
            )
        )

        val out = set.fuseMap { it.cell }

        // When fused cell changes from a colliding value to a colliding value

        val changes = mutableListOf<SetChange<Int>>()

        out.changes.subscribe(changes::add)

        cell.set(33)

        // Then

        assertEquals(
            emptyList(),
            changes,
        )

        assertEquals(
            out.sample(),
            setOf(11, 22, 33),
        )
    }

    @Test
    fun testCellChanged3() {
        // Given

        val cell = MutCell(33)

        val set = MutableDynamicSet.of(
            setOf(
                Foo(Cell.constant(11)),
                Foo(Cell.constant(22)),
                Foo(cell),
            )
        )

        val out = set.fuseMap { it.cell }

        // When fused cell changes from a non-colliding value to a colliding value

        val changes = mutableListOf<SetChange<Int>>()

        out.changes.subscribe(changes::add)

        cell.set(22)

        // Then

        assertEquals(
            listOf(
                SetChange(
                    added = emptySet(),
                    removed = setOf(33),
                )
            ),
            changes,
        )

        assertEquals(
            out.sample(),
            setOf(11, 22),
        )
    }


    @Test
    fun testCellChanged4() {
        // Given

        val cell = MutCell(33)

        val set = MutableDynamicSet.of(
            setOf(
                Foo(Cell.constant(11)),
                Foo(Cell.constant(22)),
                Foo(Cell.constant(33)),
                Foo(cell),
            )
        )

        val out = set.fuseMap { it.cell }

        // When fused cell changes from a colliding value to a non-colliding value

        val changes = mutableListOf<SetChange<Int>>()

        out.changes.subscribe(changes::add)

        cell.set(34)

        // Then

        assertEquals(
            listOf(
                SetChange(
                    added = setOf(34),
                    removed = emptySet(),
                )
            ),
            changes,
        )

        assertEquals(
            out.sample(),
            setOf(11, 22, 33, 34),
        )
    }
}
