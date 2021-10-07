@file:JsModule("frp-js")
@file:JsNonModule

package icesword.frpjs

import frpjs.Hash
import frpjs.HashImpl
import frpjs.HashTableIterator

external fun printMsg()

@JsName("FastSet")
external class FastSetJs<E>(
    hash: HashImpl<E>,
) {
    val size: Int

    fun contains(element: E): Boolean

    fun containsAll(elements: Collection<E>): Boolean

    fun iterator(): Iterator<E>
}

@JsName("FastMap")
external class FastMapJs<K, V>(
    hash: HashImpl<K>,
) {
    val entries: FastSetJs<Map.Entry<K, V>>

    val keys: FastSetJs<K>

    val size: Int

    val values: Collection<V>

    fun containsKey(key: K): Boolean

    fun containsValue(value: V): Boolean

    fun get(key: K): V?
}


external class HashTable<K, E>(
    hash: Hash<K>,
    extract: (E) -> K,
) {
    val size: Int

    fun get(key: K): E?

    fun put(key: K, entry: E): E?

    fun clear()

    fun iterate(): HashTableIterator<K, E>
}

external interface PeekIterator<E> {
    fun next(): E

    fun hasNext(): Boolean
}

external interface JsMapEntry<K, V>

@JsName("HashMap")
external class HashMapJs<K, V>(
    entries: Any?,
    hash: HashImpl<K>,
) {
    val size: Int

    fun iterate(): PeekIterator<JsMapEntry<K, V>>

    fun get(key: K): V?

    fun has(key: K): Boolean

    fun clear(): Unit

    fun delete(key: K): V?

    fun set(key: K, value: V): V?
}
