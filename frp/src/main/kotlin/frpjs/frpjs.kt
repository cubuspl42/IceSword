package frpjs

import icesword.frpjs.*
import kotlin.collections.MutableMap.MutableEntry

external interface Hash<T> {
    fun hash(value: T): Int

    fun isEqual(value1: T, value2: T): Boolean
}

class HashImpl<T> : Hash<T> {
    override fun hash(value: T): Int =
        value.hashCode()

    override fun isEqual(value1: T, value2: T): Boolean =
        value1 == value2
}

interface HashTableIterator<K, E> {
    fun next(): E

    fun hasNext(): Boolean
}

value class FastSetIterator<E>(
    private val iterator: HashTableIterator<E, E>,
) : MutableIterator<E> {
    override fun hasNext(): Boolean =
        iterator.hasNext()

    override fun next(): E =
        iterator.next()

    override fun remove() {
        TODO("Not yet implemented")
    }
}

private value class WrapperIterator<E>(
    private val iterator: PeekIterator<JsMapEntry<E, E>>,
) : MutableIterator<E> {
    override fun hasNext(): Boolean =
        iterator.hasNext()

    override fun next(): E =
        iterator.next().key

    override fun remove() {
        TODO("Not yet implemented")
    }
}

value class FastSet<E>(
    private val hashTable: HashMapJs<E, E>,
) : MutableSet<E> {
    override val size: Int
        get() = hashTable.size

    override fun contains(element: E): Boolean =
        hashTable.get(element) != null

    override fun containsAll(elements: Collection<E>): Boolean =
        elements.all { contains(it) }

    override fun isEmpty(): Boolean =
        size == 0

    override fun iterator(): MutableIterator<E> =
        WrapperIterator(hashTable.iterate())

    override fun add(element: E): Boolean =
        hashTable.set(element, element) != null

    override fun addAll(elements: Collection<E>): Boolean =
        elements.map { add(it) }.any()

    override fun clear() {
        hashTable.clear()
    }

    override fun remove(element: E): Boolean =
        hashTable.delete(element) != null

    override fun removeAll(elements: Collection<E>): Boolean {
        return elements.any { remove(it) }
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        TODO("Not yet implemented")
    }

    override fun toString(): String =
        toSet().toString()
}


fun <E> fastSetOf(hash: Hash<E>): MutableSet<E> =
    FastSet<E>(
        hashTable = HashMapJs(
            hash = hash,
            entries = null,
        ),
    )

data class FastMapEntry<K, V>(
    override val key: K,
    override val value: V,
) : MutableEntry<K, V> {
    override fun setValue(newValue: V): V {
        TODO("Not yet implemented")
    }
}

value class FastMapEntrySetIterator<K, V>(
    private val iterator: HashTableIterator<K, FastMapEntry<K, V>>,
) : MutableIterator<MutableEntry<K, V>> {
    override fun hasNext(): Boolean =
        iterator.hasNext()

    override fun next(): MutableEntry<K, V> =
        iterator.next()

    override fun remove() {
        TODO("Not yet implemented")
    }
}

value class FastMapEntrySet<K, V>(
    private val hashTable: HashTable<K, FastMapEntry<K, V>>,
) : MutableSet<MutableEntry<K, V>> {
    override val size: Int
        get() = hashTable.size

    override fun contains(element: MutableEntry<K, V>): Boolean =
        hashTable.get(element.key) == element.value

    override fun containsAll(elements: Collection<MutableEntry<K, V>>): Boolean =
        elements.all { contains(it) }

    override fun isEmpty(): Boolean =
        size == 0

    override fun iterator(): MutableIterator<MutableEntry<K, V>> =
        FastMapEntrySetIterator(hashTable.iterate())

    override fun add(element: MutableEntry<K, V>): Boolean =
        hashTable.put(
            element.key,
            FastMapEntry(element.key, element.value),
        )?.value != element.value

    override fun addAll(elements: Collection<MutableEntry<K, V>>): Boolean =
        elements.map { add(it) }.any()

    override fun clear() {
        hashTable.clear();
    }

    override fun remove(element: MutableEntry<K, V>): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeAll(elements: Collection<MutableEntry<K, V>>): Boolean {
        TODO("Not yet implemented")
    }

    override fun retainAll(elements: Collection<MutableEntry<K, V>>): Boolean {
        TODO("Not yet implemented")
    }
}

value class FastMapKeySetIterator<K, V>(
    private val iterator: HashTableIterator<K, FastMapEntry<K, V>>,
) : MutableIterator<K> {
    override fun hasNext(): Boolean =
        iterator.hasNext()

    override fun next(): K =
        iterator.next().key

    override fun remove() {
        TODO("Not yet implemented")
    }
}

value class FastMapKeySet<K, V>(
    private val hashTable: HashTable<K, FastMapEntry<K, V>>,
) : MutableSet<K> {
    override val size: Int
        get() = hashTable.size

    override fun contains(element: K): Boolean =
        hashTable.get(element) != null

    override fun containsAll(elements: Collection<K>): Boolean =
        elements.all { contains(it) }

    override fun isEmpty(): Boolean =
        size == 0

    override fun iterator(): MutableIterator<K> =
        FastMapKeySetIterator(hashTable.iterate())

    override fun add(element: K): Boolean {
        TODO("Not yet implemented")
    }

    override fun addAll(elements: Collection<K>): Boolean {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun remove(element: K): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeAll(elements: Collection<K>): Boolean {
        TODO("Not yet implemented")
    }

    override fun retainAll(elements: Collection<K>): Boolean {
        TODO("Not yet implemented")
    }
}

value class FastMap<K, V>(
    private val hashTable: HashTable<K, FastMapEntry<K, V>> = HashTable(
        hash = HashImpl(),
        extract = { it.key },
    ),
) : MutableMap<K, V> {
    override val entries: MutableSet<MutableEntry<K, V>>
        get() = FastMapEntrySet(hashTable)

    override val keys: MutableSet<K>
        get() = FastMapKeySet(hashTable)

    override val size: Int
        get() = hashTable.size

    override val values: MutableCollection<V>
        get() = TODO("Not yet implemented")

    override fun containsKey(key: K): Boolean =
        hashTable.get(key) != null

    override fun containsValue(value: V): Boolean =
        TODO("Not yet implemented")

    override fun get(key: K): V? =
        hashTable.get(key)?.value

    override fun isEmpty(): Boolean =
        size == 0

    override fun clear() {
        hashTable.clear();
    }

    override fun put(key: K, value: V): V? =
        hashTable.put(key, FastMapEntry(key, value))?.value

    override fun putAll(from: Map<out K, V>) {
        from.forEach { (key, value) -> put(key, value) }
    }

    override fun remove(key: K): V? {
        TODO("Not yet implemented")
    }
}
