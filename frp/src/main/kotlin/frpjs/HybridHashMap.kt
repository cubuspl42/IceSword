package icesword.frpjs

import frpjs.Hash
import frpjs.HashImpl
import kotlin.collections.MutableMap.MutableEntry


inline val <K, V> JsMapEntry<K, V>.key: K
    get() = this.asDynamic()[0] as K

inline val <K, V> JsMapEntry<K, V>.value: V
    get() = this.asDynamic()[1] as V


//interface HashTableIterator<K, E> {
//    fun next(): E
//
//    fun hasNext(): Boolean
//}

private value class HybridMapEntry<K, V>(
    private val entry: JsMapEntry<K, V>,
) : MutableEntry<K, V> {
    override val key: K
        get() = entry.key

    override val value: V
        get() = entry.value

    override fun setValue(newValue: V): V {
        TODO("Not yet implemented")
    }
}

private value class HybridIterator<K, V>(
    private val iterator: PeekIterator<JsMapEntry<K, V>>,
) : MutableIterator<MutableEntry<K, V>> {
    override fun hasNext(): Boolean =
        iterator.hasNext()

    override fun next(): MutableEntry<K, V> =
        HybridMapEntry(iterator.next())

    override fun remove() {
        TODO("Not yet implemented")
    }
}

//data class HybridMapEntry<K, V>(
//    override val key: K,
//    override val value: V,
//) : MutableEntry<K, V> {
//    override fun setValue(newValue: V): V {
//        TODO("Not yet implemented")
//    }
//}

//value class HybridMapEntrySetIterator<K, V>(
//    private val iterator: HashTableIterator<K, HybridMapEntry<K, V>>,
//) : MutableIterator<MutableEntry<K, V>> {
//    override fun hasNext(): Boolean =
//        iterator.hasNext()
//
//    override fun next(): MutableEntry<K, V> =
//        iterator.next()
//
//    override fun remove() {
//        TODO("Not yet implemented")
//    }
//}

value class HybridMapEntrySet<K, V>(
    private val hashMap: HashMapJs<K, V>,
) : MutableSet<MutableEntry<K, V>> {
    override val size: Int
        get() = hashMap.size

    override fun contains(element: MutableEntry<K, V>): Boolean =
        hashMap.get(element.key) == element.value

    override fun containsAll(elements: Collection<MutableEntry<K, V>>): Boolean =
        elements.all { contains(it) }

    override fun isEmpty(): Boolean =
        size == 0

    override fun iterator(): MutableIterator<MutableEntry<K, V>> =
        HybridIterator(hashMap.iterate())

    override fun add(element: MutableEntry<K, V>): Boolean {
        TODO("Not yet implemented")
    }

    override fun addAll(elements: Collection<MutableEntry<K, V>>): Boolean =
        elements.map { add(it) }.any()

    override fun clear() {
        hashMap.clear();
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

private value class HybridMapKeySetIterator<K, V>(
    private val iterator: PeekIterator<JsMapEntry<K, V>>,
) : MutableIterator<K> {
    override fun hasNext(): Boolean =
        iterator.hasNext()

    override fun next(): K =
        iterator.next().key

    override fun remove() {
        TODO("Not yet implemented")
    }
}

value class HybridMapKeySet<K, V>(
    private val hashMap: HashMapJs<K, V>,
) : MutableSet<K> {
    override val size: Int
        get() = hashMap.size

    override fun contains(element: K): Boolean =
        hashMap.get(element) != null

    override fun containsAll(elements: Collection<K>): Boolean =
        elements.all { contains(it) }

    override fun isEmpty(): Boolean =
        size == 0

    override fun iterator(): MutableIterator<K> =
        HybridMapKeySetIterator(hashMap.iterate())

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

private class HybridMap<K, V>(
    private val hashMap: HashMapJs<K, V>,
) : MutableMap<K, V> {
    override val entries: MutableSet<MutableEntry<K, V>>
        get() = HybridMapEntrySet<K, V>(hashMap)

    override val keys: MutableSet<K>
        get() = HybridMapKeySet(hashMap)

    override val size: Int
        get() = hashMap.size

    override val values: MutableCollection<V>
        get() = TODO("Not yet implemented")

    override fun containsKey(key: K): Boolean =
        hashMap.has(key)

    override fun containsValue(value: V): Boolean =
        TODO("Not yet implemented")

    override fun get(key: K): V? =
        hashMap.get(key)

    override fun isEmpty(): Boolean =
        size == 0

    override fun clear() {
        hashMap.clear();
    }

    override fun put(key: K, value: V): V? =
        hashMap.set(key, value)

    override fun putAll(from: Map<out K, V>) {
        from.forEach { (key, value) -> put(key, value) }
    }

    override fun remove(key: K): V? =
        hashMap.delete(key)

    override fun equals(other: Any?): Boolean {
        return toMap() == other
    }

    override fun hashCode(): Int {
        return toMap().hashCode()
    }

    override fun toString(): String =
        this.toMap().toString()
}

fun <K, V> hybridMapOf(keyHash: Hash<K> = HashImpl()): MutableMap<K, V> = HybridMap(
    hashMap = HashMapJs(
        entries = null,
        hash = keyHash,
    ),
)
