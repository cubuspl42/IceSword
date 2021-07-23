package icesword.frp

class DynamicMap<K, V>(
    val content: Map<K, V>,
) {
    fun union(baseTiles: DynamicMap<K, V>): DynamicMap<K, V> =
        of(content + baseTiles.content)

    companion object {
        fun <K, V> of(content: Map<K, V>): DynamicMap<K, V> =
            DynamicMap(content)
    }
}
