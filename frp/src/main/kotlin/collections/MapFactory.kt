package icesword.collections

import frpjs.Hash
import icesword.frpjs.hybridMapOf

interface MapFactory<K, V> {
    fun create(): MutableMap<K, V>
}

fun <K, V> defaultMapFactory(): MapFactory<K, V> = object : MapFactory<K, V> {
    override fun create(): MutableMap<K, V> = mutableMapOf()
}

value class HybridMapFactory<K, V>(
    private val keyHash: Hash<K>,
) : MapFactory<K, V> {
    override fun create(): MutableMap<K, V> =
        hybridMapOf(keyHash)
}
