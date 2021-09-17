package frpjs

import icesword.frpjs.HashTable
import kotlin.test.Test
import kotlin.test.assertEquals

class HashTableTest {
    @Test
    fun testSizeBasic() {
        val hashTable = HashTable<Int, Int>(
            hash = HashImpl(),
            extract = { it },
        )

        hashTable.put(1, 1)
        hashTable.put(2, 2)
        hashTable.put(3, 3)

        assertEquals(
            expected = 3,
            actual = hashTable.size,
        )
    }

    @Test
    fun testIterateBasic() {
        val hashTable = HashTable<Int, Int>(
            hash = HashImpl(),
            extract = { it },
        )

        hashTable.put(1, 1)
        hashTable.put(2, 2)
        hashTable.put(3, 3)

        val iterator = hashTable.iterate()

        assertEquals(
            expected = true,
            actual = iterator.hasNext(),
        )

        val value1 = iterator.next()

        assertEquals(
            expected = 1,
            actual = value1,
        )

        assertEquals(
            expected = true,
            actual = iterator.hasNext(),
        )

        val value2 = iterator.next()

        assertEquals(
            expected = 2,
            actual = value2,
        )

        assertEquals(
            expected = true,
            actual = iterator.hasNext(),
        )

        val value3 = iterator.next()

        assertEquals(
            expected = 3,
            actual = value3,
        )

        assertEquals(
            expected = false,
            actual = iterator.hasNext(),
        )
    }

    @Test
    fun testIterateDouble() {
        val hashTable = HashTable<Int, Int>(
            hash = HashImpl(),
            extract = { it },
        )

        hashTable.put(1, 1)
        hashTable.put(2, 2)
        hashTable.put(3, 3)

        val iterator1 = hashTable.iterate()
        val iterator2 = hashTable.iterate()

        assertEquals(
            expected = true,
            actual = iterator1.hasNext(),
        )

        assertEquals(
            expected = true,
            actual = iterator2.hasNext(),
        )

        assertEquals(
            expected = 1,
            actual = iterator1.next(),
        )

        assertEquals(
            expected = 1,
            actual = iterator2.next(),
        )
    }
}
